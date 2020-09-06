// Copyright 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beanmodel.PropertyConduit;
import org.apache.tapestry5.beanmodel.PropertyConduit2;
import org.apache.tapestry5.beanmodel.internal.InternalPropertyConduit;
import org.apache.tapestry5.beanmodel.internal.services.Invariant;
import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.IntegerRange;
import org.apache.tapestry5.integration.app1.data.IntegerHolder;
import org.apache.tapestry5.internal.bindings.PropBindingFactoryTest;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.util.Holder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Most of the testing occurs inside {@link PropBindingFactoryTest} (due to
 * historical reasons).
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
        } catch (RuntimeException ex)
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
        } catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Literal values are not updateable.");
        }
    }

    @Test
    public void question_dot_operator_for_object_type()
    {
        InternalPropertyConduit normal = (InternalPropertyConduit) source.create(CompositeBean.class,
                "simple.firstName");
        InternalPropertyConduit smart = (InternalPropertyConduit) source.create(CompositeBean.class,
                "simple?.firstName");

        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        assertEquals(normal.getPropertyName(), "firstName");
        assertEquals(smart.getPropertyName(), "firstName");

        try
        {
            normal.get(bean);
            unreachable();
        } catch (NullPointerException ex)
        {
            // Expected.
        }

        assertNull(smart.get(bean));

        try
        {
            normal.set(bean, "Howard");
            unreachable();
        } catch (NullPointerException ex)
        {
            // Expected.
        }

        // This will be a no-op due to the null property in the expression

        smart.set(bean, "Howard");
    }

    static class GenericBean {
        public List<Date> dates;
        public List<GenericBean> genericBeans;
        
        public List<Long> getLongs() {
            return Collections.emptyList();
        }
        
        public void setMap(Map<String, Integer> map) {
        }
    }
    
    @Test
    public void generic_types_are_determined()
    {
        PropertyConduit2 datesConduit = (PropertyConduit2) source.create(GenericBean.class, "dates");
        PropertyConduit2 longsConduit = (PropertyConduit2) source.create(GenericBean.class, "longs");
        PropertyConduit2 nestedDatesConduit = (PropertyConduit2) source.create(GenericBean.class, "genericBeans.get(0).dates");
        PropertyConduit2 mapConduit = (PropertyConduit2) source.create(GenericBean.class, "map");
        assertEquals(datesConduit.getPropertyGenericType().toString(), "java.util.List<java.util.Date>");
        assertEquals(longsConduit.getPropertyGenericType().toString(), "java.util.List<java.lang.Long>");
        assertEquals(nestedDatesConduit.getPropertyGenericType().toString(), "java.util.List<java.util.Date>");
        assertEquals(mapConduit.getPropertyGenericType().toString(), "java.util.Map<java.lang.String, java.lang.Integer>");
    }
    
    @Test
    public void method_names_are_matched_caselessly()
    {
        InternalPropertyConduit conduit = (InternalPropertyConduit) source.create(CompositeBean.class,
                "GETSIMPLE().firstName");

        assertEquals(conduit.getPropertyName(), "firstName");

        CompositeBean bean = new CompositeBean();
        SimpleBean inner = new SimpleBean();
        bean.setSimple(inner);

        conduit.set(bean, "Howard");

        assertEquals(inner.getFirstName(), "Howard");
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

    public static class One<A, B>
    {
        A a;
        B b;

        public A getA()
        {
            return a;
        }

        public void setA(A a)
        {
            this.a = a;
        }

        public B getB()
        {
            return b;
        }

        public void setB(B b)
        {
            this.b = b;
        }
    }

    public static class Two<B> extends One<String, B>
    {
        String s;
        B b2;

        public String getS()
        {
            return s;
        }

        public void setS(String s)
        {
            this.s = s;
        }

        public B getB2()
        {
            return b2;
        }

        public void setB2(B b2)
        {
            this.b2 = b2;
        }
    }

    public static class Three extends Two<Long>
    {
        Long x;

        public Long getX()
        {
            return x;
        }

        public void setX(Long x)
        {
            this.x = x;
        }
    }

    public static class WithParameters<C, T>
    {
        private C type1Property; // method access
        public C type1Field; // field access
        private T type2Property; // method access
        public T type2Field; // field access

        private T[] type2ArrayProperty;
        public T[] type2ArrayField;

        public C getType1Property()
        {
            return type1Property;
        }

        public void setType1Property(C type1Property)
        {
            this.type1Property = type1Property;
        }

        public T getType2Property()
        {
            return type2Property;
        }

        public void setType2Property(T type2Property)
        {
            this.type2Property = type2Property;
        }

        public T[] getType2ArrayProperty()
        {
            return type2ArrayProperty;
        }

        public void setType2ArrayProperty(T[] type2ArrayProperty)
        {
            this.type2ArrayProperty = type2ArrayProperty;
        }
    }

    public static class RealizedParameters extends WithParameters<Holder<SimpleBean>, Long>
    {
    }

    public static class WithGenericProperties
    {
        public Holder<SimpleBean> holder = new Holder<SimpleBean>();
    }

    public static interface GenericInterface<A, B>
    {
        A genericA();

        B genericB();
    }

    public static class WithRealizedGenericInterface implements GenericInterface<String, Long>
    {
        String a;
        Long b;

        public String genericA()
        {
            return a;
        }

        public Long genericB()
        {
            return b;
        }
    }

    @Test
    public void generic_properties()
    {
        final WithGenericProperties bean = new WithGenericProperties();
        final String first = "John";
        final String last = "Doe";
        final SimpleBean simple = new SimpleBean();
        simple.setLastName(last);
        simple.setAge(2);
        simple.setFirstName(first);
        bean.holder.put(simple);

        PropertyConduit conduit = source.create(WithGenericProperties.class, "holder.get().firstName");
        assertSame(conduit.get(bean), first);
    }

    @Test
    public void generic_parameterized_base_with_properties()
    {
        final String first = "John";
        final String last = "Doe";
        final SimpleBean simple = new SimpleBean();
        simple.setAge(2);
        simple.setFirstName(first);
        simple.setLastName(last);

        final RealizedParameters bean = new RealizedParameters();
        final Holder<SimpleBean> holder = new Holder<SimpleBean>();
        holder.put(simple);
        bean.setType1Property(holder);
        bean.setType2Property(1234L);
        bean.type1Field = holder;
        bean.type2Field = 5678L;
        bean.type2ArrayField = new Long[]
                {123L, 456L};

        PropertyConduit conduit = source.create(RealizedParameters.class, "type1property.get().firstName");
        assertSame(conduit.get(bean), first);
        conduit.set(bean, "Change");
        assertSame(conduit.get(bean), "Change");
        conduit.set(bean, first);

        conduit = source.create(RealizedParameters.class, "type1field.get().firstName");
        assertSame(conduit.get(bean), first);

        conduit = source.create(RealizedParameters.class, "type2field");
        assertEquals(conduit.get(bean), bean.type2Field);

        conduit = source.create(RealizedParameters.class, "type2property");
        assertEquals(conduit.get(bean), bean.getType2Property());

        conduit = source.create(RealizedParameters.class, "type2ArrayField");
        assertEquals(conduit.get(bean), bean.type2ArrayField);

    }

    @Test
    public void generic_interface()
    {
        final WithRealizedGenericInterface bean = new WithRealizedGenericInterface();
        bean.a = "Hello";
        bean.b = 12345L;

        PropertyConduit conduit = source.create(WithRealizedGenericInterface.class, "genericA()");
        assertSame(conduit.get(bean), "Hello");
        conduit = source.create(WithRealizedGenericInterface.class, "genericB()");
        assertEquals(conduit.get(bean), 12345L);
    }

    @Test
    public void generic_nested()
    {
        Three bean = new Three();
        bean.setA("hello");
        bean.setB(123L);
        bean.setB2(1235L);
        bean.setX(54321L);

        PropertyConduit conduit = source.create(Three.class, "a");
        assertSame(conduit.get(bean), "hello");
    }

    @Test
    public void null_root_object()
    {
        PropertyConduit conduit = source.create(StringHolderBean.class, "value.get()");

        try
        {
            conduit.get(null);
            unreachable();
        } catch (NullPointerException ex)
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
        } catch (NullPointerException ex)
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
    public void arrays_as_method_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoArray(storedArray)");
        EchoBean bean = new EchoBean();

        bean.setStoredArray(new Number[][]
                {new Integer[]
                        {1, 2}, new Double[]
                        {3.0, 4.0}});

        Number[][] array = (Number[][]) conduit.get(bean);

        assertArraysEqual(array[0], 1, 2);
        assertArraysEqual(array[1], 3.0, 4.0);
    }

    @Test
    public void top_level_map()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "{'one': true, 'two': 2.0, stringSource.value: 3, 'four': storedString}");
        EchoBean bean = new EchoBean();

        bean.setStoredString("four");
        bean.setStringSource(new StringSource("three"));

        Map actual = (Map) conduit.get(bean);
        assertEquals(actual.get("one"), true);
        assertEquals(actual.get("two"), 2.0);
        assertEquals(actual.get("three"), 3L);
        assertEquals(actual.get("four"), "four");
    }

    @Test
    public void empty_map()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "{ }");
        EchoBean bean = new EchoBean();
        Map m = (Map) conduit.get(bean);

        assertTrue(m.isEmpty());

    }

    @Test
    public void map_as_method_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoMap({ 1: 'one', 2.0: 'two', storedString: stringSource.value })");
        EchoBean bean = new EchoBean();

        bean.setStoredString("3");
        bean.setStringSource(new StringSource("three"));

        Map m = (Map) conduit.get(bean);
        assertEquals("one", m.get(1L));
        assertEquals("two", m.get(2.0));
        assertEquals("three", m.get("3"));

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
        } catch (RuntimeException ex)
        {
            //note that addition of map support changed how this expression was parsed such that the error is now
            //reported at character 8, (, rather than 0: getValue(.
            assertEquals(ex.getMessage(),
                    "Error parsing property expression 'getValue(': line 1:8 no viable alternative at input '('.");
        }
    }

    @Test
    public void lexer_error_in_property_expression()
    {
        try
        {
            source.create(IntegerHolder.class, "fred #");
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                    "Error parsing property expression 'fred #': Unable to parse input at character position 6.");
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

    /**
     * TAP5-747
     */
    @Test
    public void dereference_result_of_method_invocation()
    {
        ComplexObject co = new ComplexObject();
        PropertyConduit pc = source.create(ComplexObject.class, "get(nestedIndex).name");

        assertEquals(pc.get(co), "zero");

        co.setNestedIndex(1);

        assertEquals(pc.get(co), "one");
    }

    @Test
    public void public_object_field()
    {
        PublicFieldBean bean = new PublicFieldBean();

        bean.stringField = "x";

        PropertyConduit pc = source.create(PublicFieldBean.class, "stringField");

        assertEquals(pc.get(bean), "x");

        pc.set(bean, "y");

        assertEquals(bean.stringField, "y");
    }

    @Test
    public void navigate_through_public_field()
    {
        PublicFieldBean bean = new PublicFieldBean();
        PublicFieldBeanHolder holder = new PublicFieldBeanHolder(bean);

        bean.stringField = "x";

        PropertyConduit pc = source.create(PublicFieldBeanHolder.class, "bean.stringField");

        assertEquals(pc.get(holder), "x");

        pc.set(holder, "y");

        assertEquals(bean.stringField, "y");
    }

    @Test
    public void public_primitive_field()
    {
        PublicFieldBean bean = new PublicFieldBean();

        bean.intField = 99;

        // check out the case insensitiveness:

        PropertyConduit pc = source.create(PublicFieldBean.class, "IntField");

        assertEquals(pc.get(bean), new Integer(99));

        pc.set(bean, 37);

        assertEquals(bean.intField, 37);
    }

    @Test
    public void annotation_of_public_field()
    {
        PropertyConduit pc = source.create(PublicFieldBean.class, "StringField");

        assertNotNull(pc.getAnnotation(NonVisual.class));
    }

    /**
     * TAP5-1555
     */
    @Test
    public void this_and_null_inside_array()
    {
        PropertyConduit pc = source.create(NonVisualBean.class, "[this, null]");

        Object bean = new NonVisualBean();

        List list = (List) pc.get(bean);

        assertEquals(list.size(), 2);
        assertSame(list.get(0), bean);
        assertNull(list.get(1));
    }

    /**
     * TAP5-1673
     */
    @Test
    public void public_static_fields_are_accessible()
    {
        PropertyConduit pc = source.create(PublicStaticFieldBean.class, "value");

        assertSame(pc.get(null), PublicStaticFieldBean.VALUE);

        pc.set(null, "new-value");

        assertEquals(PublicStaticFieldBean.VALUE, "new-value");
    }

    @Test
    public void final_static_fields_are_read_only()
    {
        PropertyConduit pc = source.create(PublicStaticFieldBean.class, "read_only");

        try
        {
            pc.set(null, "new-value");
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                    "Expression 'read_only' for class org.apache.tapestry5.internal.services.PublicStaticFieldBean is read-only.");
        }
    }

    @Test
    public void public_static_field_in_an_array()
    {
        PropertyConduit pc = source.create(PublicStaticFieldBean.class, "[read_only]");

        // Need to instantiate it, or exception "Root object of property expression is null"

        List<String> actual = (List<String>) pc.get(new PublicStaticFieldBean());

        assertListsEquals(actual, PublicStaticFieldBean.READ_ONLY);
    }
    
    // TAP5-1493
    @Test
    public void covariant_property_return_type() {

        // example from Howard
        try {
            assertConduitPropertyType(Foo.class, "bar", Bar.class);
        } catch (AssertionError e) {
            List<Method> matches = CollectionFactory.newList();
            for (Method method : Foo.class.getMethods()) {
                if (method.getName().equals("getBar")) {
                    matches.add(method);
                }
            }
            fail(String.format("%s (possible candidates %s)", e.getMessage(), matches)); 
        }
        assertConduitPropertyType(AbstractFoo.class, "bar", AbstractBar.class);
        
        // example from Robert
        assertConduitPropertyType(RobertMyClass.class, "foo.robertBarValue", int.class);
        
    }
    
    // TAP5-1493
    public static abstract class AbstractBar
    {
    }

    public static class Bar extends AbstractBar
    {
    }

    public static abstract class AbstractFoo
    {
        public abstract AbstractBar getBar();
    }

    public static class Foo extends AbstractFoo
    {
        Bar bar;

        public Bar getBar()
        {
            return bar;
        }
    }

    public static interface RobertFoo
    {
        int getRobertFooValue();
    }

    public static interface RobertBar extends RobertFoo
    {
        int getRobertBarValue();
    }

    public static interface RobertBaz
    {
        RobertFoo getFoo();
    }

    public static interface RobertQux extends RobertBaz
    {
        RobertBar getFoo();
    }

    public static class RobertAbstractClass implements RobertBaz
    {
        public RobertFoo getFoo()
        {
            return null;
        }
    }

    public static class RobertMyClass extends RobertAbstractClass implements RobertQux
    {
        public RobertBar getFoo()
        {
            return null;
        }
    }
    
    private void assertConduitPropertyType(Class<?> origin, String property, Class<?> expectedType) {
        assertEquals(expectedType, source.create(origin, property).getPropertyType());
    }
}
