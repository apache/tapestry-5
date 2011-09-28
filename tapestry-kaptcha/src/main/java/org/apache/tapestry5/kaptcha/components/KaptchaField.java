// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.kaptcha.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FieldValidatorSource;
import org.apache.tapestry5.services.Request;

/**
 * Field paired with a {@link KaptchaImage} to ensure that the user has provided
 * the correct value.
 *
 * @since 5.3
 */
@SupportsInformalParameters
@Import(stylesheet = "kaptcha.css")
public class KaptchaField extends AbstractField
{

    /**
     * The image output for this field. The image will display a distorted text
     * string. The user must decode the distorted text and enter the same value.
     */
    @Parameter(required = true, defaultPrefix = BindingConstants.COMPONENT)
    private KaptchaImage image;

    @Inject
    private Request request;

    @Inject
    private Messages messages;

    @Inject
    private ComponentResources resources;

    @Environmental
    private ValidationTracker validationTracker;

    @Inject
    private FieldValidatorSource fieldValidatorSource;

    @Override
    public boolean isRequired()
    {
        return true;
    }

    @Override
    protected void processSubmission(String controlName)
    {
        String userValue = request.getParameter(controlName);

        if (TapestryInternalUtils.isEqual(image.getCaptchaText(), userValue))
            return;

        validationTracker.recordError(this, messages.get("tapestry-incorrect-captcha"));
    }

    @SuppressWarnings("rawtypes")
    @BeginRender
    boolean begin(MarkupWriter writer)
    {

        writer.element("input",

                "type", "password",

                "id", getClientId(),

                "name", getControlName(),

                "value", "");

        resources.renderInformalParameters(writer);

        FieldValidator fieldValidator = fieldValidatorSource
                .createValidator(this, "required", null);

        fieldValidator.render(writer);

        writer.end();

        return false;
    }
}
