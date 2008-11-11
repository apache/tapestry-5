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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;

import java.util.List;
import java.util.Map;

public final class OptionGroupModelImpl implements OptionGroupModel
{
    private final String label;

    private final boolean disabled;

    private final List<OptionModel> options;

    private final Map<String, String> attributes;

    public OptionGroupModelImpl(String label, boolean disabled, List<OptionModel> options,
                                String... attributeKeysAndValues)
    {
        this(label, disabled, options, attributeKeysAndValues.length == 0 ? null : TapestryInternalUtils
                .mapFromKeysAndValues(attributeKeysAndValues));
    }

    public OptionGroupModelImpl(String label, boolean disabled, List<OptionModel> options,
                                Map<String, String> attributes)
    {
        this.label = label;
        this.disabled = disabled;
        this.options = options;
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }

    public String getLabel()
    {
        return label;
    }

    public List<OptionModel> getOptions()
    {
        return options;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    @Override
    public String toString()
    {
        return String.format("OptionGroupModel[%s]", label);
    }

}
