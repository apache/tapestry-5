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

package org.apache.tapestry.internal.transform.pages;

import org.apache.tapestry.annotations.Retain;

/**
 * Used to test retained vs. discard properties.
 */
public class BasicComponent
{
    private String _value;

    @Retain
    private String _retainedValue;

    public final String getRetainedValue()
    {
        return _retainedValue;
    }

    public final void setRetainedValue(String retainedValue)
    {
        _retainedValue = retainedValue;
    }

    public final String getValue()
    {
        return _value;
    }

    public final void setValue(String value)
    {
        _value = value;
    }
}
