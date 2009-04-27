// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Block;
import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.integration.app1.data.IntegerHolder;
import org.apache.tapestry5.internal.bindings.PropBindingFactoryTest;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.util.IntegerRange;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.List;

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
    public void literal_conduits_have_invariant_annotation()
    {
        PropertyConduit pc = source.create(CompositeBean.class, "12345");

        Invariant annotation = pc.getAnnotation(Invariant.class);

        assertNotNull(annotation);

        assertSame(annotation.annotationType(), Invariant.class);
    }

    @Test
    public void range_variable_to()
    {
        PropertyConduit pc = source.create(IntegerHolder.class, "10..value");
        IntegerHolder h = new IntegerHolder();

        h.setValue(5);

        IntegerRange ir = (IntegerRange) pc.get(h);

        assertEquals(ir, new IntegerRange(10, 5));
    }

    @Test
    public void range_variable_from()
    {
        PropertyConduit pc = source.create(IntegerHolder.class, "value..99");
        IntegerHolder h = new IntegerHolder();

        h.setValue(72);

        IntegerRange ir = (IntegerRange) pc.get(h);

        assertEquals(ir, new IntegerRange(72, 99));
    }


    @Test
    public void literal_conduits_are_not_updateable()
    {
        PropertyConduit pc = source.create(CompositeBean.class, "12345");
        CompositeBean bean = new CompositeBean();

        try
        {
            pc.set(bean, 42);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Literal values are not updateable.");
        }
    }

    @Test
    public void this_literal_conduit_is_not_updateable()
    {
        PropertyConduit normal = source.create(CompositeBean.class, "this");
        CompositeBean bean = new CompositeBean();

        try
        {
            normal.set(bean, 42);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Literal values are not updateable.");
        }
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

    @Test
    public void method_invocation_with_integer_arguments()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoInt(storedInt, 3)");
        EchoBean bean = new EchoBean();

        for (int i = 0; i < 10; i++)
        {
            bean.setStoredInt(i);
            assertEquals(conduit.get(bean), new Integer(i * 3));
        }
    }

    @Test
    public void method_invocation_with_double_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoDouble(storedDouble, 2.0)");
        EchoBean bean = new EchoBean();

        double value = 22. / 7.;

        bean.setStoredDouble(value);

        assertEquals(conduit.get(bean), new Double(2. * value));
    }

    @Test
    public void method_invocation_with_string_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoString(storedString, 'B4', 'AFTER')");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Moe");

        assertEquals(conduit.get(bean), "B4 - Moe - AFTER");
    }

    @Test
    public void method_invocation_using_dereference()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoString(storedString, stringSource.value, 'beta')");
        EchoBean bean = new EchoBean();

        StringSource source = new StringSource("alpha");

        bean.setStringSource(source);
        bean.setStoredString("Barney");

        assertEquals(conduit.get(bean), "alpha - Barney - beta");
    }

    @Test
    public void top_level_list()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "[ 1, 2.0, storedString ]");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Lisa");

        List l = (List) conduit.get(bean);

        assertListsEquals(l, new Long(1), new Double(2.0), "Lisa");
    }

    @Test
    public void empty_list()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "[  ]");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Lisa");

        List l = (List) conduit.get(bean);

        assertEquals(l.size(), 0);
    }

    @Test
    public void list_as_method_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoList([ 1, 2.0, storedString ])");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Bart");

        List l = (List) conduit.get(bean);

        assertListsEquals(l, new Long(1), new Double(2.0), "Bart");
    }

    @Test
    public void not_operator()
    {
        PropertyConduit conduit = source.create(IntegerHolder.class, "! value");
        IntegerHolder holder = new IntegerHolder();

        assertEquals(conduit.get(holder), Boolean.TRUE);

        holder.setValue(99);

        assertEquals(conduit.get(holder), Boolean.FALSE);
    }

    @Test
    public void not_operator_in_subexpression()
    {
        PropertyConduit conduit = source.create(Switch.class, "label(! value)");

        Switch sw = new Switch();

        assertEquals(conduit.get(sw), "aye");

        sw.setValue(true);

        assertEquals(conduit.get(sw), "nay");
    }

    /**
     * TAP5-330
     */
    @Test
    public void object_methods_can_be_invoked()
    {
        PropertyConduit conduit = source.create(Block.class, "toString()");

        Block b = new Block()
        {
            @Override
            public String toString()
            {
                return "Do You Grok Ze Block?";
            }
        };

        assertEquals(conduit.get(b), "Do You Grok Ze Block?");
    }

    @Test
    public void parse_error_in_property_expression()
    {
        try
        {
            source.create(IntegerHolder.class, "getValue(");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Error parsing property expression 'getValue(': line 1:0 no viable alternative at input 'getValue'.");
        }
    }

    @Test
    public void lexer_error_in_property_expression()
    {
        try
        {
            source.create(IntegerHolder.class, "fred {");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Error parsing property expression 'fred {': Unable to parse input at character position 6.");
        }
    }

    @Test
    public void boolean_constant_as_method_parameter()
    {
        Bedrock bedrock = new Bedrock();

        PropertyConduit trueConduit = source.create(Bedrock.class, "toName(true)");
        PropertyConduit falseConduit = source.create(Bedrock.class, "toName(false)");

        assertEquals(trueConduit.get(bedrock), "Fred");
        assertEquals(falseConduit.get(bedrock), "Barney");
    }
}
