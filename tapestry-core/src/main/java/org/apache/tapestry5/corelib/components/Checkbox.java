// Copyright 2006-2013 The Apache Software Foundation
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

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;

/**
 * A Checkbox component is simply a &lt;input type="checkbox"&gt;.
 *
 * @tapestrydoc
 */
public class Checkbox extends AbstractField
{
    /**
     * The value to be read or updated. If not bound, the Checkbox will attempt to edit a property of its container
     * whose name matches the component's id.
     */
    @Parameter(required = true, autoconnect = true)
    private boolean value;

    /**
     * The object that will perform input validation. The validate binding prefix is
     * generally used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE, allowNull = false)
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    @BeginRender
    void begin(MarkupWriter writer)
    {
        String asSubmitted = validationTracker.getInput(this);

        boolean checked = asSubmitted != null ? Boolean.parseBoolean(asSubmitted) : value;

        writer.element("input", "type", "checkbox",
                "name", getControlName(),
                "id", getClientId(),
                "checked", checked ? "checked" : null);

        putPropertyNameIntoBeanValidationContext("value");

        validate.render(writer);

        removePropertyNameFromBeanValidationContext();

        resources.renderInformalParameters(writer);


        decorateInsideField();
    }

    @AfterRender
    void after(MarkupWriter writer)
    {
        writer.end(); // input
    }

    @Override
    protected void processSubmission(String controlName)
    {
        String postedValue = request.getParameter(controlName);

        boolean translated = postedValue != null;

        // record as "true" or "false"
        validationTracker.recordInput(this, Boolean.toString(translated));

        putPropertyNameIntoBeanValidationContext("value");
        try
        {
            fieldValidationSupport.validate(translated, resources, validate);

            value = translated;
        } catch (ValidationException ex)
        {
            validationTracker.recordError(this, ex.getMessage());
        }
        removePropertyNameFromBeanValidationContext();
    }

    /**
     * Computes a default value for the "validate" parameter using
     * {@link org.apache.tapestry5.services.FieldValidatorDefaultSource}.
     */
    final Binding defaultValidate()
    {
        return defaultProvider.defaultValidatorBinding("value", resources);
    }
}
