// Copyright 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A component used to collect a provided date from the user using a client-side JavaScript calendar. Non-JavaScript
 * clients can simply type into a text field.
 * <p/>
 * One wierd aspect here is that, because client-side JavaScript formatting and parsing is so limited, we (currently)
 * use Ajax to send the user's input to the server for parsing (before raising the popup) and formatting (after closing
 * the popup). Weird and inefficient, but easier than writing client-side JavaScript for that purpose.
 * <p/>
 * Tapestry's DateField component is a wrapper around <a
 * href="http://webfx.eae.net/dhtml/datepicker/datepicker.html">WebFX DatePicker</a>.
 *
 * @tapestrydoc
 * @see Form
 * @see TextField
 */
// TODO: More testing; see https://issues.apache.org/jira/browse/TAPESTRY-1844
@Import(stack = "core-datefield")
@Events(EventConstants.VALIDATE)
public class DateField extends AbstractField
{
    /**
     * The value parameter of a DateField must be a {@link java.util.Date}.
     */
    @Parameter(required = true, principal = true, autoconnect = true)
    private Date value;

    /**
     * The format used to format <em>and parse</em> dates. This is typically specified as a string which is coerced to a
     * DateFormat. You should be aware that using a date format with a two digit year is problematic: Java (not
     * Tapestry) may get confused about the century.
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private DateFormat format;

    /**
     * If true, then the text field will be hidden, and only the icon for the date picker will be visible. The default
     * is false.
     */
    @Parameter
    private boolean hideTextField;

    /**
     * The object that will perform input validation (which occurs after translation). The translate binding prefix is
     * generally used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate;

    @Parameter(defaultPrefix = BindingConstants.ASSET, value = "datefield.gif")
    private Asset icon;

    /**
     * Used to override the component's message catalog.
     *
     * @since 5.2.0.0
     */
    @Parameter("componentResources.messages")
    private Messages messages;

    @Environmental
    private JavaScriptSupport support;

    @Environmental
    private ValidationTracker tracker;

    @Inject
    private ComponentResources resources;

    @Inject
    private Request request;

    @Inject
    private Locale locale;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    private static final String RESULT = "result";

    private static final String ERROR = "error";
    private static final String INPUT_PARAMETER = "input";

    DateFormat defaultFormat()
    {
        DateFormat shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        if (shortDateFormat instanceof SimpleDateFormat)
        {
            SimpleDateFormat simpleDateFormat = (SimpleDateFormat) shortDateFormat;

            String pattern = simpleDateFormat.toPattern();

            String revised = pattern.replaceAll("([^y])yy$", "$1yyyy");

            return new SimpleDateFormat(revised);
        }

        return shortDateFormat;
    }

    /**
     * Computes a default value for the "validate" parameter using {@link ComponentDefaultProvider}.
     */
    final Binding defaultValidate()
    {
        return defaultProvider.defaultValidatorBinding("value", resources);
    }

    /**
     * Ajax event handler, used when initiating the popup. The client sends the input value form the field to the server
     * to parse it according to the server-side format. The response contains a "result" key of the formatted date in a
     * format acceptable to the JavaScript Date() constructor. Alternately, an "error" key indicates the the input was
     * not formatted correct.
     */
    JSONObject onParse(@RequestParameter(INPUT_PARAMETER)
                       String input)
    {
        JSONObject response = new JSONObject();

        try
        {
            Date date = format.parse(input);

            response.put(RESULT, date.getTime());
        } catch (ParseException ex)
        {
            response.put(ERROR, ex.getMessage());
        }

        return response;
    }

    /**
     * Ajax event handler, used after the client-side popup completes. The client sends the date, formatted as
     * milliseconds since the epoch, to the server, which reformats it according to the server side format and returns
     * the result.
     */
    JSONObject onFormat(@RequestParameter(INPUT_PARAMETER)
                        String input)
    {
        JSONObject response = new JSONObject();

        try
        {
            long millis = Long.parseLong(input);

            Date date = new Date(millis);

            response.put(RESULT, format.format(date));
        } catch (NumberFormatException ex)
        {
            response.put(ERROR, ex.getMessage());
        }

        return response;
    }

    void beginRender(MarkupWriter writer)
    {
        String value = tracker.getInput(this);

        if (value == null)
            value = formatCurrentValue();

        String clientId = getClientId();
        String triggerId = clientId + "-trigger";

        writer.element("input",

                "type", hideTextField ? "hidden" : "text",

                "name", getControlName(),

                "id", clientId,

                "value", value);

        writeDisabled(writer);

        putPropertyNameIntoBeanValidationContext("value");

        validate.render(writer);

        removePropertyNameFromBeanValidationContext();

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

        JSONObject spec = new JSONObject();

        spec.put("field", clientId);
        spec.put("parseURL", resources.createEventLink("parse").toURI());
        spec.put("formatURL", resources.createEventLink("format").toURI());

        support.addInitializerCall("dateField", spec);
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

        return format.format(value);
    }

    @Override
    protected void processSubmission(String controlName)
    {
        String value = request.getParameter(controlName);

        tracker.recordInput(this, value);

        Date parsedValue = null;

        try
        {
            if (InternalUtils.isNonBlank(value))
                parsedValue = format.parse(value);
        } catch (ParseException ex)
        {
            tracker.recordError(this, messages.format("date-value-not-parseable", value));
            return;
        }

        putPropertyNameIntoBeanValidationContext("value");
        try
        {
            fieldValidationSupport.validate(parsedValue, resources, validate);

            this.value = parsedValue;
        } catch (ValidationException ex)
        {
            tracker.recordError(this, ex.getMessage());
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
