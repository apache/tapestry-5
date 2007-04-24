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

import java.util.List;
import java.util.Map;

import org.apache.tapestry.OptionGroupModel;
import org.apache.tapestry.OptionModel;

public final class OptionGroupModelImpl implements OptionGroupModel
{
    private final String _label;

    private final boolean _disabled;

    private final List<OptionModel> _options;

    private final Map<String, String> _attributes;

    public OptionGroupModelImpl(String label, boolean disabled, List<OptionModel> options,
            String... attributeKeysAndValues)
    {
        this(label, disabled, options, attributeKeysAndValues.length == 0 ? null : TapestryUtils
                .mapFromKeysAndValues(attributeKeysAndValues));
    }

    public OptionGroupModelImpl(String label, boolean disabled, List<OptionModel> options,
            Map<String, String> attributes)
    {
        _label = label;
        _disabled = disabled;
        _options = options;
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

    public List<OptionModel> getOptions()
    {
        return _options;
    }

    public boolean isDisabled()
    {
        return _disabled;
    }

    @Override
    public String toString()
    {
        return String.format("OptionGroupModel[%s]", _label);
    }

}
