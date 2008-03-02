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

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.ComponentDefaultProvider;

/**
 * Used to output raw markup to the client. Unlike, say, an expansion, the output from OutputRaw is unfiltered, with any
 * special characters or entities left exactly as is. This is used in situations where the markup is provided
 * externally, rather than constructed within Tapestry.
 *
 * @see MarkupWriter#writeRaw(String)
 */
public class OutputRaw
{
    /**
     * The value to to render. If unbound, and a property of the container matches the component's id, then that
     * property will be the source of the value.
     */
    @Parameter(required = true)
    private String _value;

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
        if (_value != null && _value.length() > 0) writer.writeRaw(_value);

        // Abort the rest of the render.

        return false;
    }

    // For testing:

    void setValue(String value)
    {
        _value = value;
    }

}
