package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.ioc.ServiceBinder;

public class ConventionModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(StringHolder.class);
    }
}
