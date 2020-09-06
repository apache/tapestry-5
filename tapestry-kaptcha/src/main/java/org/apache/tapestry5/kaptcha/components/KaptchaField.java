// Copyright 2011-2013 The Apache Software Foundation
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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.kaptcha.KaptchaSymbolConstants;
import org.apache.tapestry5.services.FieldValidatorSource;

/**
 * Field paired with a {@link KaptchaImage} to ensure that the user has provided
 * the correct value. Renders a class attribute of "form-control", which can be extended
 * via the class informal attribute.
 *
 * @since 5.3
 * @tapestrydoc
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

    /**
     * Controls whether the field is rendered like a password field (false)
     * or like a normal text field (true, the factory default).
     *
     * @see KaptchaSymbolConstants#KAPTCHA_DEFAULT_VISIBLE
     * @since 5.3
     */
    @Parameter("symbol:" + KaptchaSymbolConstants.KAPTCHA_DEFAULT_VISIBLE)
    private boolean visible;

    @Inject
    private Messages messages;

    @Inject
    private FieldValidatorSource fieldValidatorSource;

    /**
     * Always required.
     */
    @Override
    public boolean isRequired()
    {
        return true;
    }

    @Override
    protected void processSubmission(String controlName)
    {
        String userValue = request.getParameter(controlName);

        validationTracker.recordInput(this, userValue);

        if (TapestryInternalUtils.isEqual(image.getCaptchaText(), userValue))
            return;

        validationTracker.recordError(this, messages.get("tapestry-incorrect-captcha"));
    }

    @SuppressWarnings("rawtypes")
    boolean beginRender(MarkupWriter writer)
    {
        writer.element("input",

                "type", visible ? "text" : "password",

                "id", getClientId(),

                "class", "form-control",

                "name", getControlName(),

                "value", visible ? validationTracker.getInput(this) : "");

        resources.renderInformalParameters(writer);

        FieldValidator fieldValidator = fieldValidatorSource.createValidator(this, "required", null);

        fieldValidator.render(writer);

        writer.end();

        return false;
    }
}
