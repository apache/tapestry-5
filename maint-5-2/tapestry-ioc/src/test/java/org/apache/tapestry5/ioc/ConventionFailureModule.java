package org.apache.tapestry5.ioc;

public class ConventionFailureModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(Pingable.class);
    }
}
