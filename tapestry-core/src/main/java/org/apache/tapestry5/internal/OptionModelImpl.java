// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.AbstractOptionModel;

public class OptionModelImpl extends AbstractOptionModel
{
    private final String label;

    private final Object value;

    /**
     * Constructor for when the value and the label are the same.
     */
    public OptionModelImpl(String value)
    {
        this(value, value);
    }

    public OptionModelImpl(String label, Object value)
    {
        this.label = label;
        this.value = value;
    }

    public String getLabel()
    {
        return label;
    }

    public Object getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return String.format("OptionModel[%s %s]", label, value);
    }
}
