// Copyright 2006, 2007 The Apache Software Foundation
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
 * The start element of a component within the template. Will be followed by a series of {@link
 * org.apache.tapestry5.internal.parser.AttributeToken}s for any attributes (outside of id and type), and eventually
 * will be balanced by an {@link org.apache.tapestry5.internal.parser.EndElementToken}.
 */
public class StartComponentToken extends TemplateToken
{
    private final String elementName;

    private final String id;

    private final String componentType;

    private final String mixins;

    /**
     * @param elementName the name of the element from which this component was parsed, or null if the element was the
     *                    t:comp placeholder
     * @param id          the id of the component (may be null for anonymous components)
     * @param type        the type of component (may be null if the component type is specified outside the template)
     * @param mixins      a comma-separated list of mixins (possibly null)
     * @param location    the location within the template at which the element was parsed
     */
    public StartComponentToken(String elementName, String id, String type, String mixins,
                               Location location)
    {
        super(TokenType.START_COMPONENT, location);

        // TODO: id or type may be null, but not both!

        this.elementName = elementName;
        this.id = id;
        componentType = type;
        this.mixins = mixins;
    }

    /**
     * Returns the element for this component. When using the &lt;t:comp&gt; placeholder, this value will be null. When
     * using "invisible instrumentation", where t:id or t:type attributes are added to existing elements, this is the
     * local name of the element so attached.
     *
     * @return the element name or null
     */
    public String getElementName()
    {
        return elementName;
    }

    /**
     * Returns a non-blank id if one was provided in the template. If the id attribute was missing (or the value was
     * blank), returns null.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns a non-blank component type if one was provided in the template. If the type attribute was missing (or the
     * value was blank), returns null.
     */
    public String getComponentType()
    {
        return componentType;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        add(builder, "element", elementName);
        add(builder, "id", id);
        add(builder, "type", componentType);
        add(builder, "mixins", mixins);

        builder.insert(0, "StartComponentToken[");
        builder.append("]");

        return builder.toString();
    }

    private void add(StringBuilder builder, String label, String value)
    {
        if (value == null)
            return;

        if (builder.length() > 0)
            builder.append(" ");

        builder.append(label);
        builder.append("=");
        builder.append(value);
    }

    /**
     * Returns the list of mixins for this component instance, or null for no mixins.
     */
    public String getMixins()
    {
        return mixins;
    }

}
