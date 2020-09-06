// Copyright 2011-2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.Invokable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Wraps the invocation of a constructor (with exception reporting) as an {@link Invokable}.
 *
 * @since 5.3
 */
public class ConstructorInvoker<T> implements Invokable<T>
{
    private final Constructor<T> constructor;

    private final ObjectCreator[] constructorParameters;

    public ConstructorInvoker(Constructor constructor, ObjectCreator[] constructorParameters)
    {
        this.constructor = constructor;
        this.constructorParameters = constructorParameters;
    }

    @Override
    public T invoke()
    {
        Throwable fail;

        Object[] realized = InternalUtils.realizeObjects(constructorParameters);

        try
        {
            return constructor.newInstance(realized);
        } catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        } catch (Exception ex)
        {
            fail = ex;
        }

        throw new RuntimeException(String.format("Error invoking constructor %s: %s",
                constructor, ExceptionUtils.toMessage(fail)), fail);
    }
}
