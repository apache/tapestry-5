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

package org.apache.tapestry.internal;

import java.util.Map;

import org.apache.tapestry.OptionModel;

public final class OptionModelImpl implements OptionModel
{
    private final String _label;

    private final boolean _disabled;

    private final Object _value;

    private final Map<String, String> _attributes;

    public OptionModelImpl(String label, boolean disabled, Object value, String... keysAndValues)
    {
        this(label, disabled, value, keysAndValues.length > 0 ? TapestryInternalUtils
                .mapFromKeysAndValues(keysAndValues) : null);
    }

    public OptionModelImpl(String label, boolean disabled, Object value,
            Map<String, String> attributes)
    {
        _label = label;
        _disabled = disabled;
        _value = value;
        _attributes = attributes;
    }

    public Map<String, String> getAttributes()
    {
        return _attributes;
    }

    public String getLabel()
    {
        return _label;
    }

    public Object getValue()
    {
        return _value;
    }

    public boolean isDisabled()
    {
        return _disabled;
    }

    @Override
    public String toString()
    {
        return String.format("OptionModel[%s %s]", _label, _value);
    }
}
