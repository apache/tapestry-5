package org.apache.tapestry5.ioc.test;

public class InterfaceWithDefaultMethodImpl implements InterfaceWithDefaultMethod 
{
    @Override
    public String method() 
    {
        return "whatever";
    }

} 
