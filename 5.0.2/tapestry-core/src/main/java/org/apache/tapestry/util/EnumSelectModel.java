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

package org.apache.tapestry.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.io.Serializable;
import java.util.List;

import org.apache.tapestry.OptionGroupModel;
import org.apache.tapestry.OptionModel;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.internal.OptionModelImpl;
import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.Defense;

/**
 * A basic select model for a particular Enum type. The labels for each Enum are drawn from the Enum
 * instance name and the provides message catalog:
 * <ul>
 * <li>As key <em>ClassName</em>-<em>name</em> if present. The class name excludes the
 * package portion. Ex: "ElementType.LOCAL_VARIABLE"
 * <li>As key <em>name</em> if present, i.e., "LOCAL_VARIABLE".
 * <li>As a user-presentable version of the name, i.e., "Local Variable".
 * </ul>
 */
public final class EnumSelectModel implements SelectModel, Serializable
{
    private static final long serialVersionUID = -3590412082766899684L;

    private final List<OptionModel> _options = newList();;

    public <T extends Enum> EnumSelectModel(Class<T> enumClass, Messages messages)
    {
        this(enumClass, messages, enumClass.getEnumConstants());
    }

    public <T extends Enum> EnumSelectModel(Class<T> enumClass, Messages messages, T[] values)
    {
        Defense.notNull(enumClass, "enumClass");
        Defense.notNull(messages, "messages");

        String prefix = IOCUtilities.toSimpleId(enumClass.getName());

        for (T value : values)
        {
            String label = TapestryUtils.getLabelForEnum(messages, prefix, value);

            _options.add(new OptionModelImpl(label, false, value));
        }
    }

    /** Returns null. */
    public List<OptionGroupModel> getOptionGroups()
    {
        return null;
    }

    /** Returns the option groupos created in the constructor. */
    public List<OptionModel> getOptions()
    {
        return _options;
    }

}
