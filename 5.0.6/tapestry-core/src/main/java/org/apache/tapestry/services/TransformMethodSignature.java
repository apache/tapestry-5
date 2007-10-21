// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import java.lang.reflect.Modifier;

/**
 * A representation of a method signature, which consists of its name, modifiers (primarily,
 * visibility), return type, parameter types, and declared exception types.
 * <p>
 * Types are stored as class names (or primitive names) because the signature is used with
 * {@link ClassTransformation} (which operates on as-yet unloaded classes).
 */
public class TransformMethodSignature implements Comparable<TransformMethodSignature>
{
    private int _hashCode = -1;

    private final int _modifiers;

    private final String _returnType;

    private final String _methodName;

    private final String[] _parameterTypes;

    private final String[] _exceptionTypes;

    private static final String[] EMPTY_STRINGS = new String[0];

    /** Convenience for adding a public void method with no parameters or exception types. */

    public TransformMethodSignature(String name)
    {
        this(Modifier.PUBLIC, "void", name, EMPTY_STRINGS, EMPTY_STRINGS);
    }

    public TransformMethodSignature(int modifiers, String type, String name,
            String[] parameterTypes, String[] exceptionTypes)
    {
        _modifiers = modifiers;

        _returnType = notBlank(type, "type");
        _methodName = notBlank(name, "name");

        // TODO: Checks that no element within the two arrays
        // is null or blank.

        _parameterTypes = typeNamesOrEmpty(parameterTypes);
        _exceptionTypes = typeNamesOrEmpty(exceptionTypes);
    }

    private String[] typeNamesOrEmpty(String[] types)
    {
        return types == null ? EMPTY_STRINGS : types;
    }

    /**
     * Returns a non-null array of the names of each declared exception type thrown by the method.
     * Calling code should not modify the array.
     */
    public String[] getExceptionTypes()
    {
        return _exceptionTypes;
    }

    /** Returns the name of the method. */
    public String getMethodName()
    {
        return _methodName;
    }

    /**
     * Returns the set of modifier flags for this method.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers()
    {
        return _modifiers;
    }

    /**
     * Returns an array of the type name for each parameter. Calling code should not modify the
     * array.
     */
    public String[] getParameterTypes()
    {
        return _parameterTypes;
    }

    /** Return the type name of the return type of the method. */
    public String getReturnType()
    {
        return _returnType;
    }

    @Override
    public int hashCode()
    {
        if (_hashCode == -1)
        {
            _hashCode = 17 * _modifiers;
            _hashCode += 31 * _returnType.hashCode();
            _hashCode += 31 * _methodName.hashCode();

            for (String parameterType : _parameterTypes)
            {
                _hashCode += 31 * parameterType.hashCode();
            }

            for (String exceptionType : _exceptionTypes)
            {
                _hashCode += 31 * exceptionType.hashCode();
            }
        }

        return _hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || !(other instanceof TransformMethodSignature)) return false;

        TransformMethodSignature ms = (TransformMethodSignature) other;

        return _modifiers == ms._modifiers && _returnType.equals(ms._returnType)
                && _methodName.equals(ms._methodName)
                && matches(_parameterTypes, ms._parameterTypes)
                && matches(_exceptionTypes, ms._exceptionTypes);
    }

    private boolean matches(String[] values, String[] otherValues)
    {
        if (values.length != otherValues.length) return false;

        for (int i = 0; i < values.length; i++)
        {
            if (!values[i].equals(otherValues[i])) return false;
        }

        return true;
    }

    /**
     * Returns the long form description of the signature. This includes modifiers, return type,
     * method name, parameters and thrown exceptions, formatter approximately as it would appear in
     * Java source (except that parameter names, which are not known, do no appear).
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        // Package private is simply omitted.

        if (_modifiers != 0)
        {
            builder.append(Modifier.toString(_modifiers));
            builder.append(' ');
        }

        builder.append(_returnType);
        builder.append(' ');

        addMethodNameAndParameters(builder);

        for (int i = 0; i < _exceptionTypes.length; i++)
        {
            if (i == 0)
                builder.append(" throws ");
            else
                builder.append(", ");

            builder.append(_exceptionTypes[i]);
        }

        return builder.toString();
    }

    private void addMethodNameAndParameters(StringBuilder builder)
    {
        builder.append(_methodName);
        builder.append('(');

        for (int i = 0; i < _parameterTypes.length; i++)
        {
            if (i > 0) builder.append(", ");

            builder.append(_parameterTypes[i]);
        }

        builder.append(')');
    }

    /**
     * Sorting is primarily via method name. For methods with the same name, the second level of
     * sorting is by parameter count (descending).
     */
    public int compareTo(TransformMethodSignature o)
    {
        int result = _methodName.compareTo(o._methodName);

        // Sort descending
        if (result == 0) result = o._parameterTypes.length - _parameterTypes.length;

        return result;
    }

    /**
     * Returns a shortened form of the string representation of the method. It lists just the name
     * of the method and the types of any parameters, omitting return type, exceptions and
     * modifiers.
     * 
     * @return
     */
    public String getMediumDescription()
    {
        StringBuilder builder = new StringBuilder();

        addMethodNameAndParameters(builder);

        return builder.toString();
    }

}
