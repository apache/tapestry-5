package org.apache.tapestry5.ioc.test;


public interface InterfaceWithDefaultMethod 
{

    public static final String STATIC_METHOD_RETURN_VALUE = "whatever";

    static String staticMethod() 
    {
        return STATIC_METHOD_RETURN_VALUE;
    }

    String method();
} 

