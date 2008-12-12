// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * A representation of a {@link java.lang.reflect.Method}, identifying the name, return type, parameter types and
 * exception types. Actual Method objects are tied to a particular class, and don't compare well with other otherwise
 * identical Methods from other classes or interface; MethodSignatures are distinct from classes and compare well.
 * <p/>
 * Because the intended purpose is to compare methods from interfaces (which are always public and abstract) we don't
 * bother to actually track the modifiers. In addition, at this time, MethodSignature <em>does not distinguish between
 * instance and static methods</em>.
 * <p/>
 * This version of MethodSignature works with <em>loaded</em> classes, and it usually used in the context of {@link
 * org.apache.tapestry5.ioc.services.ClassFab} to create new classes and subclasses.
 */
public class MethodSignature
{
    private int hashCode = -1;

    private final Class returnType;

    private final String name;

    private final Class[] parameterTypes;

    private final Class[] exceptionTypes;

    public MethodSignature(Class returnType, String name, Class[] parameterTypes, Class[] exceptionTypes)
    {
        this.returnType = Defense.notNull(returnType, "returnType");
        this.name = Defense.notBlank(name, "name");

        // Can be null!
        this.parameterTypes = parameterTypes;
        this.exceptionTypes = exceptionTypes;
    }

    public MethodSignature(Method m)
    {
        this(m.getReturnType(), m.getName(), m.getParameterTypes(), m.getExceptionTypes());
    }

    /**
     * Returns the exceptions for this method. Caution: do not modify the returned array. May return null.
     */
    public Class[] getExceptionTypes()
    {
        return exceptionTypes;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Returns the parameter types for this method. May return null. Caution: do not modify the returned array.
     */
    public Class[] getParameterTypes()
    {
        return parameterTypes;
    }

    public Class getReturnType()
    {
        return returnType;
    }

    @Override
    public int hashCode()
    {
        if (hashCode == -1)
        {

            hashCode = returnType.hashCode();

            hashCode = 31 * hashCode + name.hashCode();

            int count = InternalUtils.size(parameterTypes);

            for (int i = 0; i < count; i++)
                hashCode = 31 * hashCode + parameterTypes[i].hashCode();

            count = InternalUtils.size(exceptionTypes);

            for (int i = 0; i < count; i++)
                hashCode = 31 * hashCode + exceptionTypes[i].hashCode();
        }

        return hashCode;
    }

    /**
     * Returns true if the other object is an instance of MethodSignature with <em>identical</em> values for return
     * type, name, parameter types and exception types.
     *
     * @see #isOverridingSignatureOf(MethodSignature)
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof MethodSignature)) return false;

        MethodSignature ms = (MethodSignature) o;

        if (returnType != ms.returnType) return false;

        if (!name.equals(ms.name)) return false;

        if (mismatch(parameterTypes, ms.parameterTypes)) return false;

        return !mismatch(exceptionTypes, ms.exceptionTypes);
    }

    private boolean mismatch(Class[] a1, Class[] a2)
    {
        int a1Count = InternalUtils.size(a1);
        int a2Count = InternalUtils.size(a2);

        if (a1Count != a2Count) return true;

        // Hm. What if order is important (for exceptions)? We're really saying here that they
        // were derived from the name Method.

        for (int i = 0; i < a1Count; i++)
        {
            if (a1[i] != a2[i]) return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(ClassFabUtils.toJavaClassName(returnType));
        buffer.append(" ");
        buffer.append(name);
        buffer.append("(");

        for (int i = 0; i < InternalUtils.size(parameterTypes); i++)
        {
            if (i > 0) buffer.append(", ");

            buffer.append(ClassFabUtils.toJavaClassName(parameterTypes[i]));
        }

        buffer.append(")");

        int _exceptionCount = InternalUtils.size(exceptionTypes);
        String _exceptionNames[] = new String[_exceptionCount];
        for (int i = 0; i < _exceptionCount; i++)
        {
            _exceptionNames[i] = exceptionTypes[i].getName();
        }

        Arrays.sort(_exceptionNames);

        for (int i = 0; i < _exceptionCount; i++)
        {
            if (i == 0) buffer.append(" throws ");
            else buffer.append(", ");

            buffer.append(_exceptionNames[i]);
        }

        return buffer.toString();
    }

    /**
     * Returns a string consisting of the name of the method and its parameter types. This is similar to {@link
     * #toString()}, but omits the return type and information about thrown exceptions. A unique id is used by {@link
     * MethodIterator} to identify overlapping methods (methods with the same name and parameter types but with
     * different thrown exceptions).
     *
     * @see #isOverridingSignatureOf(MethodSignature)
     */
    public String getUniqueId()
    {
        StringBuilder buffer = new StringBuilder(name);
        buffer.append("(");

        for (int i = 0; i < InternalUtils.size(parameterTypes); i++)
        {
            if (i > 0) buffer.append(",");

            buffer.append(ClassFabUtils.toJavaClassName(parameterTypes[i]));
        }

        buffer.append(")");

        return buffer.toString();
    }

    /**
     * Returns true if this signature has the same return type, name and parameters types as the method signature passed
     * in, and this signature's exceptions "trump" (are the same as, or super-implementations of, all exceptions thrown
     * by the other method signature).
     */

    public boolean isOverridingSignatureOf(MethodSignature ms)
    {
        if (returnType != ms.returnType) return false;

        if (!name.equals(ms.name)) return false;

        if (mismatch(parameterTypes, ms.parameterTypes)) return false;

        return exceptionsEncompass(ms.exceptionTypes);
    }

    /**
     * The nuts and bolts of checking that another method signature's exceptions are a subset of this signature's.
     */

    @SuppressWarnings("unchecked")
    private boolean exceptionsEncompass(Class[] otherExceptions)
    {
        int ourCount = InternalUtils.size(exceptionTypes);
        int otherCount = InternalUtils.size(otherExceptions);

        // If we have no exceptions, then ours encompass theirs only if they
        // have no exceptions, either.

        if (ourCount == 0) return otherCount == 0;

        boolean[] matched = new boolean[otherCount];
        int unmatched = otherCount;

        for (int i = 0; i < ourCount && unmatched > 0; i++)
        {
            for (int j = 0; j < otherCount; j++)
            {
                // Ignore exceptions that have already been matched

                if (matched[j]) continue;

                // When one of our exceptions is a super-class of one of their exceptions,
                // then their exceptions is matched.

                if (exceptionTypes[i].isAssignableFrom(otherExceptions[j]))
                {
                    matched[j] = true;
                    unmatched--;
                }
            }
        }

        return unmatched == 0;
    }
}
