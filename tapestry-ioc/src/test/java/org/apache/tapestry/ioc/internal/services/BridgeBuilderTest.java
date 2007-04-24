// Copyright 2006, 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.ioc.internal.services;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.testng.annotations.Test;

public class BridgeBuilderTest extends IOCInternalTestCase
{
    private ClassFactory _classFactory = new ClassFactoryImpl();

    @Test
    public void standard_interface_and_filter()
    {
        Log log = mockLog();

        replay();

        BridgeBuilder<StandardService, StandardFilter> bb = new BridgeBuilder<StandardService, StandardFilter>(
                log, StandardService.class, StandardFilter.class, _classFactory);

        StandardFilter sf = new StandardFilter()
        {
            public int run(int i, StandardService ss)
            {
                return ss.run(i + 1);
            }
        };

        StandardService ss = new StandardService()
        {
            public int run(int i)
            {
                return i * 3;
            }
        };

        StandardService bridge = bb.instantiateBridge(ss, sf);

        // The filter adds 1, then the service multiplies by 3.
        // (5 +_1) * 3 = 18.

        assertEquals(bridge.run(5), 18);

        // Since toString() is not part of the service interface,
        // it will be implemented in the bridge.

        assertEquals(
                bridge.toString(),
                "<PipelineBridge from org.apache.tapestry.ioc.internal.services.StandardService to org.apache.tapestry.ioc.internal.services.StandardFilter>");

        verify();
    }

    @Test
    public void toString_part_of_service_interface()
    {
        Log log = mockLog();

        replay();

        BridgeBuilder<ToStringService, ToStringFilter> bb = new BridgeBuilder<ToStringService, ToStringFilter>(
                log, ToStringService.class, ToStringFilter.class, _classFactory);

        ToStringFilter f = new ToStringFilter()
        {
            public String toString(ToStringService s)
            {
                return s.toString().toUpperCase();
            }
        };

        ToStringService s = new ToStringService()
        {
            @Override
            public String toString()
            {
                return "Service";
            }
        };

        ToStringService bridge = bb.instantiateBridge(s, f);

        assertEquals("SERVICE", bridge.toString());

        verify();
    }

    @Test
    public void service_interface_method_not_matched_in_filter_interface()
    {
        Log log = mockLog();
        ExtraServiceMethod next = newMock(ExtraServiceMethod.class);
        Serializable filter = newMock(Serializable.class);

        log
                .error(
                        "Method void extraServiceMethod() has no match in filter interface java.io.Serializable.",
                        null);

        replay();

        BridgeBuilder<ExtraServiceMethod, Serializable> bb = new BridgeBuilder<ExtraServiceMethod, Serializable>(
                log, ExtraServiceMethod.class, Serializable.class, _classFactory);

        ExtraServiceMethod esm = bb.instantiateBridge(next, filter);

        try
        {
            esm.extraServiceMethod();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Method void extraServiceMethod() has no match in filter interface java.io.Serializable.");
        }

        verify();
    }

    @Test
    public void filter_interface_contains_extra_methods()
    {
        Log log = mockLog();
        Serializable next = newMock(Serializable.class);
        ExtraFilterMethod filter = newMock(ExtraFilterMethod.class);

        log
                .error(
                        "Method void extraFilterMethod() of filter interface "
                                + "org.apache.tapestry.ioc.internal.services.ExtraFilterMethod does not have a matching method "
                                + "in java.io.Serializable.",
                        null);

        replay();

        BridgeBuilder<Serializable, ExtraFilterMethod> bb = new BridgeBuilder<Serializable, ExtraFilterMethod>(
                log, Serializable.class, ExtraFilterMethod.class, _classFactory);

        assertNotNull(bb.instantiateBridge(next, filter));

        verify();
    }

    @Test
    public void service_parameter_in_middle_of_filter_method()
    {
        Log log = mockLog();

        replay();

        BridgeBuilder<MiddleService, MiddleFilter> bb = new BridgeBuilder<MiddleService, MiddleFilter>(
                log, MiddleService.class, MiddleFilter.class, _classFactory);

        MiddleFilter mf = new MiddleFilter()
        {
            public void execute(int count, char ch, MiddleService service, StringBuilder buffer)
            {
                service.execute(count, ch, buffer);

                buffer.append(' ');

                service.execute(count + 1, Character.toUpperCase(ch), buffer);

            }
        };

        MiddleService ms = new MiddleService()
        {
            public void execute(int count, char ch, StringBuilder buffer)
            {
                for (int i = 0; i < count; i++)
                    buffer.append(ch);
            }
        };

        // This also tests building the bridge methods with a void return type.

        MiddleService bridge = bb.instantiateBridge(ms, mf);

        StringBuilder buffer = new StringBuilder("CODE: ");

        bridge.execute(3, 'a', buffer);

        assertEquals("CODE: aaa AAAA", buffer.toString());

        verify();
    }
}
