// Copyright 2006, 2008, 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.model;

import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.ParameterModel;

public class ParameterModelImpl implements ParameterModel
{
    private final ComponentModel componentModel;

    private final String name;

    private final boolean required;

    private final boolean allowNull;

    private final String defaultBindingPrefix;

    private final boolean cached;

    public ParameterModelImpl(ComponentModel componentModel, String name, boolean required, boolean allowNull, String defaultBindingPrefix, boolean cached)
    {
        this.componentModel = componentModel;
        this.name = name;
        this.required = required;
        this.allowNull = allowNull;
        this.defaultBindingPrefix = defaultBindingPrefix;
        this.cached = cached;
    }

    public String getName()
    {
        return name;
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getDefaultBindingPrefix()
    {
        return defaultBindingPrefix;
    }

    public boolean isAllowNull()
    {
        return allowNull;
    }

    public boolean isCached()
    {
        return cached;
    }

    public ComponentModel getComponentModel()
    {
        return componentModel;
    }
}
