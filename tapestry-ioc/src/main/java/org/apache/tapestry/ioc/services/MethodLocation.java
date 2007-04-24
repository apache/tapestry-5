// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.services;

import java.lang.reflect.Method;

import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * Enapsulates a method and its location (source file and line number) when the latter information
 * is available.
 * 
 * @see ClassFactory#getMethodLocation(Method)
 */
public final class MethodLocation
{
    private final Method _method;

    private final String _sourceFile;

    private final int _lineNumber;

    public MethodLocation(Method method, final String sourceFile, final int lineNumber)
    {
        _method = method;
        _sourceFile = sourceFile;
        _lineNumber = lineNumber;
    }

    /**
     * Creates a user presentable string identifying the method (class name, method name, and
     * parameter list), plus the source file and line number, i.e.:
     * "org.example.myapp.MyClass.myMethod(String, List) (at MyClass.java:23)".
     */
    @Override
    public String toString()
    {
        return String.format(
                "%s (at %s:%d)",
                InternalUtils.asString(_method),
                _sourceFile,
                _lineNumber);
    }

    public int getLineNumber()
    {
        return _lineNumber;
    }

    public String getSourceFile()
    {
        return _sourceFile;
    }
}
