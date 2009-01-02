// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class ContributionDefImplTest extends IOCTestCase implements ModuleBuilderSource
{
    private final OperationTracker tracker = new QuietOperationTracker();

    private Object toContribute;

    public Object getModuleBuilder()
    {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void unordered_contribution()
    {
        toContribute = new Object();
        Configuration configuration = mockConfiguration();
        ServiceResources serviceResources = mockServiceResources(tracker);
        Logger logger = mockLogger();

        train_getLogger(serviceResources, logger);
        train_getServiceId(serviceResources, "Bif");

        configuration.add(toContribute);

        replay();

        Method m = findMethod("contributeUnordered");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, serviceResources, configuration);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void unordered_collection_with_service_lookup()
    {
        Configuration configuration = mockConfiguration();
        ServiceResources resources = mockServiceResources(tracker);
        UpcaseService service = mockUpcaseService();
        Logger logger = mockLogger();

        train_getLogger(resources, logger);
        train_getService(resources, "zip.Zap", UpcaseService.class, service);
        train_getServiceId(resources, "Bif");

        configuration.add(service);

        replay();

        Method m = findMethod("contributeUnorderedParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, resources, configuration);

        verify();
    }

    @Test
    public void unordered_collection_with_incorrect_configuration_parameter()
    {
        Configuration configuration = mockConfiguration();
        ServiceResources resources = mockServiceResources(tracker);
        Logger logger = mockLogger();

        train_getLogger(resources, logger);
        train_getServiceId(resources, "Bif");

        replay();

        Method m = findMethod("contributeUnorderedWrongParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        try
        {
            def.contribute(this, resources, configuration);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Error invoking service contribution method org.apache.tapestry5.ioc.internal.ContributionDefImplTest.contributeUnorderedWrongParameter(MappedConfiguration)",
                                  "Service 'Bif' is configured using org.apache.tapestry5.ioc.Configuration, not org.apache.tapestry5.ioc.MappedConfiguration."
            );
        }

        verify();
    }

    // From here on in, it's an almost identical code path, so we won't be
    // as exhaustive.

    @SuppressWarnings("unchecked")
    @Test
    public void ordered_collection_with_service_lookup()
    {
        OrderedConfiguration configuration = mockOrderedConfiguration();
        ServiceResources resources = mockServiceResources(tracker);
        UpcaseService service = mockUpcaseService();
        Logger logger = mockLogger();

        train_getLogger(resources, logger);

        train_getService(resources, "zip.Zap", UpcaseService.class, service);
        train_getServiceId(resources, "Bif");

        configuration.add("fred", service);

        replay();

        Method m = findMethod("contributeOrderedParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, resources, configuration);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void mapped_collection_with_service_lookup()
    {
        MappedConfiguration configuration = mockMappedConfiguration();
        ServiceResources resources = mockServiceResources(tracker);
        UpcaseService service = mockUpcaseService();
        Logger logger = mockLogger();

        train_getLogger(resources, logger);

        train_getService(resources, "zip.Zap", UpcaseService.class, service);
        train_getServiceId(resources, "Bif");

        configuration.add("upcase", service);

        replay();

        Method m = findMethod("contributeMappedParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, resources, configuration);

        verify();
    }

    private UpcaseService mockUpcaseService()
    {
        return newMock(UpcaseService.class);
    }

    @SuppressWarnings("unchecked")
    public void contributeUnordered(Configuration configuration)
    {
        configuration.add(toContribute);
    }

    public void contributeUnorderedParameter(Configuration<UpcaseService> configuration,
                                             @InjectService("zip.Zap")
                                             UpcaseService service)
    {
        configuration.add(service);
    }

    public void contributeOrderedParameter(OrderedConfiguration<UpcaseService> configuration,
                                           @InjectService("zip.Zap")
                                           UpcaseService service)
    {
        configuration.add("fred", service);
    }

    public void contributeMappedParameter(MappedConfiguration<String, UpcaseService> configuration,
                                          @InjectService("zip.Zap")
                                          UpcaseService service)
    {
        configuration.add("upcase", service);
    }

    public void contributeUnorderedWrongParameter(MappedConfiguration configuration)
    {
        unreachable();
    }
}
