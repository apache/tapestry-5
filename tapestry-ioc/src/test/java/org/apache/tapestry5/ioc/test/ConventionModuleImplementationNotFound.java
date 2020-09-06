package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.ioc.ServiceBinder;

public class ConventionModuleImplementationNotFound
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(StringTransformer.class);
    }
}
