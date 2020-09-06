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

package org.apache.tapestry5.internal.services;

import java.util.List;

import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.ioc.BaseLocatable;

/**
 * A token parsed from an XML file, used when parsing templates.
 * 
 * @since 5.2.0
 */
class XMLToken extends BaseLocatable
{
    final XMLTokenType type;

    DTDData dtdData;

    String uri, localName, qName;

    List<AttributeInfo> attributes;

    List<NamespaceMapping> namespaceMappings;

    // COMMENT, CDATA, CHARACTERS, WHITESPACE
    String text;

    XMLToken(XMLTokenType type, Location location)
    {
        super(location);

        this.type = type;
    }

}
