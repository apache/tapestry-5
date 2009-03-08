// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.util;

import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.ioc.Messages;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * A basic select model for a collection of Strings.
 */
public class StringSelectModel extends AbstractSelectModel implements Serializable
{
    private List<OptionModel> options = CollectionFactory.newList();

    public StringSelectModel(List<String> values)
    {
        notNull(values, "values");

        options = Collections.unmodifiableList(TapestryInternalUtils.toOptionModels(values));
    }

    public StringSelectModel(String... values)
    {
        this(Arrays.asList(values));
    }

    public List<OptionGroupModel> getOptionGroups()
    {
        return null;
    }

    public List<OptionModel> getOptions()
    {
        return options;
    }
}
