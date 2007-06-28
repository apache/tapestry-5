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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.Binding;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Request;

/** A Checkbox component is simply a &lt;input type="checkbox"&gt;. */
public class Checkbox extends AbstractField
{
    @Inject
    private Request _request;

    /**
     * The value to be read or updated. If not bound, the Checkbox will attempt to edit a property
     * of its container whose name matches the component's id.
     */
    @Parameter(required = true)
    private boolean _value;

    Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    @BeginRender
    void begin(MarkupWriter writer)
    {
        writer.element(
                "input",
                "type",
                "checkbox",
                "name",
                getElementName(),
                "id",
                getClientId(),
                "checked",
                _value ? "checked" : null);

        getValidationDecorator().insideField(this);
    }

    @AfterRender
    void after(MarkupWriter writer)
    {
        writer.end(); // input
    }

    @Override
    protected void processSubmission(FormSupport formSupport, String elementName)
    {
        String postedValue = _request.getParameter(elementName);

        _value = postedValue != null;
    }

}
