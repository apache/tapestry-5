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

package org.apache.tapestry.internal;

import org.apache.tapestry.AbstractOptionModel;

public class OptionModelImpl extends AbstractOptionModel
{
    private final String _label;

    private final Object _value;

    /**
     * Constructor for when the value and the label are the same.
     */
    public OptionModelImpl(String value)
    {
        this(value, value);
    }

    public OptionModelImpl(String label, Object value)
    {
        _label = label;
        _value = value;
    }

    public String getLabel()
    {
        return _label;
    }

    public Object getValue()
    {
        return _value;
    }

    @Override
    public String toString()
    {
        return String.format("OptionModel[%s %s]", _label, _value);
    }
}
