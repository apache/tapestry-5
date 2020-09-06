package org.apache.tapestry5.ioc.test.internal.services;

public class ShadowedPublicFieldBean
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
