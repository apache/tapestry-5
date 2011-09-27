// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.beaneditor.DataType;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.internal.util.Pair;
import org.apache.tapestry5.ioc.internal.util.StringLongPair;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.testng.annotations.Test;

import java.awt.*;
import java.beans.*;
import java.util.Arrays;
import java.util.Random;

public class PropertyAccessImplTest extends IOCInternalTestCase
{
    private static final String CLASS_NAME = PropertyAccessImplTest.class.getName();

    private PropertyAccess access = new PropertyAccessImpl();

    private Random random = new Random();

    public static class Bean
    {
        public static final Double PI = 3.14;

        @DataType("fred")
        @Validate("field-value-overridden")
        private int value;

        @Validate("getter-value-overrides")
        public int getValue()
        {
            return value;
        }

        public void setValue(int value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return "PropertyUtilsTestBean";
        }

        public void setWriteOnly(boolean b)
        {
        }

        public String getReadOnly()
        {
            return null;
        }
    }

    public static class BeanSubclass extends Bean
    {

    }

    public static class ExceptionBean
    {
        public boolean getFailure()
        {
            throw new RuntimeException("getFailure");
        }

        public void setFailure(boolean b)
        {
            throw new RuntimeException("setFailure");
        }

        @Override
        public String toString()
        {
            return "PropertyUtilsExceptionBean";
        }
    }

    public static class UglyBean
    {
    }

    public static class UglyBeanBeanInfo implements BeanInfo
    {

        public BeanInfo[] getAdditionalBeanInfo()
        {
            return new BeanInfo[0];
        }

        public BeanDescriptor getBeanDescriptor()
        {
            return null;
        }

        public int getDefaultEventIndex()
        {
            return 0;
        }

        public int getDefaultPropertyIndex()
        {
            return 0;
        }

        public EventSetDescriptor[] getEventSetDescriptors()
        {
            return new EventSetDescriptor[0];
        }

        public Image getIcon(int iconKind)
        {
            return null;
        }

        public MethodDescriptor[] getMethodDescriptors()
        {
            return new MethodDescriptor[0];
        }

        public PropertyDescriptor[] getPropertyDescriptors()
        {
            throw new RuntimeException("This is the UglyBean.");
        }

    }

    public static class ScalaBean
    {
        private String value;

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

        public String value()
        {
            return value;
        }

        public void value_$eq(String value)
        {
            this.value = value;
        }
    }

    public static class ScalaClass
    {
        private String value;

        public String value()
        {
            return value;
        }

        public void value_$eq(String value)
        {
            this.value = value;
        }
    }

    public static class BooleanHolder
    {
        private boolean flag;

        public boolean isFlag()
        {
            return flag;
        }

        public void setFlag(boolean flag)
        {
            this.flag = flag;
        }
    }

    public static class PublicFieldBean
    {
        public String value;
    }

    public static class ShadowedPublicFieldBean
    {
        private String _value;

        public String value;

        public String getValue()
        {
            return _value;
        }

        public void setValue(String value)
        {
            _value = value;
        }
    }

    public static abstract class GenericBean<T>
    {
        public T value;
    }

    public static class GenericStringBean extends GenericBean<String>
    {
    }

    public static interface BeanInterface
    {
        String getValue();

        void setValue(String v);

        String getOtherValue();

        void setOtherValue(String v);

        int getIntValue(); // read-only
    }

    public static abstract class AbstractBean implements BeanInterface
    {
        // abstract class implements method from interface
        private String other;

        public String getOtherValue()
        {
            return other;
        }

        public void setOtherValue(String v)
        {
            other = v;
        }
    }

    public static class ConcreteBean extends AbstractBean
    {
        private String value;
        private int intValue;

        public ConcreteBean(int intValue)
        {
            this.intValue = intValue;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String v)
        {
            value = v;
        }

        public int getIntValue()
        {
            return intValue;
        }
    }

    @Test
    public void simple_read_access()
    {
        Bean b = new Bean();

        int value = random.nextInt();

        b.setValue(value);

        assertEquals(access.get(b, "value"), value);
    }

    @Test
    public void property_name_case_is_ignored_on_read()
    {
        Bean b = new Bean();

        int value = random.nextInt();

        b.setValue(value);

        assertEquals(access.get(b, "VALUE"), value);
    }

    @Test
    public void simple_write_access()
    {
        Bean b = new Bean();

        int value = random.nextInt();

        access.set(b, "value", value);

        assertEquals(b.getValue(), value);
    }

    @Test
    public void property_name_case_is_ignored_on_write()
    {
        Bean b = new Bean();

        int value = random.nextInt();

        access.set(b, "VALUE", value);

        assertEquals(b.getValue(), value);
    }

    @Test
    public void missing_property()
    {
        Bean b = new Bean();

        try
        {
            access.get(b, "zaphod");

            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Class " + CLASS_NAME + "$Bean does not "
                    + "contain a property named 'zaphod'.");
        }
    }

    @Test
    public void attempt_to_update_read_only_property()
    {
        Bean b = new Bean();

        try
        {
            access.set(b, "class", null);
            unreachable();
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(ex.getMessage(), "Class " + CLASS_NAME
                    + "$Bean does not provide an mutator ('setter') method for property 'class'.");
        }
    }

    @Test
    public void attempt_to_read_from_write_only_property()
    {
        Bean b = new Bean();

        try
        {
            access.get(b, "writeOnly");
            unreachable();
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(ex.getMessage(), "Class " + CLASS_NAME
                    + "$Bean does not provide an accessor ('getter') method for property 'writeOnly'.");
        }
    }

    @Test
    public void exception_thrown_inside_getter()
    {
        ExceptionBean b = new ExceptionBean();

        try
        {
            access.get(b, "failure");
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Error reading property 'failure' of PropertyUtilsExceptionBean: getFailure");
        }
    }

    @Test
    public void exception_thrown_inside_setter()
    {
        ExceptionBean b = new ExceptionBean();

        try
        {
            access.set(b, "failure", false);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Error updating property 'failure' of PropertyUtilsExceptionBean: setFailure");
        }
    }

    @Test
    public void failure_when_introspecting_class()
    {
        UglyBean b = new UglyBean();

        try
        {
            access.get(b, "google");
            unreachable();
        } catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "java.lang.RuntimeException: This is the UglyBean.");
        }
    }

    @Test
    public void clear_wipes_internal_cache()
    {
        ClassPropertyAdapter cpa1 = access.getAdapter(Bean.class);

        assertSame(cpa1.getBeanType(), Bean.class);

        ClassPropertyAdapter cpa2 = access.getAdapter(Bean.class);

        assertSame(cpa2, cpa1);

        access.clearCache();

        ClassPropertyAdapter cpa3 = access.getAdapter(Bean.class);

        assertNotSame(cpa3, cpa1);
    }

    @Test
    public void class_property_adapter_toString()
    {
        ClassPropertyAdapter cpa = access.getAdapter(Bean.class);

        assertEquals(cpa.toString(), "<ClassPropertyAdaptor " + CLASS_NAME
                + "$Bean : PI, class, readOnly, value, writeOnly>");
    }

    @Test
    public void property_adapter_read_only_property()
    {
        ClassPropertyAdapter cpa = access.getAdapter(Bean.class);
        PropertyAdapter pa = cpa.getPropertyAdapter("readOnly");

        assertTrue(pa.isRead());
        assertFalse(pa.isUpdate());
        assertFalse(pa.isCastRequired());

        assertNull(pa.getWriteMethod());
        assertEquals(pa.getReadMethod(), findMethod(Bean.class, "getReadOnly"));
    }

    @Test
    public void property_adapter_write_only_property()
    {
        ClassPropertyAdapter cpa = access.getAdapter(Bean.class);
        PropertyAdapter pa = cpa.getPropertyAdapter("writeOnly");

        assertFalse(pa.isRead());
        assertTrue(pa.isUpdate());

        assertEquals(pa.getWriteMethod(), findMethod(Bean.class, "setWriteOnly"));
        assertNull(pa.getReadMethod());
    }

    @Test
    public void class_property_adapter_returns_null_for_unknown_property()
    {
        ClassPropertyAdapter cpa = access.getAdapter(Bean.class);

        assertNull(cpa.getPropertyAdapter("google"));
    }

    @Test
    public void access_to_property_type()
    {
        ClassPropertyAdapter cpa = access.getAdapter(Bean.class);

        assertEquals(cpa.getPropertyAdapter("value").getType(), int.class);
        assertEquals(cpa.getPropertyAdapter("readOnly").getType(), String.class);
        assertEquals(cpa.getPropertyAdapter("writeOnly").getType(), boolean.class);
    }

    @Test
    public void property_names()
    {
        ClassPropertyAdapter cpa = access.getAdapter(Bean.class);

        assertEquals(cpa.getPropertyNames(), Arrays.asList("PI", "class", "readOnly", "value", "writeOnly"));
    }

    @Test
    public void read_static_field()
    {
        PropertyAdapter adapter = access.getAdapter(Bean.class).getPropertyAdapter("pi");

        assertSame(adapter.get(null), Bean.PI);
    }

    @Test
    public void static_final_fields_may_not_be_changed()
    {
        PropertyAdapter adapter = access.getAdapter(Bean.class).getPropertyAdapter("pi");

        try
        {
            adapter.set(null, 3.0d);
            unreachable();
        } catch (RuntimeException ex)
        {
            // The exact text varies from JDK to JDK
            assertMessageContains(ex, "final", "PI");
        }
    }

    @Test
    public void integration()
    {
        Registry registry = buildRegistry();

        PropertyAccess pa = registry.getService("PropertyAccess", PropertyAccess.class);

        Bean b = new Bean();

        int value = random.nextInt();

        pa.set(b, "value", value);

        assertEquals(b.getValue(), value);

        registry.shutdown();
    }

    @Test
    public void super_interface_methods_inherited_by_sub_interface()
    {
        ClassPropertyAdapter cpa = access.getAdapter(SubInterface.class);

        assertEquals(cpa.getPropertyNames(), Arrays.asList("grandParentProperty", "parentProperty", "subProperty"));
    }

    @Test
    public void indexed_properties_are_ignored()
    {
        ClassPropertyAdapter cpa = access.getAdapter(BeanWithIndexedProperty.class);

        assertEquals(cpa.getPropertyNames(), Arrays.asList("class", "primitiveProperty"));
    }

    @Test
    public void get_annotation_when_annotation_not_present()
    {
        PropertyAdapter pa = access.getAdapter(AnnotatedBean.class).getPropertyAdapter("readWrite");

        assertNull(pa.getAnnotation(Scope.class));
    }

    @Test
    public void get_annotation_with_annotation_on_write_method()
    {
        PropertyAdapter pa = access.getAdapter(AnnotatedBean.class).getPropertyAdapter("annotationOnWrite");

        Scope annotation = pa.getAnnotation(Scope.class);
        assertNotNull(annotation);

        assertEquals(annotation.value(), "onwrite");
    }

    @Test
    public void read_method_annotation_overrides_write_method_annotation()
    {
        PropertyAdapter pa = access.getAdapter(AnnotatedBean.class).getPropertyAdapter("annotationOnRead");

        Scope annotation = pa.getAnnotation(Scope.class);
        assertNotNull(annotation);

        assertEquals(annotation.value(), "onread");
    }

    @Test
    public void no_write_method_reading_missing_annotation()
    {
        PropertyAdapter pa = access.getAdapter(AnnotatedBean.class).getPropertyAdapter("readOnly");

        assertNull(pa.getAnnotation(Scope.class));
    }

    @Test
    public void get_annotation_will_read_field()
    {
        PropertyAdapter pa = access.getAdapter(Bean.class).getPropertyAdapter("value");

        DataType dt = pa.getAnnotation(DataType.class);

        assertNotNull(dt);
        assertEquals(dt.value(), "fred");
    }

    /**
     * TAPESTRY-2448
     */
    @Test
    public void get_annotation_will_read_inherited_field()
    {
        PropertyAdapter pa = access.getAdapter(BeanSubclass.class).getPropertyAdapter("value");

        DataType dt = pa.getAnnotation(DataType.class);

        assertNotNull(dt);
        assertEquals(dt.value(), "fred");

    }

    @Test
    public void field_annotation_overridden_by_getter_annotation()
    {
        PropertyAdapter pa = access.getAdapter(Bean.class).getPropertyAdapter("value");

        assertEquals(pa.getAnnotation(Validate.class).value(), "getter-value-overrides");
    }

    @Test
    public void using_generics()
    {
        ClassPropertyAdapter cpa1 = access.getAdapter(StringLongPair.class);

        PropertyAdapter pa1 = cpa1.getPropertyAdapter("key");
        assertSame(pa1.getType(), String.class);
        assertTrue(pa1.isCastRequired());

        assertSame(pa1.getDeclaringClass(), Pair.class);

        PropertyAdapter pa2 = cpa1.getPropertyAdapter("value");
        assertSame(pa2.getType(), Long.class);
        assertTrue(pa2.isCastRequired());

        // On the base class, which defines the generic parameter type variables,
        // the properties just look like Object.

        ClassPropertyAdapter cpa2 = access.getAdapter(Pair.class);

        pa1 = cpa2.getPropertyAdapter("key");
        assertSame(pa1.getType(), Object.class);
        assertFalse(pa1.isCastRequired());

        pa2 = cpa2.getPropertyAdapter("value");
        assertSame(pa2.getType(), Object.class);
        assertFalse(pa2.isCastRequired());

    }

    @Test
    public void get_scala_properties_with_bean_accessors()
    {
        PropertyAdapter pa = access.getAdapter(ScalaBean.class).getPropertyAdapter("value");

        // even thought scala accessors are present the java bean ones should be the ones used by Tapestry
        assertEquals(pa.getReadMethod().getName(), "getValue");
        assertEquals(pa.getWriteMethod().getName(), "setValue");
    }

    @Test
    public void get_scala_properties()
    {
        PropertyAdapter pa = access.getAdapter(ScalaClass.class).getPropertyAdapter("value");

        assertEquals(pa.getReadMethod().getName(), "value");
        assertEquals(pa.getWriteMethod().getName(), "value_$eq");
    }

    @Test
    public void access_to_public_field()
    {
        PropertyAdapter pa = access.getAdapter(PublicFieldBean.class).getPropertyAdapter("value");

        assertTrue(pa.isField());
        assertTrue(pa.isRead());
        assertTrue(pa.isUpdate());

        PublicFieldBean bean = new PublicFieldBean();

        pa.set(bean, "fred");

        assertEquals(bean.value, "fred");

        bean.value = "barney";

        assertEquals(pa.get(bean), "barney");
    }

    @Test
    public void property_is_favored_over_public_field()
    {
        PropertyAdapter pa = access.getAdapter(ShadowedPublicFieldBean.class).getPropertyAdapter("value");

        assertFalse(pa.isField());

        ShadowedPublicFieldBean bean = new ShadowedPublicFieldBean();

        pa.set(bean, "fred");

        assertNull(bean.value);

        bean.value = "barney";
        bean.setValue("wilma");

        assertEquals(pa.get(bean), "wilma");
    }

    @Test
    public void access_property_from_unimplemented_interface_in_abstract_base_class()
    {
        AbstractBean bean = new ConcreteBean(33);

        PropertyAdapter valueAdapter = access.getAdapter(AbstractBean.class).getPropertyAdapter("value");

        assertNotNull(valueAdapter);
        assertFalse(valueAdapter.isField());

        valueAdapter.set(bean, "Hello");

        assertSame(valueAdapter.get(bean), "Hello");
        assertSame(bean.getValue(), "Hello");

        PropertyAdapter otherValueAdapter = access.getAdapter(AbstractBean.class).getPropertyAdapter("otherValue");

        assertNotNull(otherValueAdapter);
        assertFalse(otherValueAdapter.isField());

        otherValueAdapter.set(bean, "Other Value");

        assertSame(otherValueAdapter.get(bean), "Other Value");
        assertSame(bean.getOtherValue(), "Other Value");

        PropertyAdapter intValueAdapter = access.getAdapter(AbstractBean.class).getPropertyAdapter("intvalue");
        assertNotNull(intValueAdapter);

        assertEquals(intValueAdapter.get(bean), 33);

        assertTrue(intValueAdapter.isRead());
        assertFalse(intValueAdapter.isUpdate());
    }


    @Test
    public void generic_field_is_recognized()
    {
        PropertyAdapter pa = access.getAdapter(GenericStringBean.class).getPropertyAdapter("value");

        assertTrue(pa.isCastRequired());
        assertEquals(pa.getType(), String.class);
        assertSame(pa.getDeclaringClass(), GenericBean.class);
    }
}
