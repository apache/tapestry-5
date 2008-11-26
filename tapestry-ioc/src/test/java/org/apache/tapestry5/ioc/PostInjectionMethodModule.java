package org.apache.tapestry5.ioc;

public class PostInjectionMethodModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(Greeter.class, ServiceIdGreeter.class).withId("ServiceIdGreeter");
    }
}
