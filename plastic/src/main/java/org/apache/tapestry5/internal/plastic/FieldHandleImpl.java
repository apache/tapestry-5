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

public class FieldHandleImpl implements FieldHandle
{
    private final String className, fieldName;

    private final int fieldIndex;

    protected volatile PlasticClassHandleShim shim;

    public FieldHandleImpl(String className, String fieldName, int fieldIndex)
    {
        this.className = className;
        this.fieldName = fieldName;
        this.fieldIndex = fieldIndex;
    }

    @Override
    public String toString()
    {
        return String.format("FieldHandle[%s#%s]", className, fieldName);
    }

    @Override
    public Object get(Object instance)
    {
        checkNullInstance(instance, "get");

        return shim.get(instance, fieldIndex);
    }

    private void checkNullInstance(Object instance, String action)
    {
        if (instance == null)
            throw new NullPointerException(String.format(
                    "Unable to %s value of field %s of class %s, as provided instance is null.", action, fieldName,
                    className));
    }

    @Override
    public void set(Object instance, Object newValue)
    {
        checkNullInstance(instance, "set");

        shim.set(instance, fieldIndex, newValue);
    }
}
