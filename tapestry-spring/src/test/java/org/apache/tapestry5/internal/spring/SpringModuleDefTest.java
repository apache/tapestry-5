// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.spring.SpringConstants;
import org.apache.tapestry5.spring.SpringTestCase;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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
        ConfigurableListableBeanFactory beanFactory = newMock(
                ConfigurableListableBeanFactory.class);
        ConfigurableWebApplicationContext ac = newMock(ConfigurableWebApplicationContext.class);
        Runnable fred = mockRunnable();
        Runnable barney = mockRunnable();
        Runnable arnold = mockRunnable();
        BeanDefinition fredBeanDef = newMock(BeanDefinition.class);
        BeanDefinition barneyBeanDef = newMock(BeanDefinition.class);
        BeanDefinition arnoldBeanDef = newMock(BeanDefinition.class);

        ServiceBuilderResources resources = mockServiceBuilderResources();

        train_getInitParameter(servletContext, SpringConstants.USE_EXTERNAL_SPRING_CONTEXT, "true");

        train_getAttribute(servletContext, WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ac);
        expect(ac.getBeanFactory()).andReturn(beanFactory);
        expect(ac.getBeanDefinitionNames()).andReturn(new String[] { "fred", "&barney", "arnold" });

        expect(fredBeanDef.isAbstract()).andReturn(false);
        expect(barneyBeanDef.isAbstract()).andReturn(false);
        expect(arnoldBeanDef.isAbstract()).andReturn(true);

        expect(beanFactory.getBeanDefinition("fred")).andReturn(fredBeanDef);
        expect(beanFactory.getBeanDefinition("&barney")).andReturn(barneyBeanDef);
        expect(beanFactory.getBeanDefinition("arnold")).andReturn(arnoldBeanDef);

        replay();

        SpringModuleDef moduleDef = new SpringModuleDef(servletContext);

        ServiceDef serviceDef = moduleDef.getServiceDef(SpringModuleDef.SERVICE_ID);

        ObjectCreator serviceCreator = serviceDef.createServiceCreator(resources);

        assertSame(serviceCreator.createObject(), ac);

        verify();

        // Now, let's test for some of the services.

        ServiceDef sd = moduleDef.getServiceDef("ApplicationContext");

        assertEquals(sd.getServiceInterface(), ac.getClass());
        assertEquals(sd.createServiceCreator(null).toString(),
                     "<ObjectCreator for externally configured Spring ApplicationContext>");

        expect((Class)ac.getType("fred")).andReturn(Runnable.class);
        expect(ac.getBean("fred")).andReturn(fred);


        sd = moduleDef.getServiceDef("fred");

        replay();

        assertEquals(sd.getServiceId(), "fred");
        assertEquals(sd.getServiceInterface(), Runnable.class);
        assertEquals(sd.getServiceScope(), ScopeConstants.DEFAULT);
        assertSame(sd.createServiceCreator(null).createObject(), fred);
        assertTrue(sd.getMarkers().isEmpty());
        assertFalse(sd.isEagerLoad());
        assertEquals(sd.createServiceCreator(null).toString(), "ObjectCreator<Spring Bean 'fred'>");

        verify();

        expect((Class)ac.getType("barney")).andReturn(Runnable.class);
        expect(ac.getBean("barney")).andReturn(barney);

        replay();

        sd = moduleDef.getServiceDef("barney");

        assertSame(sd.createServiceCreator(null).createObject(), barney);

        assertNull(moduleDef.getServiceDef("arnold"));
    }

    @Test
    public void missing_external_application_context()
    {
        ServletContext servletContext = mockServletContext();

        train_getInitParameter(servletContext, SpringConstants.USE_EXTERNAL_SPRING_CONTEXT, "true");

        train_getAttribute(servletContext, WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, null);

        replay();

        try
        {
            new SpringModuleDef(servletContext);

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
}
