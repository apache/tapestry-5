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

package org.apache.tapestry.internal.transform.components;

import org.apache.tapestry.annotations.Parameter;

/**
 * Used by {@link org.apache.tapestry.internal.services.ParameterWorkerTest}.
 */
public class ParameterComponent
{
    @Parameter
    private String _object;

    @Parameter(cache = false, name = "uncached", defaultPrefix = "literal")
    private String _uncachedObject;

    @Parameter(required = true)
    private int _primitive;

    @Parameter
    private String _invariantObject;

    @Parameter
    private long _invariantPrimitive;

    public String getObject()
    {
        return _object;
    }

    public void setObject(String object)
    {
        _object = object;
    }

    public int getPrimitive()
    {
        return _primitive;
    }

    public void setPrimitive(int primitive)
    {
        _primitive = primitive;
    }

    public String getUncachedObject()
    {
        return _uncachedObject;
    }

    public void setUncachedObject(String uncachedObject)
    {
        _uncachedObject = uncachedObject;
    }

    public String getInvariantObject()
    {
        return _invariantObject;
    }

    public void setInvariantObject(String invariantObject)
    {
        _invariantObject = invariantObject;
    }

    public long getInvariantPrimitive()
    {
        return _invariantPrimitive;
    }

    public void setInvariantPrimitive(long invariantPrimitive)
    {
        _invariantPrimitive = invariantPrimitive;
    }

}
