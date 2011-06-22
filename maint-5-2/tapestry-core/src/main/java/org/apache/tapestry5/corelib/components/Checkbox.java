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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

/**
 * A Checkbox component is simply a &lt;input type="checkbox"&gt;.
 */
public class Checkbox extends AbstractField
{
    /**
     * The value to be read or updated. If not bound, the Checkbox will attempt to edit a property of its container
     * whose name matches the component's id.
     */
    @Parameter(required = true, autoconnect = true)
    private boolean value;

    @Inject
    private Request request;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    @Inject
    private ComponentResources resources;

    @Environmental
    private ValidationTracker tracker;

    @BeginRender
    void begin(MarkupWriter writer)
    {
        String asSubmitted = tracker.getInput(this);

        boolean checked = asSubmitted != null ? Boolean.parseBoolean(asSubmitted) : value;

        writer.element("input", "type", "checkbox",

                       "name", getControlName(),

                       "id", getClientId(),

                       "checked", checked ? "checked" : null);

        resources.renderInformalParameters(writer);

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
        String postedValue = request.getParameter(elementName);

        // record as "true" or "false"

        tracker.recordInput(this, Boolean.toString(postedValue != null));

        value = postedValue != null;
    }
}
