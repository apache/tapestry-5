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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;

/**
 * A token from a template that defines a namespace prefix. This will always follow a {@link
 * org.apache.tapestry5.internal.parser.StartComponentToken} or {@link org.apache.tapestry5.internal.parser.StartElementToken}
 * (and come before {@link org.apache.tapestry5.internal.parser.AttributeToken}) and applies to the component or
 * element.
 *
 * @see org.apache.tapestry5.dom.Element#defineNamespace(String, String)
 */
public class DefineNamespacePrefixToken extends TemplateToken
{
    private final String namespaceURI;
    private final String namespacePrefix;

    public DefineNamespacePrefixToken(String namespaceURI, String namespacePrefix, Location location)
    {
        super(TokenType.DEFINE_NAMESPACE_PREFIX, location);

        this.namespacePrefix = namespacePrefix;
        this.namespaceURI = namespaceURI;
    }

    public String getNamespacePrefix()
    {
        return namespacePrefix;
    }

    public String getNamespaceURI()
    {
        return namespaceURI;
    }

    @Override
    public String toString()
    {
        return String.format("DefineNamespacePrefix[%s=%s]", namespacePrefix, namespaceURI);
    }
}
