// Copyright 2007, 2008, 2009, 2010, 2011, 2026 The Apache Software Foundation
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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
import org.apache.tapestry5.commons.util.IntegerRange;
import org.apache.tapestry5.integration.app1.data.IntegerHolder;
import org.apache.tapestry5.internal.bindings.PropBindingFactoryTest;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.modules.TapestryModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Most of the testing occurs inside {@link PropBindingFactoryTest} (due to
 * historical reasons).
 */
public class PropertyConduitSourceImplTest
{
    private static Registry registry;

    private static PropertyConduitSource source;

    @BeforeAll
    static void setup()
    {
        registry = new RegistryBuilder().add(TapestryModule.class, ForceDevelopmentModeModule.class).build();
        registry.performRegistryStartup();

        source = registry.getObject(PropertyConduitSource.class, null);
    }

    @AfterAll
    static void cleanup()
    {
        registry.shutdown();

        registry = null;
        source = null;
    }

    @AfterEach
    void cleanupThread()
    {
        registry.cleanupThread();
    }

    @Test
    void literal_conduits_have_invariant_annotation()
    {
        PropertyConduit pc = source.create(CompositeBean.class, "12345");

        Invariant annotation = pc.getAnnotation(Invariant.class);

        assertNotNull(annotation);

        assertSame(Invariant.class, annotation.annotationType());
    }

    @Test
    void range_variable_to()
    {
        PropertyConduit pc = source.create(IntegerHolder.class, "10..value");
        IntegerHolder h = new IntegerHolder();

        h.setValue(5);

        IntegerRange ir = (IntegerRange) pc.get(h);

        assertEquals(new IntegerRange(10, 5), ir);
    }

    @Test
    void range_variable_from()
    {
        PropertyConduit pc = source.create(IntegerHolder.class, "value..99");
        IntegerHolder h = new IntegerHolder();

        h.setValue(72);

        IntegerRange ir = (IntegerRange) pc.get(h);

        assertEquals(new IntegerRange(72, 99), ir);
    }

    @Test
    void literal_conduits_are_not_updateable()
    {
        PropertyConduit pc = source.create(CompositeBean.class, "12345");
        CompositeBean bean = new CompositeBean();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pc.set(bean, 42));

        assertEquals("Literal values are not updateable.", ex.getMessage());
    }

    @Test
    void this_literal_conduit_is_not_updateable()
    {
        PropertyConduit normal = source.create(CompositeBean.class, "this");
        CompositeBean bean = new CompositeBean();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> normal.set(bean, 42));

        assertEquals("Literal values are not updateable.", ex.getMessage());
    }

    @Test
    void question_dot_operator_has_property_name_of_last_term()
    {
        InternalPropertyConduit normal = (InternalPropertyConduit) source.create(CompositeBean.class,
                "simple.firstName");
        InternalPropertyConduit smart = (InternalPropertyConduit) source.create(CompositeBean.class,
                "simple?.firstName");

        assertEquals("firstName", normal.getPropertyName());
        assertEquals("firstName", smart.getPropertyName());
    }

    @Test
    void dot_operator_read_throws_for_null_object()
    {
        PropertyConduit normal = source.create(CompositeBean.class, "simple.firstName");
        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        assertThrows(NullPointerException.class, () -> normal.get(bean));
    }

    @Test
    void question_dot_operator_read_yields_null_for_null_object()
    {
        PropertyConduit smart = source.create(CompositeBean.class, "simple?.firstName");
        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        assertNull(smart.get(bean));
    }

    @Test
    void dot_operator_write_throws_for_null_object()
    {
        PropertyConduit normal = source.create(CompositeBean.class, "simple.firstName");
        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        assertThrows(NullPointerException.class, () -> normal.set(bean, "Howard"));
    }

    @Test
    void question_dot_operator_write_is_a_no_op_for_null_object()
    {
        PropertyConduit smart = source.create(CompositeBean.class, "simple?.firstName");
        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        smart.set(bean, "Howard");

        assertNull(bean.getSimple());
    }

    static class GenericBean
    {
        public List<Date> dates;
        public List<GenericBean> genericBeans;

        public List<Long> getLongs()
        {
            return Collections.emptyList();
        }

        public void setMap(Map<String, Integer> map)
        {
        }
    }

    @Test
    void generic_types_are_determined()
    {
        PropertyConduit2 datesConduit = (PropertyConduit2) source.create(GenericBean.class, "dates");
        PropertyConduit2 longsConduit = (PropertyConduit2) source.create(GenericBean.class, "longs");
        PropertyConduit2 nestedDatesConduit = (PropertyConduit2) source.create(GenericBean.class, "genericBeans.get(0).dates");
        PropertyConduit2 mapConduit = (PropertyConduit2) source.create(GenericBean.class, "map");
        assertEquals("java.util.List<java.util.Date>", datesConduit.getPropertyGenericType().toString());
        assertEquals("java.util.List<java.lang.Long>", longsConduit.getPropertyGenericType().toString());
        assertEquals("java.util.List<java.util.Date>", nestedDatesConduit.getPropertyGenericType().toString());
        assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", mapConduit.getPropertyGenericType().toString());
    }

    @Test
    void method_names_are_matched_caselessly()
    {
        InternalPropertyConduit conduit = (InternalPropertyConduit) source.create(CompositeBean.class,
                "GETSIMPLE().firstName");

        assertEquals("firstName", conduit.getPropertyName());

        CompositeBean bean = new CompositeBean();
        SimpleBean inner = new SimpleBean();
        bean.setSimple(inner);

        conduit.set(bean, "Howard");

        assertEquals("Howard", inner.getFirstName());
    }

    @Test
    void generics()
    {
        String string = "surprise";
        StringHolder stringHolder = new StringHolder();
        stringHolder.put(string);
        StringHolderBean bean = new StringHolderBean();
        bean.setValue(stringHolder);

        PropertyConduit conduit = source.create(StringHolderBean.class, "value.get()");

        assertSame(string, conduit.get(bean));

        assertSame(String.class, conduit.getPropertyType());
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
    void generic_properties()
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
        assertSame(first, conduit.get(bean));
    }

    private static RealizedParameters realizedParameters(SimpleBean simple)
    {
        final RealizedParameters bean = new RealizedParameters();
        final Holder<SimpleBean> holder = new Holder<SimpleBean>();
        holder.put(simple);
        bean.setType1Property(holder);
        bean.setType2Property(1234L);
        bean.type1Field = holder;
        bean.type2Field = 5678L;
        bean.type2ArrayField = new Long[]
                {123L, 456L};
        return bean;
    }

    private static SimpleBean john()
    {
        final SimpleBean simple = new SimpleBean();
        simple.setAge(2);
        simple.setFirstName("John");
        simple.setLastName("Doe");
        return simple;
    }

    @Test
    void generic_parameterized_base_property_is_read()
    {
        SimpleBean simple = john();
        RealizedParameters bean = realizedParameters(simple);

        PropertyConduit conduit = source.create(RealizedParameters.class, "type1property.get().firstName");

        assertSame(simple.getFirstName(), conduit.get(bean));
    }

    @Test
    void generic_parameterized_base_property_is_written()
    {
        RealizedParameters bean = realizedParameters(john());
        PropertyConduit conduit = source.create(RealizedParameters.class, "type1property.get().firstName");

        conduit.set(bean, "Change");

        assertSame("Change", conduit.get(bean));
    }

    @Test
    void generic_parameterized_base_field_is_read()
    {
        SimpleBean simple = john();
        RealizedParameters bean = realizedParameters(simple);

        PropertyConduit conduit = source.create(RealizedParameters.class, "type1field.get().firstName");

        assertSame(simple.getFirstName(), conduit.get(bean));
    }

    @Test
    void generic_parameterized_base_type_variable_field_is_read()
    {
        RealizedParameters bean = realizedParameters(john());

        PropertyConduit conduit = source.create(RealizedParameters.class, "type2field");

        assertEquals(bean.type2Field, conduit.get(bean));
    }

    @Test
    void generic_parameterized_base_type_variable_property_is_read()
    {
        RealizedParameters bean = realizedParameters(john());

        PropertyConduit conduit = source.create(RealizedParameters.class, "type2property");

        assertEquals(bean.getType2Property(), conduit.get(bean));
    }

    @Test
    void generic_parameterized_base_type_variable_array_field_is_read()
    {
        RealizedParameters bean = realizedParameters(john());

        PropertyConduit conduit = source.create(RealizedParameters.class, "type2ArrayField");

        assertEquals(bean.type2ArrayField, conduit.get(bean));
    }

    @Test
    void generic_interface()
    {
        final WithRealizedGenericInterface bean = new WithRealizedGenericInterface();
        bean.a = "Hello";
        bean.b = 12345L;

        PropertyConduit conduit = source.create(WithRealizedGenericInterface.class, "genericA()");
        assertSame("Hello", conduit.get(bean));
        conduit = source.create(WithRealizedGenericInterface.class, "genericB()");
        assertEquals(12345L, conduit.get(bean));
    }

    @Test
    void generic_nested()
    {
        Three bean = new Three();
        bean.setA("hello");
        bean.setB(123L);
        bean.setB2(1235L);
        bean.setX(54321L);

        PropertyConduit conduit = source.create(Three.class, "a");
        assertSame("hello", conduit.get(bean));
    }

    @Test
    void null_root_object()
    {
        PropertyConduit conduit = source.create(StringHolderBean.class, "value.get()");

        NullPointerException ex = assertThrows(NullPointerException.class, () -> conduit.get(null));

        assertEquals("Root object of property expression 'value.get()' is null.", ex.getMessage());
    }

    @Test
    void null_property_in_chain()
    {
        PropertyConduit conduit = source.create(CompositeBean.class, "simple.lastName");

        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        NullPointerException ex = assertThrows(NullPointerException.class, () -> conduit.get(bean));

        assertTrue(ex.getMessage().contains("Property 'simple' (within property expression 'simple.lastName', of"),
                ex.getMessage());
        assertTrue(ex.getMessage().contains(") is null."), ex.getMessage());
    }

    @Test
    void last_term_may_be_null()
    {
        PropertyConduit conduit = source.create(CompositeBean.class, "simple.firstName");

        CompositeBean bean = new CompositeBean();

        bean.getSimple().setFirstName(null);

        assertNull(conduit.get(bean));
    }

    @Test
    void field_annotations_are_visible()
    {
        PropertyConduit conduit = source.create(CompositeBean.class, "simple.firstName");

        Validate annotation = conduit.getAnnotation(Validate.class);

        assertNotNull(annotation);

        assertEquals("required", annotation.value());
    }

    @Test
    void method_invocation_with_integer_arguments()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoInt(storedInt, 3)");
        EchoBean bean = new EchoBean();

        for (int i = 0; i < 10; i++)
        {
            bean.setStoredInt(i);
            assertEquals(Integer.valueOf(i * 3), conduit.get(bean));
        }
    }

    @Test
    void method_invocation_with_double_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoDouble(storedDouble, 2.0)");
        EchoBean bean = new EchoBean();

        double value = 22. / 7.;

        bean.setStoredDouble(value);

        assertEquals(Double.valueOf(2. * value), conduit.get(bean));
    }

    @Test
    void method_invocation_with_string_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoString(storedString, 'B4', 'AFTER')");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Moe");

        assertEquals("B4 - Moe - AFTER", conduit.get(bean));
    }

    @Test
    void method_invocation_using_dereference()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoString(storedString, stringSource.value, 'beta')");
        EchoBean bean = new EchoBean();

        StringSource source = new StringSource("alpha");

        bean.setStringSource(source);
        bean.setStoredString("Barney");

        assertEquals("alpha - Barney - beta", conduit.get(bean));
    }

    @Test
    void top_level_list()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "[ 1, 2.0, storedString ]");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Lisa");

        List l = (List) conduit.get(bean);

        assertEquals(Arrays.asList(Long.valueOf(1), Double.valueOf(2.0), "Lisa"), l);
    }

    @Test
    void empty_list()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "[  ]");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Lisa");

        List l = (List) conduit.get(bean);

        assertEquals(0, l.size());
    }

    @Test
    void list_as_method_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoList([ 1, 2.0, storedString ])");
        EchoBean bean = new EchoBean();

        bean.setStoredString("Bart");

        List l = (List) conduit.get(bean);

        assertEquals(Arrays.asList(Long.valueOf(1), Double.valueOf(2.0), "Bart"), l);
    }

    @Test
    void arrays_as_method_argument()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "echoArray(storedArray)");
        EchoBean bean = new EchoBean();

        bean.setStoredArray(new Number[][]
                {new Integer[]
                        {1, 2}, new Double[]
                        {3.0, 4.0}});

        Number[][] array = (Number[][]) conduit.get(bean);

        assertArrayEquals(new Number[]
                {1, 2}, array[0]);
        assertArrayEquals(new Number[]
                {3.0, 4.0}, array[1]);
    }

    @Test
    void top_level_map()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "{'one': true, 'two': 2.0, stringSource.value: 3, 'four': storedString}");
        EchoBean bean = new EchoBean();

        bean.setStoredString("four");
        bean.setStringSource(new StringSource("three"));

        Map actual = (Map) conduit.get(bean);
        assertEquals(true, actual.get("one"));
        assertEquals(2.0, actual.get("two"));
        assertEquals(3L, actual.get("three"));
        assertEquals("four", actual.get("four"));
    }

    @Test
    void empty_map()
    {
        PropertyConduit conduit = source.create(EchoBean.class, "{ }");
        EchoBean bean = new EchoBean();
        Map m = (Map) conduit.get(bean);

        assertTrue(m.isEmpty());
    }

    @Test
    void map_as_method_argument()
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
    void not_operator()
    {
        PropertyConduit conduit = source.create(IntegerHolder.class, "! value");
        IntegerHolder holder = new IntegerHolder();

        assertEquals(Boolean.TRUE, conduit.get(holder));

        holder.setValue(99);

        assertEquals(Boolean.FALSE, conduit.get(holder));
    }

    @Test
    void not_operator_in_subexpression()
    {
        PropertyConduit conduit = source.create(Switch.class, "label(! value)");

        Switch sw = new Switch();

        assertEquals("aye", conduit.get(sw));

        sw.setValue(true);

        assertEquals("nay", conduit.get(sw));
    }

    /**
     * TAP5-330
     */
    @Test
    void object_methods_can_be_invoked()
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

        assertEquals("Do You Grok Ze Block?", conduit.get(b));
    }

    @Test
    void parse_error_in_property_expression()
    {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> source.create(IntegerHolder.class, "getValue("));

        //note that addition of map support changed how this expression was parsed such that the error is now
        //reported at character 8, (, rather than 0: getValue(.
        assertEquals("Error parsing property expression 'getValue(': line 1:8 no viable alternative at input '('.",
                ex.getMessage());
    }

    @Test
    void lexer_error_in_property_expression()
    {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> source.create(IntegerHolder.class, "fred #"));

        assertEquals("Error parsing property expression 'fred #': Unable to parse input at character position 6.",
                ex.getMessage());
    }

    @Test
    void boolean_constant_as_method_parameter()
    {
        Bedrock bedrock = new Bedrock();

        PropertyConduit trueConduit = source.create(Bedrock.class, "toName(true)");
        PropertyConduit falseConduit = source.create(Bedrock.class, "toName(false)");

        assertEquals("Fred", trueConduit.get(bedrock));
        assertEquals("Barney", falseConduit.get(bedrock));
    }

    /**
     * TAP5-747
     */
    @Test
    void dereference_result_of_method_invocation()
    {
        ComplexObject co = new ComplexObject();
        PropertyConduit pc = source.create(ComplexObject.class, "get(nestedIndex).name");

        assertEquals("zero", pc.get(co));

        co.setNestedIndex(1);

        assertEquals("one", pc.get(co));
    }

    @Test
    void public_object_field()
    {
        PublicFieldBean bean = new PublicFieldBean();

        bean.stringField = "x";

        PropertyConduit pc = source.create(PublicFieldBean.class, "stringField");

        assertEquals("x", pc.get(bean));

        pc.set(bean, "y");

        assertEquals("y", bean.stringField);
    }

    @Test
    void navigate_through_public_field()
    {
        PublicFieldBean bean = new PublicFieldBean();
        PublicFieldBeanHolder holder = new PublicFieldBeanHolder(bean);

        bean.stringField = "x";

        PropertyConduit pc = source.create(PublicFieldBeanHolder.class, "bean.stringField");

        assertEquals("x", pc.get(holder));

        pc.set(holder, "y");

        assertEquals("y", bean.stringField);
    }

    @Test
    void public_primitive_field()
    {
        PublicFieldBean bean = new PublicFieldBean();

        bean.intField = 99;

        // check out the case insensitiveness:

        PropertyConduit pc = source.create(PublicFieldBean.class, "IntField");

        assertEquals(Integer.valueOf(99), pc.get(bean));

        pc.set(bean, 37);

        assertEquals(37, bean.intField);
    }

    @Test
    void annotation_of_public_field()
    {
        PropertyConduit pc = source.create(PublicFieldBean.class, "StringField");

        assertNotNull(pc.getAnnotation(NonVisual.class));
    }

    /**
     * TAP5-1555
     */
    @Test
    void this_and_null_inside_array()
    {
        PropertyConduit pc = source.create(NonVisualBean.class, "[this, null]");

        Object bean = new NonVisualBean();

        List list = (List) pc.get(bean);

        assertEquals(2, list.size());
        assertSame(bean, list.get(0));
        assertNull(list.get(1));
    }

    /**
     * TAP5-1673
     */
    @Test
    void public_static_fields_are_accessible()
    {
        PropertyConduit pc = source.create(PublicStaticFieldBean.class, "value");

        assertSame(PublicStaticFieldBean.VALUE, pc.get(null));

        pc.set(null, "new-value");

        assertEquals("new-value", PublicStaticFieldBean.VALUE);
    }

    @Test
    void final_static_fields_are_read_only()
    {
        PropertyConduit pc = source.create(PublicStaticFieldBean.class, "read_only");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pc.set(null, "new-value"));

        assertEquals(
                "Expression 'read_only' for class org.apache.tapestry5.internal.services.PublicStaticFieldBean is read-only.",
                ex.getMessage());
    }

    @Test
    void public_static_field_in_an_array()
    {
        PropertyConduit pc = source.create(PublicStaticFieldBean.class, "[read_only]");

        // Need to instantiate it, or exception "Root object of property expression is null"

        List<String> actual = (List<String>) pc.get(new PublicStaticFieldBean());

        assertEquals(Collections.singletonList(PublicStaticFieldBean.READ_ONLY), actual);
    }

    // TAP5-1493
    @Test
    void covariant_property_return_type()
    {
        // example from Howard
        assertSame(Bar.class, source.create(Foo.class, "bar").getPropertyType());
    }

    // TAP5-1493
    @Test
    void abstract_property_return_type()
    {
        assertSame(AbstractBar.class, source.create(AbstractFoo.class, "bar").getPropertyType());
    }

    // TAP5-1493
    @Test
    void covariant_property_return_type_through_interfaces()
    {
        // example from Robert
        assertSame(int.class, source.create(RobertMyClass.class, "foo.robertBarValue").getPropertyType());
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
}
