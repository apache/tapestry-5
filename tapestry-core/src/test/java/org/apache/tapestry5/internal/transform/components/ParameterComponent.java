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

package org.apache.tapestry5.internal.transform.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Parameter;

/**
 * Used by {@link org.apache.tapestry5.internal.transform.ParameterWorkerTest}.
 */
public class ParameterComponent
{
    @Parameter
    private String object;

    @Parameter(cache = false, name = "uncached", defaultPrefix = BindingConstants.LITERAL)
    private String uncachedObject;

    @Parameter(required = true)
    private int primitive;

    @Parameter
    private String invariantObject;

    @Parameter
    private long invariantPrimitive;

    public String getObject()
    {
        return object;
    }

    public void setObject(String object)
    {
        this.object = object;
    }

    public int getPrimitive()
    {
        return primitive;
    }

    public void setPrimitive(int primitive)
    {
        this.primitive = primitive;
    }

    public String getUncachedObject()
    {
        return uncachedObject;
    }

    public void setUncachedObject(String uncachedObject)
    {
        this.uncachedObject = uncachedObject;
    }

    public String getInvariantObject()
    {
        return invariantObject;
    }

    public void setInvariantObject(String invariantObject)
    {
        this.invariantObject = invariantObject;
    }

    public long getInvariantPrimitive()
    {
        return invariantPrimitive;
    }

    public void setInvariantPrimitive(long invariantPrimitive)
    {
        this.invariantPrimitive = invariantPrimitive;
    }

}
