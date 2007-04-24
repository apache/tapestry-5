// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.parser;

import org.apache.tapestry.ioc.Location;

/**
 * Stores an attribute/value pair (as part of an XML element).
 * 
 * 
 */
public class AttributeToken extends TemplateToken
{
    private final String _name;

    private final String _value;

    public AttributeToken(String name, String value, Location location)
    {
        super(TokenType.ATTRIBUTE, location);

        _name = name;
        _value = value;
    }

    /** Returns local name for the attribute. */
    public String getName()
    {
        return _name;
    }

    /** Returns the value for the attribute. */
    public String getValue()
    {
        return _value;
    }

    @Override
    public String toString()
    {
        return String.format("Attribute[%s=%s]", _name, _value);
    }
}
