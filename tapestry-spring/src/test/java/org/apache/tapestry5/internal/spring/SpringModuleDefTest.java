// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.QuietOperationTracker;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.spring.SpringTestCase;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;

public class SpringModuleDefTest extends SpringTestCase
{
    @Test
    public void load_application_context_externally()
    {
        ServletContext servletContext = mockServletContext();
        TypeCoercer tc = mockTypeCoercer();
        SymbolSource ss = mockSymbolSource();
        ConfigurableWebApplicationContext ac = newMock(ConfigurableWebApplicationContext.class);

        ServiceBuilderResources resources = mockServiceBuilderResources();

        train_for_external_spring_context(resources, tc, ss);

        train_getAttribute(servletContext, WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ac);

        train_getTracker(resources, new QuietOperationTracker());

        replay();

        SpringModuleDef moduleDef = new SpringModuleDef(servletContext, null);

        ServiceDef serviceDef = moduleDef.getServiceDef(SpringModuleDef.SERVICE_ID);

        ObjectCreator serviceCreator = serviceDef.createServiceCreator(resources);

        assertSame(serviceCreator.createObject(), ac);

        verify();
    }

    @Test
    public void missing_external_application_context()
    {
        ServletContext servletContext = mockServletContext();
        TypeCoercer tc = mockTypeCoercer();
        SymbolSource ss = mockSymbolSource();

        ServiceBuilderResources resources = mockServiceBuilderResources();

        train_for_external_spring_context(resources, tc, ss);

        train_getAttribute(servletContext, WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, null);

        train_getTracker(resources, new QuietOperationTracker());

        replay();

        SpringModuleDef moduleDef = new SpringModuleDef(servletContext, null);

        ServiceDef serviceDef = moduleDef.getServiceDef(SpringModuleDef.SERVICE_ID);

        ObjectCreator serviceCreator = serviceDef.createServiceCreator(resources);

        try
        {
            serviceCreator.createObject();
            unreachable();
        }
        catch (NullPointerException ex)
        {
            assertMessageContains(ex,
                                  "No Spring ApplicationContext stored in the ServletContext",
                                  WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        }

        verify();
    }

    private void train_for_external_spring_context(ServiceBuilderResources resources, TypeCoercer coercer,
                                                   SymbolSource source)
    {

        train_getService(resources, TypeCoercer.class, coercer);
        train_getService(resources, SymbolSource.class, source);

        expect(source.valueForSymbol(SymbolConstants.USE_EXTERNAL_SPRING_CONTEXT)).andReturn("true");
        train_coerce(coercer, "true", boolean.class, true);
    }
}
