package org.apache.tapestry5.ioc.test.internal;

import java.io.File;

import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.IntermediateType;

public class NonAnnotatedGenericSetServiceImpl implements NonAnnotatedGenericSetServiceInterface
{
    @Override
    @Advise(id = "id", serviceInterface = NonAnnotatedServiceInterface.class)
    public String execute1(@IntermediateType(String.class)
    final int i)
    {
        return null;
    }

    @Override
    @Advise(id = "id", serviceInterface = NonAnnotatedServiceInterface.class)
    public String execute2(@IntermediateType(String.class)
    final String t)
    {
        return null;
    }

    @Advise(id = "file", serviceInterface = NonAnnotatedServiceInterface.class)
    @Override
    public String execute2(File t)
    {
        return null;
    }

    @Advise(id = "int", serviceInterface = NonAnnotatedServiceInterface.class)
    @Override
    public String execute2(int t)
    {
        return null;
    }

    @Override
    @Advise(id = "id", serviceInterface = NonAnnotatedServiceInterface.class)
    public String execute3(@IntermediateType(String.class) int i)
    {
        return null;
    }

    @Override
    @Advise(id = "id", serviceInterface = NonAnnotatedServiceInterface.class)
    public String execute2(@IntermediateType(String.class) String t, String s)
    {
        return null;
    }

    public static void main(String[] args) throws NoSuchMethodException, SecurityException
    {
        System.out.println(Object.class.isAssignableFrom(String.class));
    }

}
