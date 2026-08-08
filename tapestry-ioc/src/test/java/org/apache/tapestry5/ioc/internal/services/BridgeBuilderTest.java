// Copyright 2026 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.ioc.internal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.test.RecordingLogger;
import org.apache.tapestry5.ioc.test.internal.services.ExtraFilterMethod;
import org.apache.tapestry5.ioc.test.internal.services.ExtraServiceMethod;
import org.apache.tapestry5.ioc.test.internal.services.MiddleFilter;
import org.apache.tapestry5.ioc.test.internal.services.MiddleService;
import org.apache.tapestry5.ioc.test.internal.services.StandardFilter;
import org.apache.tapestry5.ioc.test.internal.services.StandardService;
import org.apache.tapestry5.ioc.test.internal.services.ToStringFilter;
import org.apache.tapestry5.ioc.test.internal.services.ToStringService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

class BridgeBuilderTest
{
    private static final String STANDARD_SERVICE = StandardService.class.getName();

    private static final String STANDARD_FILTER = StandardFilter.class.getName();

    private static PlasticProxyFactory proxyFactory;

    private RecordingLogger logger;

    @BeforeAll
    static void createProxyFactory()
    {
        proxyFactory = new PlasticProxyFactoryImpl(BridgeBuilderTest.class.getClassLoader(), null);
    }

    @BeforeEach
    void createLogger()
    {
        logger = new RecordingLogger();
    }

    @Test
    void toStringOfBridgeDescribesThePipeline()
    {
        StandardFilter filter = new StandardFilter()
        {
            @Override
            public int run(int i, StandardService service)
            {
                return service.run(i);
            }

            @Override
            public String toString()
            {
                return "FILTER";
            }
        };

        StandardService next = new StandardService()
        {
            @Override
            public int run(int i)
            {
                return i;
            }

            @Override
            public String toString()
            {
                return "NEXT";
            }
        };

        BridgeBuilder<StandardService, StandardFilter> builder = new BridgeBuilder<>(logger, StandardService.class,
                StandardFilter.class, proxyFactory);

        StandardService bridge = builder.instantiateBridge(next, filter);

        assertEquals(String.format("<PipelineBridge from %s to %s>: FILTER -> NEXT", STANDARD_SERVICE, STANDARD_FILTER),
                bridge.toString());
    }

    @Test
    void bridgeExposesItsFilterAndItsNextStep()
    {
        StandardFilter filter = (i, service) -> service.run(i);

        StandardService next = i -> i;

        BridgeBuilder<StandardService, StandardFilter> builder = new BridgeBuilder<>(logger, StandardService.class,
                StandardFilter.class, proxyFactory);

        PipelineStep bridge = (PipelineStep) builder.instantiateBridge(next, filter);

        assertSame(filter, bridge.getPipelineFilter());
        assertSame(next, bridge.getPipelineNext());
    }

    @Test
    void standardServiceAndInterface()
    {
        AtomicInteger filterCount = new AtomicInteger();
        AtomicInteger serviceCount = new AtomicInteger();

        // The filter runs first and passes 6 to the service, so the result is 3 * (5 + 1).

        StandardFilter filter = (i, service) ->
        {
            filterCount.incrementAndGet();

            return service.run(i + 1);
        };

        StandardService next = i ->
        {
            serviceCount.incrementAndGet();

            return 3 * i;
        };

        BridgeBuilder<StandardService, StandardFilter> builder = new BridgeBuilder<>(logger, StandardService.class,
                StandardFilter.class, proxyFactory);

        StandardService bridge = builder.instantiateBridge(next, filter);

        assertEquals(18, bridge.run(5));

        assertEquals(1, filterCount.get());
        assertEquals(1, serviceCount.get());
    }

    @Test
    void whenToStringIsPartOfServiceInterfaceItIsForwardedThroughTheFilter()
    {
        ToStringService service = new ToStringService()
        {
            @Override
            public String toString()
            {
                return "Service";
            }
        };

        ToStringFilter filter = service1 -> service1.toString().toUpperCase();

        BridgeBuilder<ToStringService, ToStringFilter> builder = new BridgeBuilder<>(logger, ToStringService.class,
                ToStringFilter.class, proxyFactory);

        ToStringService bridge = builder.instantiateBridge(service, filter);

        assertEquals("SERVICE", bridge.toString());

        // toString() is taken by the service interface, but the pipeline can still be walked.

        assertSame(filter, ((PipelineStep) bridge).getPipelineFilter());
        assertSame(service, ((PipelineStep) bridge).getPipelineNext());
    }

    @Test
    void unmatchedServiceInterfaceMethodIsLoggedAndExceptionThrown()
    {
        String expectedMessage = "Method void extraServiceMethod() has no match in filter interface java.io.Serializable.";

        ExtraServiceMethod next = () ->
        {
        };

        Serializable filter = new Serializable()
        {
        };

        BridgeBuilder<ExtraServiceMethod, Serializable> builder = new BridgeBuilder<>(logger, ExtraServiceMethod.class,
                Serializable.class, proxyFactory);

        ExtraServiceMethod bridge = builder.instantiateBridge(next, filter);

        assertEquals(Collections.singletonList(expectedMessage), logger.getMessages(Level.ERROR));

        RuntimeException e = assertThrows(RuntimeException.class, bridge::extraServiceMethod);

        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void extraMethodsInFilterInterfaceAreLoggedAndIgnored()
    {
        Serializable next = new Serializable()
        {
        };

        ExtraFilterMethod filter = () ->
        {
        };

        BridgeBuilder<Serializable, ExtraFilterMethod> builder = new BridgeBuilder<>(logger, Serializable.class,
                ExtraFilterMethod.class, proxyFactory);

        assertNotNull(builder.instantiateBridge(next, filter));

        assertEquals(Collections.singletonList(String.format(
                "Method void extraFilterMethod() of filter interface %s does not have a matching method in %s.",
                ExtraFilterMethod.class.getName(), Serializable.class.getName())), logger.getMessages(Level.ERROR));
    }

    @Test
    void theServiceParameterMayBeAMiddleParameterOfTheFilterMethod()
    {
        MiddleFilter filter = (count, ch, service, buffer) ->
        {
            service.execute(count, ch, buffer);

            buffer.append(' ');

            service.execute(count + 1, Character.toUpperCase(ch), buffer);
        };

        MiddleService next = (count, ch, buffer) ->
        {
            for (int i = 0; i < count; i++)
            {
                buffer.append(ch);
            }
        };

        BridgeBuilder<MiddleService, MiddleFilter> builder = new BridgeBuilder<>(logger, MiddleService.class,
                MiddleFilter.class, proxyFactory);

        MiddleService bridge = builder.instantiateBridge(next, filter);

        StringBuilder buffer = new StringBuilder("CODE: ");

        bridge.execute(3, 'a', buffer);

        assertEquals("CODE: aaa AAAA", buffer.toString());
    }
}
