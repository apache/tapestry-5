package org.apache.tapestry5.ioc;

public class ConventionModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(StringHolder.class);
    }
}
