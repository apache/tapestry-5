// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.annotations.*;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.Request;

/**
 * A Checkbox component is simply a &lt;input type="checkbox"&gt;.
 */
public class Checkbox extends AbstractField
{
    @Inject
    private Request _request;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled _renderDisabled;

    /**
     * The value to be read or updated. If not bound, the Checkbox will attempt to edit a property of its container
     * whose name matches the component's id.
     */
    @Parameter(required = true)
    private boolean _value;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private ValidationTracker _tracker;

    Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    @BeginRender
    void begin(MarkupWriter writer)
    {
        String asSubmitted = _tracker.getInput(this);

        boolean checked = asSubmitted != null ? Boolean.parseBoolean(asSubmitted) : _value;

        writer.element("input", "type", "checkbox",

                       "name", getElementName(),

                       "id", getClientId(),

                       "checked", checked ? "checked" : null);

        _resources.renderInformalParameters(writer);

        decorateInsideField();
    }

    @AfterRender
    void after(MarkupWriter writer)
    {
        writer.end(); // input
    }

    @Override
    protected void processSubmission(String elementName)
    {
        String postedValue = _request.getParameter(elementName);

        // record as "true" or "false"

        _tracker.recordInput(this, Boolean.toString(postedValue != null));

        _value = postedValue != null;
    }

}
