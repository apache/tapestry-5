package org.apache.tapestry5.ioc.test.internal.services;

import org.apache.tapestry5.beaneditor.DataType;
import org.apache.tapestry5.beaneditor.Validate;

public class Bean
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
        return "PropertyAccessImplSpecBean";
    }

    public void setWriteOnly(boolean b)
    {
    }

    public String getReadOnly()
    {
        return null;
    }
}
