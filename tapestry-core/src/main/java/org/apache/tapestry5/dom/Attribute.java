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

package org.apache.tapestry5.dom;

import org.apache.tapestry5.internal.TapestryInternalUtils;

import java.util.Map;

/**
 * An attribute within an {@link org.apache.tapestry5.dom.Element}. Each attribute has a namespace URI, a local name
 * within the namespace, and a value.
 *
 * @since 5.1.0.2
 */
public class Attribute
{
    private final Element element;

    private final String namespace;

    private final String name;

    String value;

    Attribute nextAttribute;

    Attribute(Element element, String namespace, String name, String value, Attribute nextAttribute)
    {
        this.element = element;
        this.namespace = namespace;
        this.name = name;
        this.value = value;
        this.nextAttribute = nextAttribute;
    }

    public String getName()
    {
        return name;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public String getValue()
    {
        return value;
    }

    void render(MarkupModel model, StringBuilder builder, Map<String, String> namespaceURIToPrefix)
    {
        builder.append(" ");
        builder.append(element.toPrefixedName(namespaceURIToPrefix, namespace, name));
        builder.append("=");
        builder.append(model.getAttributeQuote());
        model.encodeQuoted(value, builder);
        builder.append(model.getAttributeQuote());
    }

    boolean matches(String namespace, String name)
    {
        return TapestryInternalUtils.isEqual(this.namespace, namespace) &&
                this.name.equalsIgnoreCase(name);
    }
}
