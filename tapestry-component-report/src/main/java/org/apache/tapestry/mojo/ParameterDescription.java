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

package org.apache.tapestry.mojo;

public class ParameterDescription
{
    private final String _name;

    private final String _type;

    private final String _defaultValue;

    private final String _defaultPrefix;

    private final boolean _required;

    private final boolean _cache;

    private final String _description;

    public ParameterDescription(String name, String type, String defaultValue,
            String defaultPrefix, boolean required, boolean cache, String description)
    {
        _name = name;
        _type = type;
        _defaultValue = defaultValue;
        _defaultPrefix = defaultPrefix;
        _required = required;
        _cache = cache;
        _description = description;
    }

    public boolean getCache()
    {
        return _cache;
    }

    public String getDefaultPrefix()
    {
        return _defaultPrefix;
    }

    public String getDefaultValue()
    {
        return _defaultValue;
    }

    public String getDescription()
    {
        return _description;
    }

    public String getName()
    {
        return _name;
    }

    public boolean getRequired()
    {
        return _required;
    }

    public String getType()
    {
        return _type;
    }

}
