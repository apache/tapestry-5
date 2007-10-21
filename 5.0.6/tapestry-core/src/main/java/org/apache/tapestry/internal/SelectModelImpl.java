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

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry.OptionGroupModel;
import org.apache.tapestry.OptionModel;
import org.apache.tapestry.util.AbstractSelectModel;

public final class SelectModelImpl extends AbstractSelectModel
{
    private final List<OptionGroupModel> _optionGroups;

    private final List<OptionModel> _optionModels;

    public SelectModelImpl(final List<OptionGroupModel> optionGroups,
            final List<OptionModel> optionModels)
    {
        _optionGroups = optionGroups;
        _optionModels = optionModels;
    }

    public SelectModelImpl(OptionModel... optionModels)
    {
        this(null, Arrays.asList(optionModels));
    }

    public SelectModelImpl(OptionGroupModel... groupModels)
    {
        this(Arrays.asList(groupModels), null);
    }

    public List<OptionGroupModel> getOptionGroups()
    {
        return _optionGroups;
    }

    public List<OptionModel> getOptions()
    {
        return _optionModels;
    }

}
