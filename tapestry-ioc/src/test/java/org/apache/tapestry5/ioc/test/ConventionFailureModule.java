package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.ioc.ServiceBinder;

public class ConventionFailureModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(Pingable.class);
    }
}
