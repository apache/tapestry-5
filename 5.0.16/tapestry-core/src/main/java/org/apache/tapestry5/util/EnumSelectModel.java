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

package org.apache.tapestry5.util;

import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;

import java.io.Serializable;
import java.util.List;

/**
 * A basic select model for a particular Enum type. The labels for each Enum are drawn from the Enum instance name and
 * the provides message catalog: <ul> <li>As key <em>ClassName</em>.<em>name</em> if present. The class name excludes
 * the package portion. Ex: "ElementType.LOCAL_VARIABLE" <li>As key <em>name</em> if present, i.e., "LOCAL_VARIABLE".
 * <li>As a user-presentable version of the name, i.e., "Local Variable". </ul>
 */
public final class EnumSelectModel extends AbstractSelectModel implements Serializable
{
    private static final long serialVersionUID = -3590412082766899684L;

    private final List<OptionModel> options = CollectionFactory.newList();

    public <T extends Enum> EnumSelectModel(Class<T> enumClass, Messages messages)
    {
        this(enumClass, messages, enumClass.getEnumConstants());
    }

    public <T extends Enum> EnumSelectModel(Class<T> enumClass, Messages messages, T[] values)
    {
        notNull(enumClass, "enumClass");
        notNull(messages, "messages");

        String prefix = enumClass.getSimpleName();

        for (T value : values)
        {
            String label = TapestryInternalUtils.getLabelForEnum(messages, prefix, value);

            options.add(new OptionModelImpl(label, value));
        }
    }

    /**
     * Returns null.
     */
    public List<OptionGroupModel> getOptionGroups()
    {
        return null;
    }

    /**
     * Returns the option groupos created in the constructor.
     */
    public List<OptionModel> getOptions()
    {
        return options;
    }

}
