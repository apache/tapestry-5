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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.internal.bindings.PropBindingFactoryTest;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.Serializable;

/**
 * Most of the testing occurs inside {@link PropBindingFactoryTest} (due to historical reasons).
 */
public class PropertyConduitSourceImplTest extends InternalBaseTestCase
{
    private PropertyConduitSource source;

    @BeforeClass
    public void setup()
    {
        source = getObject(PropertyConduitSource.class, null);
    }

    @AfterClass
    public void cleanup()
    {
        source = null;
    }

    @Test
    public void question_dot_operator_for_object_type()
    {
        PropertyConduit normal = source.create(CompositeBean.class, "simple.firstName");
        PropertyConduit smart = source.create(CompositeBean.class, "simple?.firstName");

        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        try
        {
            normal.get(bean);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            // Expected.
        }

        assertNull(smart.get(bean));

        try
        {
            normal.set(bean, "Howard");
            unreachable();
        }
        catch (NullPointerException ex)
        {
            // Expected.
        }

        // This will be a no-op due to the null property in the expression

        smart.set(bean, "Howard");
    }

    @Test
    public void method_names_are_matched_caselessly()
    {
        PropertyConduit conduit = source.create(CompositeBean.class, "GETSIMPLE().firstName");

        CompositeBean bean = new CompositeBean();
        SimpleBean inner = new SimpleBean();
        bean.setSimple(inner);

        conduit.set(bean, "Howard");

        assertEquals(inner.getFirstName(), "Howard");
    }

    /**
     * Or call this the "Hibernate" case; Hibernate creates sub-classes of entity classes in its own class loader to do
     * all sorts of proxying. This trips up Javassist.
     */
    @Test
    public void handle_beans_from_unexpected_classloader() throws Exception
    {
        // First, create something that looks like a Hibernate proxy.

        ClassFactory factory = new ClassFactoryImpl();

        Class clazz = SimpleBean.class;

        ClassFab cf = factory.newClass(clazz.getName() + "$$Proxy", clazz);

        cf.addInterface(Serializable.class);

        Class proxyClass = cf.createClass();

        SimpleBean simple = (SimpleBean) proxyClass.newInstance();

        assertTrue(simple instanceof Serializable);

        simple.setFirstName("Howard");

        PropertyConduit conduit = source.create(proxyClass, "firstName");

        assertEquals(conduit.get(simple), "Howard");
    }

    @Test
    public void generics()
    {
        String string = "surprise";
        StringHolder stringHolder = new StringHolder();
        stringHolder.put(string);
        StringHolderBean bean = new StringHolderBean();
        bean.setValue(stringHolder);

        PropertyConduit conduit = source.create(StringHolderBean.class, "value.get()");

        assertSame(conduit.get(bean), string);

        assertSame(conduit.getPropertyType(), String.class);
    }

    @Test
    public void null_root_object()
    {
        PropertyConduit conduit = source.create(StringHolderBean.class, "value.get()");

        try
        {
            conduit.get(null);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Root object of property expression 'value.get()' is null.");
        }
    }

    @Test
    public void null_property_in_chain()
    {
        PropertyConduit conduit = source.create(CompositeBean.class, "simple.lastName");

        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        try
        {
            conduit.get(bean);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            assertMessageContains(ex, "Property 'simple' (within property expression 'simple.lastName', of",
                                  ") is null.");
        }
    }

    @Test
    public void last_term_may_be_null()
    {
        PropertyConduit conduit = source.create(CompositeBean.class, "simple.firstName");

        CompositeBean bean = new CompositeBean();

        bean.getSimple().setFirstName(null);

        assertNull(conduit.get(bean));
    }

    @Test
    public void field_annotations_are_visible()
    {
        PropertyConduit conduit = source.create(CompositeBean.class, "simple.firstName");

        Validate annotation = conduit.getAnnotation(Validate.class);

        assertNotNull(annotation);

        assertEquals(annotation.value(), "required");
    }

}
