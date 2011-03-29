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

import org.apache.tapestry5.plastic.FieldHandle;
import org.apache.tapestry5.plastic.MethodHandle;
import org.apache.tapestry5.plastic.MethodInvocationResult;
import org.apache.tapestry5.plastic.PlasticClass;

/**
 * The interface for a "shim" class that provides the necessary hooks needed
 * by {@link FieldHandle} and {@link MethodHandle} implementations for a particular,
 * instantiated {@link PlasticClass}.
 */
public abstract class PlasticClassHandleShim
{
    /**
     * Gets the field at the given index.
     * 
     * @param instance
     *            object to read instance field from
     * @param fieldIndex
     *            assigned index for the field
     * @return the field's value
     * @see FieldHandle#get(Object)
     */
    public Object get(Object instance, int fieldIndex)
    {
        return null;
    }

    /**
     * Sets the value of a field.
     * 
     * @param instance
     *            object to update instance field in
     * @param fieldIndex
     *            assigned index for the field
     * @param newValue
     *            new value for field
     * @see FieldHandle#set(Object, Object)
     */
    public void set(Object instance, int fieldIndex, Object newValue)
    {
    }

    /**
     * Invokes a method.
     * 
     * @param instance
     *            object to invoke a method upon
     * @param methodIndex
     *            assigned index for the method
     * @param arguments
     *            arguments to pass to the method
     * @return result of invoking the method
     */
    public MethodInvocationResult invoke(Object instance, int methodIndex, Object[] arguments)
    {
        return null;
    }
}
