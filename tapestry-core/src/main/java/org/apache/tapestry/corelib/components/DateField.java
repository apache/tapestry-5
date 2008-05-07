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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.Request;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A component used to collect a provided date from the user using a client-side JavaScript calendar. Non-JavaScript
 * clients can simply type into a text field..
 */
// TODO: More testing; see https://issues.apache.org/jira/browse/TAPESTRY-1844
@IncludeStylesheet("${tapestry.datepicker}/css/datepicker.css")
@IncludeJavaScriptLibrary({ "${tapestry.datepicker}/js/datepicker.js", "datefield.js" })
public class DateField extends AbstractField
{
    /**
     * The value parameter of a DateField must be a {@link Date}.
     */
    @Parameter(required = true, principal = true)
    private Date value;

    /**
     * The object that will perform input validation (which occurs after translation). The translate binding prefix is
     * generally used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = "validate")
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate = NOOP_VALIDATOR;

    @Parameter(defaultPrefix = TapestryConstants.ASSET_BINDING_PREFIX, value = "datefield.gif")
    private Asset icon;

    @Environmental
    private PageRenderSupport support;

    @Environmental
    private ValidationTracker tracker;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @Inject
    private Request request;

    @Inject
    private Locale locale;

    @Inject
    private FieldValidatorDefaultSource fieldValidatorDefaultSource;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    /**
     * For output, format nicely and unambiguously as four digits.
     */
    private final DateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * When the user types a value, they may only type two digits for the year; SimpleDateFormat will do something
     * reasonable.  If they use the popup, it will be unambiguously 4 digits.
     */
    private final DateFormat inputFormat = new SimpleDateFormat("MM/dd/yy");

    /**
     * The default value is a property of the container whose name matches the component's id. May return null if the
     * container does not have a matching property.
     */
    final Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    /**
     * Computes a default value for the "validate" parameter using {@link FieldValidatorDefaultSource}.
     */
    final FieldValidator defaultValidate()
    {

        return fieldValidatorDefaultSource.createDefaultValidator(this, resources.getId(),
                                                                  resources.getContainerMessages(), locale,
                                                                  Date.class,
                                                                  resources.getAnnotationProvider("value"));
    }

    void beginRender(MarkupWriter writer)
    {
        String value = tracker.getInput(this);

        if (value == null) value = formatCurrentValue();

        String clientId = getClientId();
        String triggerId = clientId + ":trigger";

        writer.element("input",

                       "type", "text",

                       "name", getControlName(),

                       "id", clientId,

                       "value", value);

        writeDisabled(writer);

        validate.render(writer);

        resources.renderInformalParameters(writer);

        decorateInsideField();

        writer.end();

        // Now the trigger icon.

        writer.element("img",

                       "id", triggerId,

                       "class", "t-calendar-trigger",

                       "src", icon.toClientURL(),

                       "alt", "[Show]");
        writer.end(); // img

        // The setup parameters passed to Calendar.setup():

        JSONObject setup = new JSONObject();

        setup.put("field", clientId);

        // TODO: consolodate DatePicker initialization across the page.

        support.addScript("new Tapestry.DateField(%s);", setup);
    }

    private void writeDisabled(MarkupWriter writer)
    {
        if (isDisabled()) writer.attributes("disabled", "disabled");
    }


    private String formatCurrentValue()
    {
        if (value == null) return "";

        return outputFormat.format(value);
    }

    @Override
    protected void processSubmission(String elementName)
    {
        String value = request.getParameter(elementName);

        tracker.recordInput(this, value);

        Date parsedValue = null;

        try
        {
            if (InternalUtils.isNonBlank(value))
                parsedValue =
                        inputFormat.parse(value);

        }
        catch (ParseException ex)
        {
            tracker.recordError(this, "Date value is not parseable.");
            return;
        }

        try
        {
            fieldValidationSupport.validate(parsedValue, resources, validate);

            this.value = parsedValue;
        }
        catch (ValidationException ex)
        {
            tracker.recordError(this, ex.getMessage());
        }
    }

    void injectResources(ComponentResources resources)
    {
        this.resources = resources;
    }

    void injectMessages(Messages messages)
    {
        this.messages = messages;
    }

    @Override
    public boolean isRequired()
    {
        return validate.isRequired();
    }
}
