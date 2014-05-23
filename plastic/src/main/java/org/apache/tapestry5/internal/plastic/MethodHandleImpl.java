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

import org.apache.tapestry5.plastic.MethodHandle;
import org.apache.tapestry5.plastic.MethodInvocationResult;

public class MethodHandleImpl implements MethodHandle
{
    private final String className, methodDescription;

    private final int methodIndex;

    protected volatile PlasticClassHandleShim shim;

    public MethodHandleImpl(String className, String methodDescription, int methodIndex)
    {
        this.className = className;
        this.methodDescription = methodDescription;
        this.methodIndex = methodIndex;
    }

    @Override
    public String toString()
    {
        return String.format("MethodHandle[%s %s]", className, methodDescription);
    }

    @Override
    public MethodInvocationResult invoke(Object instance, Object... arguments)
    {

        if (instance == null)
            throw new NullPointerException(String.format(
                    "Unable to invoke method %s of class %s, as provided instance is null.", methodDescription,
                    className));

        return shim.invoke(instance, methodIndex, arguments);
    }

}
