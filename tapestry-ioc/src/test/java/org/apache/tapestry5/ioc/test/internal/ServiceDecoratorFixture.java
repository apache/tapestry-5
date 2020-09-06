package org.apache.tapestry5.ioc.test.internal;

import org.testng.Assert;

/**
 * Used by {@link org.apache.tapestry5.ioc.test.internal.services.ServiceDecoratorImplSpec}.
 */
public class ServiceDecoratorFixture extends Assert
{
    Object expectedDelegate;

    Object interceptorToReturn;

    RuntimeException exception;

    public <T> T decoratorReturnsInterceptor(Class<T> serviceInterface, T delegate)
    {
        assertSame(serviceInterface, FieService.class);
        assertSame(delegate, expectedDelegate);

        return serviceInterface.cast(interceptorToReturn);
    }

    public Object decoratorUntyped(Object delegate)
    {
        assertSame(delegate, expectedDelegate);

        return interceptorToReturn;
    }

    public Object decoratorThrowsException(Object delegate)
    {
        throw exception;
    }

    public Object decorateReturnNull(Object delegate)
    {
        return null;
    }
}
