// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.integration.app1.data.RegistrationData;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.BeanEditContext;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.test.TapestryTestCase;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;

public class BeanEditorTest extends TapestryTestCase
{
    @Test
    public void object_created_as_needed()
    {
        ComponentResources resources = mockComponentResources();
        BeanModelSource source = mockBeanModelSource();
        BeanModel model = mockBeanModel();
        RegistrationData data = new RegistrationData();
        Messages messages = mockMessages();
        PropertyOverrides overrides = mockPropertyOverrides();
        Environment env = EasyMock.createNiceMock(Environment.class);

        train_getBoundType(resources, "object", RegistrationData.class);

        train_createEditModel(source, RegistrationData.class, messages, model);

        train_getOverrideMessages(overrides, messages);

        expect(model.newInstance()).andReturn(data);

        replay();
        EasyMock.replay(env);

        BeanEditor component = new BeanEditor();

        component.inject(resources, overrides, source, env);

        component.doPrepare();

        assertSame(component.getObject(), data);

        verify();
    }


    @Test
    public void object_can_not_be_instantiated()
    {
        ComponentResources resources = mockComponentResources();
        BeanModelSource source = mockBeanModelSource();
        BeanModel model = mockBeanModel();
        Location l = mockLocation();
        Throwable exception = new RuntimeException("Fall down go boom.");
        PropertyOverrides overrides = mockPropertyOverrides();
        Messages messages = mockMessages();
        Environment env = EasyMock.createNiceMock(Environment.class);

        train_getOverrideMessages(overrides, messages);

        train_getBoundType(resources, "object", Runnable.class);

        train_createEditModel(source, Runnable.class, messages, model);

        expect(model.newInstance()).andThrow(exception);

        train_getCompleteId(resources, "Foo.bar");

        train_getLocation(resources, l);

        expect(model.getBeanType()).andReturn(Runnable.class);

        replay();
        EasyMock.replay(env);

        BeanEditor component = new BeanEditor();

        component.inject(resources, overrides, source, env);

        try
        {
            component.doPrepare();
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertMessageContains(
                    ex,
                    "Exception instantiating instance of java.lang.Runnable (for component \'Foo.bar\'):");

            assertSame(ex.getLocation(), l);
        }

        verify();
    }

    private static BeanEditContext contextEq()
    {
        EasyMock.reportMatcher(new IArgumentMatcher()
        {
            public void appendTo(StringBuffer buf)
            {
                buf.append("BeanEditContextEq(RegistrationData.class)");
            }

            public boolean matches(Object argument)
            {
                return (argument instanceof BeanEditContext) &&
                        ((BeanEditContext) argument).getBeanClass() == RegistrationData.class;
            }
        });

        return null;
    }

    @Test
    public void beaneditcontext_pushed_to_environment()
    {
        ComponentResources resources = mockComponentResources();
        BeanModelSource source = mockBeanModelSource();
        BeanModel model = mockBeanModel();
        Environment env = mockEnvironment();
        RegistrationData data = new RegistrationData();
        Messages messages = mockMessages();
        PropertyOverrides overrides = mockPropertyOverrides();

        train_getBoundType(resources, "object", RegistrationData.class);

        train_createEditModel(source, RegistrationData.class, messages, model);

        train_getOverrideMessages(overrides, messages);

        expect(model.newInstance()).andReturn(data);
        expect(model.getBeanType()).andReturn(RegistrationData.class);

        BeanEditContext ctxt = new BeanEditContext()
        {
            public Class<?> getBeanClass()
            {
                return RegistrationData.class;
            }

            public <T extends Annotation> T getAnnotation(Class<T> type)
            {
                return null;
            }
        };

        expect(env.push(EasyMock.eq(BeanEditContext.class), contextEq())).andReturn(ctxt);
        replay();

        BeanEditor component = new BeanEditor();

        component.inject(resources, overrides, source, env);

        component.doPrepare();

        verify();
    }

    @Test
    public void beaneditcontext_popped_from_environment()
    {
        ComponentResources resources = mockComponentResources();
        BeanModelSource source = mockBeanModelSource();
        Environment env = mockEnvironment();
        PropertyOverrides overrides = mockPropertyOverrides();

        expect(env.pop(BeanEditContext.class)).andReturn(null);

        replay();

        BeanEditor component = new BeanEditor();

        component.inject(resources, overrides, source, env);

        component.cleanupEnvironment();

        verify();
    }
}
