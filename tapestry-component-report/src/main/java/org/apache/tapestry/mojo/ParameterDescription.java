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

package org.apache.tapestry5.mojo;

public class ParameterDescription
{
    private final String name;

    private final String type;

    private final String defaultValue;

    private final String defaultPrefix;

    private final boolean required;

    private final boolean cache;

    private final String description;

    public ParameterDescription(String name, String type, String defaultValue,
                                String defaultPrefix, boolean required, boolean cache, String description)
    {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.defaultPrefix = defaultPrefix;
        this.required = required;
        this.cache = cache;
        this.description = description;
    }

    public boolean getCache()
    {
        return cache;
    }

    public String getDefaultPrefix()
    {
        return defaultPrefix;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return name;
    }

    public boolean getRequired()
    {
        return required;
    }

    public String getType()
    {
        return type;
    }

}
