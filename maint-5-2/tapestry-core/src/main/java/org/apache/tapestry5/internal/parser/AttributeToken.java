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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;

/**
 * Stores an attribute/value pair (as part of an XML element).
 */
public class AttributeToken extends TemplateToken
{
    private final String namespaceURI;

    private final String name;

    private final String value;

    public AttributeToken(String namespaceURI, String name, String value, Location location)
    {
        super(TokenType.ATTRIBUTE, location);

        this.namespaceURI = namespaceURI;
        this.name = name;
        this.value = value;
    }

    /**
     * Returns local name for the attribute.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the value for the attribute.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Returns the namespace URI containing the attribute, or the empty string for the default namespace.
     */
    public String getNamespaceURI()
    {
        return namespaceURI;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Attribute[");

        if (namespaceURI.length() > 0) builder.append(namespaceURI).append(" ");

        builder.append(name).append("=").append(value).append("]");

        return builder.toString();
    }
}
