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

package org.apache.tapestry.ioc.internal;

import java.lang.reflect.Method;

import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.ModuleBuilderSource;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class ContributionDefImplTest extends IOCTestCase implements ModuleBuilderSource
{
    private Object _toContribute;

    public Object getModuleBuilder()
    {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void unordered_contribution()
    {
        _toContribute = new Object();
        Configuration configuration = newConfiguration();
        ServiceLocator locator = newServiceLocator();

        configuration.add(_toContribute);

        replay();

        Method m = findMethod("contributeUnordered");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, locator, configuration);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void unordered_collection_with_service_lookup()
    {
        Configuration configuration = newConfiguration();
        ServiceLocator locator = newServiceLocator();
        UpcaseService service = newUpcaseService();

        train_getService(locator, "zip.Zap", UpcaseService.class, service);

        configuration.add(service);

        replay();

        Method m = findMethod("contributeUnorderedParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, locator, configuration);

        verify();
    }

    @Test
    public void unordered_collection_with_incorrect_configuration_parameter()
    {
        Configuration configuration = newConfiguration();
        ServiceLocator locator = newServiceLocator();

        Throwable t = new RuntimeException("Missing service.");

        expect(locator.getService(MappedConfiguration.class)).andThrow(t);

        replay();

        Method m = findMethod("contributeUnorderedWrongParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        try
        {
            def.contribute(this, locator, configuration);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Error invoking service contribution method "
                    + getClass().getName()
                    + ".contributeUnorderedWrongParameter(MappedConfiguration): Missing service.");
        }

        verify();
    }

    // From here on in, it's an almost identical code path, so we won't be
    // as exhaustive.

    @SuppressWarnings("unchecked")
    @Test
    public void ordered_collection_with_service_lookup()
    {
        OrderedConfiguration configuration = newOrderedConfiguration();
        ServiceLocator locator = newServiceLocator();
        UpcaseService service = newUpcaseService();

        train_getService(locator, "zip.Zap", UpcaseService.class, service);

        configuration.add("fred", service);

        replay();

        Method m = findMethod("contributeOrderedParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, locator, configuration);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void mapped_collection_with_service_lookup()
    {
        MappedConfiguration configuration = newMappedConfiguration();
        ServiceLocator locator = newServiceLocator();
        UpcaseService service = newUpcaseService();

        train_getService(locator, "zip.Zap", UpcaseService.class, service);

        configuration.add("upcase", service);

        replay();

        Method m = findMethod("contributeMappedParameter");
        ContributionDef def = new ContributionDefImpl("foo.Bar", m, null);

        def.contribute(this, locator, configuration);

        verify();
    }

    private UpcaseService newUpcaseService()
    {
        return newMock(UpcaseService.class);
    }

    @SuppressWarnings("unchecked")
    public void contributeUnordered(Configuration configuration)
    {
        configuration.add(_toContribute);
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
