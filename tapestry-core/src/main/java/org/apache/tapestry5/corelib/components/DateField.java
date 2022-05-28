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
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A component used to collect a provided date from the user using a client-side JavaScript calendar. Non-JavaScript
 * clients can simply type into a text field.
 *
 * One aspect here is that, because client-side JavaScript formatting and parsing is so limited, we (currently)
 * use Ajax to send the user's input to the server for parsing (before raising the popup) and formatting (after closing
 * the popup). Weird and inefficient, but easier than writing client-side JavaScript for that purpose.
 *
 * Tapestry's DateField component is a wrapper around <a
 * href="http://webfx.eae.net/dhtml/datepicker/datepicker.html">WebFX DatePicker</a>.
 *
 * @tapestrydoc
 * @see Form
 * @see TextField
 */
// TODO: More testing; see https://issues.apache.org/jira/browse/TAPESTRY-1844
@Import(stylesheet = "${tapestry.datepicker}/css/datepicker.css",
        module = "t5/core/datefield")
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
     * Allows the type of field to be output; normally this is "text", but can be updated to "date" or "datetime"
     * as per the HTML 5 specification.
     *
     * @since 5.4
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL, value = "text")
    private String type;

    /**
     * When the <code>format</code> parameter isn't used, this parameter defines whether the
     * <code>DateFormat</code> created by this component will be lenient or not.
     * The default value of this parameter is the value of the {@link SymbolConstants#LENIENT_DATE_FORMAT}
     * symbol.
     *
     * @see DateFormat#setLenient(boolean)
     * @see SymbolConstants#LENIENT_DATE_FORMAT
     * @since 5.4
     */
    @Parameter(principal = true)
    private boolean lenient;

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

    /**
     * Icon used for the date field trigger button. This was used in Tapestry 5.3 and earlier and is now ignored.
     *
     * @deprecated Deprecated in 5.4 with no replacement. The component leverages the Twitter Bootstrap glyphicons support.
     */
    @Parameter(defaultPrefix = BindingConstants.ASSET)
    private Asset icon;

    /**
     * Used to override the component's message catalog.
     *
     * @since 5.2.0.0
     * @deprecated Since 5.4; override the global message key "core-date-value-not-parsable" instead (see {@link org.apache.tapestry5.services.messages.ComponentMessagesSource})
     */
    @Parameter("componentResources.messages")
    private Messages messages;

    @Inject
    private Locale locale;

    @Inject
    private DeprecationWarning deprecationWarning;

    @Inject
    @Symbol(SymbolConstants.LENIENT_DATE_FORMAT)
    private boolean lenientDateFormatSymbolValue;

    private static final String RESULT = "result";

    private static final String ERROR = "error";
    private static final String INPUT_PARAMETER = "input";

    void pageLoaded()
    {
        deprecationWarning.ignoredComponentParameters(resources, "icon");
    }

    DateFormat defaultFormat()
    {
        DateFormat shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        if (shortDateFormat instanceof SimpleDateFormat)
        {
            SimpleDateFormat simpleDateFormat = (SimpleDateFormat) shortDateFormat;

            String pattern = simpleDateFormat.toPattern();

            String revised = pattern.replaceAll("([^y])yy$", "$1yyyy");

            final SimpleDateFormat revisedDateFormat = new SimpleDateFormat(revised);
            revisedDateFormat.setLenient(lenient);
            return revisedDateFormat;
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

    final boolean defaultLenient()
    {
        return lenientDateFormatSymbolValue;
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

            response.put(RESULT, new SimpleDateFormat("yyyy-MM-dd").format(date));
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
     * @throws ParseException
     */
    JSONObject onFormat(@RequestParameter(INPUT_PARAMETER)
                        String input) throws ParseException
    {
        JSONObject response = new JSONObject();

        try
        {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(input);

            response.put(RESULT, format.format(date));
        } catch (NumberFormatException ex)
        {
            response.put(ERROR, ex.getMessage());
        }

        return response;
    }

    void beginRender(MarkupWriter writer)
    {
        String value = validationTracker.getInput(this);

        if (value == null)
        {
            value = formatCurrentValue();
        }

        String clientId = getClientId();

        writer.element("div",
                "data-component-type", "core/DateField",
                "data-parse-url", resources.createEventLink("parse").toString(),
                "data-format-url", resources.createEventLink("format").toString());

        if (!hideTextField)
        {
            writer.attributes("class", "input-group");
        }

        Element field = writer.element("input",

                "type", type,

                "class", cssClass,

                "name", getControlName(),

                "id", clientId,

                "value", value);

        if (hideTextField)
        {
            field.attribute("class", "hide");
        }

        writeDisabled(writer);

        putPropertyNameIntoBeanValidationContext("value");

        validate.render(writer);

        removePropertyNameFromBeanValidationContext();

        resources.renderInformalParameters(writer);

        decorateInsideField();

        writer.end();   // input

        if (!hideTextField)
        {
            writer.element("span", "class", "input-group-btn");
        }

        writer.element("button",
                "type", "button",
                "class", "btn btn-default",
                "alt", "[Show]");

        writer.element("span", "class", "glyphicon glyphicon-calendar");
        writer.end(); // span

        writer.end(); // button

        if (!hideTextField)
        {
            writer.end();        // span.input-group-btn
        }

        writer.end(); // outer div
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

        validationTracker.recordInput(this, value);

        Date parsedValue = null;

        try
        {
            if (InternalUtils.isNonBlank(value))
                parsedValue = format.parse(value);
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
