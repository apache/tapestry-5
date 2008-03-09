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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.internal.bindings.PropBindingFactoryTest;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.services.PropertyConduitSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.Serializable;

/**
 * Most of the testing occurs inside {@link PropBindingFactoryTest} (due to historical reasons).
 */
public class PropertyConduitSourceImplTest extends InternalBaseTestCase
{
    private PropertyConduitSource _source;

    @BeforeClass
    public void setup()
    {
        _source = getObject(PropertyConduitSource.class, null);
    }

    @AfterClass
    public void cleanup()
    {
        _source = null;
    }

    @Test
    public void question_dot_operator_for_object_type()
    {
        PropertyConduit normal = _source.create(CompositeBean.class, "simple.firstName");
        PropertyConduit smart = _source.create(CompositeBean.class, "simple?.firstName");

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
        PropertyConduit conduit = _source.create(CompositeBean.class, "GETSIMPLE().firstName");

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

        PropertyConduit conduit = _source.create(proxyClass, "firstName");

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

        PropertyConduit conduit = _source.create(StringHolderBean.class, "value.get()");

        assertSame(conduit.get(bean), string);

        assertSame(conduit.getPropertyType(), String.class);
    }

}
