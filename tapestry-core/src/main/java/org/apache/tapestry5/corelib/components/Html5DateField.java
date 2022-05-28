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

package org.apache.tapestry5.corelib.components;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentDefaultProvider;

/**
 * A component used to collect a provided date from the user using the native HTML5 date picker 
 * (&lt;input type="date"&gt;)
 * @tapestrydoc
 * @see Form
 * @see TextField
 */
@Events(EventConstants.VALIDATE)
public class Html5DateField extends AbstractField
{
    
    final private static String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * The value parameter of a DateField must be a {@link java.util.Date}.
     */
    @Parameter(required = true, principal = true, autoconnect = true)
    private Date value;

    /**
     * The object that will perform input validation (which occurs after translation). The translate binding prefix is
     * generally used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    private FieldValidator<Object> validate;

    /**
     * Used to override the component's message catalog.
     *
     * @since 5.2.0.0
     * @deprecated Since 5.4; override the global message key "core-date-value-not-parsable" instead (see {@link org.apache.tapestry5.services.messages.ComponentMessagesSource})
     */
    @Parameter("componentResources.messages")
    private Messages messages;
    
    /**
     * Computes a default value for the "validate" parameter using {@link ComponentDefaultProvider}.
     */
    final Binding defaultValidate()
    {
        return defaultProvider.defaultValidatorBinding("value", resources);
    }

    void beginRender(MarkupWriter writer)
    {
        String value = validationTracker.getInput(this);

        if (value == null)
        {
            value = formatCurrentValue();
        }

        String clientId = getClientId();

        writer.element("input",

                "type", "date",

                "class", cssClass,

                "name", getControlName(),

                "id", clientId,

                "value", value);

        writeDisabled(writer);

        putPropertyNameIntoBeanValidationContext("value");

        validate.render(writer);

        removePropertyNameFromBeanValidationContext();

        resources.renderInformalParameters(writer);

        decorateInsideField();

        writer.end();   // input

    }

    private void writeDisabled(MarkupWriter writer)
    {
        if (isDisabled())
            writer.attributes("disabled", "disabled");
    }

    private String formatCurrentValue()
    {
        if (value == null)
            return "";

        return getDateFormat().format(value);
    }

    private DateFormat getDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT);
    }

    @Override
    protected void processSubmission(String controlName)
    {
        String value = request.getParameter(controlName);

        validationTracker.recordInput(this, value);

        Date parsedValue = null;

        try
        {
            if (InternalUtils.isNonBlank(value))
                parsedValue = getDateFormat().parse(value);
        } catch (ParseException ex)
        {
            validationTracker.recordError(this, messages.format("core-date-value-not-parseable", value));
            return;
        }

        putPropertyNameIntoBeanValidationContext("value");
        try
        {
            fieldValidationSupport.validate(parsedValue, resources, validate);

            this.value = parsedValue;
        } catch (ValidationException ex)
        {
            validationTracker.recordError(this, ex.getMessage());
        }

        removePropertyNameFromBeanValidationContext();
    }

    void injectResources(ComponentResources resources)
    {
        this.resources = resources;
    }

    @Override
    public boolean isRequired()
    {
        return validate.isRequired();
    }
}
