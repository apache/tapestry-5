package org.apache.tapestry5.ioc.test.internal;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.spockframework.util.Assert;

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
        assertSame(FieService.class, serviceInterface);
        assertSame(expectedDelegate, delegate);

        return serviceInterface.cast(interceptorToReturn);
    }

    public Object decoratorUntyped(Object delegate)
    {
        assertSame(expectedDelegate, delegate);

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
