// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodDescription;

import java.lang.reflect.Method;

/**
 * Bundles together the fixed (same for all instances) information needed by a {@link MethodInvocationBundle}.
 */
public class MethodInvocationBundle
{
    public final String className;

    public final MethodDescription methodDescription;

    public final MethodAdvice[] advice;

    private volatile Method method;

    public MethodInvocationBundle(String className, MethodDescription methodDescription, MethodAdvice[] advice)
    {
        this.className = className;
        this.methodDescription = methodDescription;
        this.advice = advice;
    }

    public Method getMethod(Object instance)
    {
        if (method == null)
            method = findMethod(instance.getClass().getClassLoader());

        return method;
    }

    @SuppressWarnings("unchecked")
    private Method findMethod(ClassLoader loader)
    {
        try
        {
            Class[] types = new Class[methodDescription.argumentTypes.length];

            for (int i = 0; i < types.length; i++)
            {
                types[i] = PlasticInternalUtils.toClass(loader, methodDescription.argumentTypes[i]);
            }

            Class clazz = PlasticInternalUtils.toClass(loader, className);

            return clazz.getDeclaredMethod(methodDescription.methodName, types);
        } catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to locate Method %s: %s", methodDescription,
                    PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

}
