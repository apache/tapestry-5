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
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.Request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A component used to collect a provided date from the user. This is a wrapper around the LGPL <a
 * href="http://www.dynarch.com/projects/calendar/">dynarch.com DHTML/JavaScript Calendar</a>.
 */
// TODO: More testing; see https://issues.apache.org/jira/browse/TAPESTRY-1844
public class DateField extends AbstractField
{
    /**
     * The value parameter of a DateField must be a {@link Date}.
     */
    @Parameter(required = true, principal = true)
    private Date _value;

    @Parameter(defaultPrefix = "literal")
    private String _format = "%m/%d/%y";

    /**
     * The object that will perform input validation (which occurs after translation). The translate
     * binding prefix is generally used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = "validate")
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> _validate = NOOP_VALIDATOR;


    /**
     * If true, then the client-side calendar will show the time as well as the date.  You will probably
     * need to bind the format parameter as well when this is true, say to <code>%m/%d/%y %H:%M</code>.
     */
    @Parameter
    private boolean _editTime;

    // We will eventually make the skins & themes more configurable.

    @Inject
    @Path("${tapestry.jscalendar}/skins/aqua/theme.css")
    private Asset _themeStylesheet;

    // Would be nice to use the stripped version when in production mode. Have to define "production
    // mode" first.
    @Inject
    @Path("${tapestry.jscalendar}/calendar.js")
    private Asset _mainScript;

    // Their naming convention isn't our naming convention, so we're locked to the english
    // version regardless of the application's current locale.

    @Inject
    @Path("${tapestry.jscalendar}/lang/calendar-en.js")
    private Asset _localizationScript;

    @Inject
    @Path("${tapestry.jscalendar}/calendar-setup.js")
    private Asset _setupScript;

    // TODO: Make this more configurable
    @Inject
    @Path("${tapestry.jscalendar}/img.gif")
    private Asset _defaultIcon;

    @Environmental
    private PageRenderSupport _support;

    @Environmental
    private ValidationTracker _tracker;

    @Inject
    private ComponentResources _resources;

    @Inject
    private Messages _messages;

    @Inject
    private Request _request;

    @Inject
    private Locale _locale;

    @Inject
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    /**
     * The default value is a property of the container whose name matches the component's id. May
     * return null if the container does not have a matching property.
     */
    final Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    /**
     * Computes a default value for the "validate" parameter using
     * {@link FieldValidatorDefaultSource}.
     */
    final FieldValidator defaultValidate()
    {

        return _fieldValidatorDefaultSource.createDefaultValidator(this, _resources.getId(),
                                                                   _resources.getContainerMessages(), _locale,
                                                                   Date.class,
                                                                   _resources.getAnnotationProvider("value"));
    }

    void beginRender(MarkupWriter writer)
    {
        _support.addStylesheetLink(_themeStylesheet, null);
        _support.addScriptLink(_mainScript, _localizationScript, _setupScript);

        String value = _tracker.getInput(this);

        if (value == null) value = formatCurrentValue();

        String clientId = getClientId();
        String triggerId = clientId + ":trigger";

        // TODO: Support a disabled parameter

        writer.element("input",

                       "type", "text",

                       "name", getElementName(),

                       "id", clientId,

                       "value", value);

        writeDisabled(writer);

        _validate.render(writer);

        _resources.renderInformalParameters(writer);

        decorateInsideField();

        writer.end();

        // Now the trigger icon.

        writer.element("button",

                       "class", "t-calendar-trigger",

                       "id", triggerId);

        writeDisabled(writer);


        writer.element("img",

                       "src", _defaultIcon.toClientURL(),

                       "alt", "[Show]");
        writer.end(); // img
        writer.end(); // button

        // The setup parameters passed to Calendar.setup():

        JSONObject setup = new JSONObject();

        setup.put("inputField", clientId);
        setup.put("ifFormat", _format);
        setup.put("button", triggerId);


        if (_editTime) setup.put("showsTime", true);

        // Let subclasses do more.

        configure(setup);

        _support.addScript("Calendar.setup(%s);", setup);
    }

    private void writeDisabled(MarkupWriter writer)
    {
        if (isDisabled()) writer.attributes("disabled", "disabled");
    }

    /**
     * Invoked to allow subclasses to further configure the parameters passed to the JavaScript
     * Calendar.setup() function. The values inputField, ifFormat and button are pre-configured.
     * Subclasses may override this method to configure additional features of the client-side
     * Calendar. This implementation does nothing.
     *
     * @param setup parameters object
     */
    protected void configure(JSONObject setup)
    {

    }

    String formatCurrentValue()
    {
        if (_value == null) return "";

        return toJavaDateFormat().format(_value);
    }

    @Override
    protected void processSubmission(String elementName)
    {
        // TODO: Validation

        String value = _request.getParameter(elementName);

        if (InternalUtils.isBlank(value))
        {
            _value = null;
            return;
        }

        try
        {
            _value = toJavaDateFormat().parse(value);

        }
        catch (ParseException ex)
        {
            _tracker.recordError(this, "Date value is not parseable.");
        }

    }

    SimpleDateFormat toJavaDateFormat()
    {
        String format = _format;

        StringBuilder builder = new StringBuilder();

        int startx = 0;

        while (true)
        {
            int nextx = format.indexOf('%', startx);

            if (nextx < 0)
            {
                builder.append(format.substring(startx));
                break;
            }

            builder.append(format.subSequence(startx, nextx));

            char ch = format.charAt(nextx + 1);

            String prefix = Character.isUpperCase(ch) ? "sym-up" : "sym-";

            String key = prefix + ch;

            if (!_messages.contains(key))
            {
                String message = _messages.format("unknown-symbol", ch, format);

                throw new TapestryException(message, _resources.getLocation(), null);
            }

            builder.append(_messages.get(key));

            startx = nextx + 2;
        }

        return new SimpleDateFormat(builder.toString());
    }

    void injectResources(ComponentResources resources)
    {
        _resources = resources;
    }

    void injectMessages(Messages messages)
    {
        _messages = messages;
    }

    void injectFormat(String format)
    {
        _format = format;
    }
}
