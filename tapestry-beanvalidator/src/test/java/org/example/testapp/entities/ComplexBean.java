package org.example.testapp.entities;

import javax.validation.constraints.NotNull;

public class ComplexBean
{

    private SomeSimpleBean someSimpleBean;
    private SomeOtherSimpleBean someOtherSimpleBean;

    @NotNull
    private String simpleNotNullProperty;

    public SomeSimpleBean getSomeSimpleBean()
    {
        return someSimpleBean;
    }

    public void setSomeSimpleBean(SomeSimpleBean someSimpleBean)
    {
        this.someSimpleBean = someSimpleBean;
    }

    public SomeOtherSimpleBean getSomeOtherSimpleBean()
    {
        return someOtherSimpleBean;
    }

    public void setSomeOtherSimpleBean(SomeOtherSimpleBean someOtherSimpleBean)
    {
        this.someOtherSimpleBean = someOtherSimpleBean;
    }

    public String getSimpleNotNullProperty()
    {
        return simpleNotNullProperty;
    }

    public void setSimpleNotNullProperty(String simpleNotNullProperty)
    {
        this.simpleNotNullProperty = simpleNotNullProperty;
    }

}
