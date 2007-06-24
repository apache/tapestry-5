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

package org.apache.tapestry.corelib.components;

import java.text.Format;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.ComponentDefaultProvider;

/**
 * A component for formatting output. If the component is represented in the template using an
 * element, then the element (plus any informal parameters) will be output around the formatted
 * value.
 */
@SupportsInformalParameters
public class Output
{
    /**
     * The value to be output (before formatting). If the formatted value is blank, no output is
     * produced.
     */
    @Parameter(required = true)
    private Object _value;

    /** The format to be applied to the object. */
    @Parameter(required = true)
    private Format _format;

    /**
     * The element name, derived from the component template. This can even be overridden manually
     * if desired (for example, to sometimes render a surrounding element and other times not).
     */
    @Parameter("componentResources.elementName")
    private String _elementName;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    @Inject
    private ComponentResources _resources;

    Binding defaultValue()
    {
        return _defaultProvider.defaultBinding("value", _resources);
    }

    boolean beginRender(MarkupWriter writer)
    {
        String formatted = _format.format(_value);

        if (InternalUtils.isNonBlank(formatted))
        {
            if (_elementName != null)
            {
                writer.element(_elementName);

                _resources.renderInformalParameters(writer);
            }

            writer.write(formatted);

            if (_elementName != null) writer.end();
        }

        return false;
    }

    // For testing.

    void setup(Object value, Format format, String elementName, ComponentResources resources)
    {
        _value = value;
        _format = format;
        _elementName = elementName;
        _resources = resources;
    }
}
