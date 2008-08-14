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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.text.Format;

/**
 * A component for formatting output. If the component is represented in the template using an element, then the element
 * (plus any informal parameters) will be output around the formatted value.
 */
@SupportsInformalParameters
public class Output
{
    /**
     * The value to be output (before formatting). If the formatted value is blank, no output is produced.
     */
    @Parameter(required = true, autoconnect = true)
    private Object value;

    /**
     * The format to be applied to the object.
     */
    @Parameter(required = true, allowNull = false)
    private Format format;

    /**
     * If true, the default, then output is filtered, escaping any reserved characters. If false, the output is written
     * raw.
     */
    @Parameter
    private boolean filter = true;

    /**
     * The element name, derived from the component template. This can even be overridden manually if desired (for
     * example, to sometimes render a surrounding element and other times not).
     */
    @Parameter("componentResources.elementName")
    private String elementName;

    @Inject
    private ComponentResources resources;


    boolean beginRender(MarkupWriter writer)
    {
        if (value == null) return false;

        String formatted = format.format(value);

        if (InternalUtils.isNonBlank(formatted))
        {
            if (elementName != null)
            {
                writer.element(elementName);

                resources.renderInformalParameters(writer);
            }

            if (filter) writer.write(formatted);
            else writer.writeRaw(formatted);

            if (elementName != null) writer.end();
        }

        return false;
    }

    // For testing.

    void setup(Object value, Format format, boolean filter, String elementName, ComponentResources resources)
    {
        this.value = value;
        this.format = format;
        this.filter = filter;
        this.elementName = elementName;
        this.resources = resources;
    }
}
