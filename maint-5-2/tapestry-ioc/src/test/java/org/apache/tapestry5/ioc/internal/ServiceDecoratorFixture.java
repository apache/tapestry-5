// Copyright 2006 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal;

import org.testng.Assert;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.ServiceDecoratorImplTest}.
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
