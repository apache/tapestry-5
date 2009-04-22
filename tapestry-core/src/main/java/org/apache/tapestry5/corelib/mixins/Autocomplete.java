// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.*;
import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.MarkupWriterFactory;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ResponseRenderer;
import org.apache.tapestry5.util.TextStreamResponse;

import java.util.Collections;
import java.util.List;

/**
 * A mixin for a text field that allows for autocompletion of text fields. This is based on Prototype's autocompleter
 * control.
 * <p/>
 * The mixin renders an (initially invisible) progress indicator after the field (it will also be after the error icon
 * most fields render). The progress indicator is made visible during the request to the server. The mixin then renders
 * a &lt;div&gt; that will be filled in on the client side with dynamically obtained selections.
 * <p/>
 * Multiple selection on the client is enabled by binding the tokens parameter (however, the mixin doesn't help split
 * multiple selections up on the server, that is still your code's responsibility).
 * <p/>
 * The container is responsible for providing an event handler for event "providecompletions".  The context will be the
 * partial input string sent from the client.  The return value should be an array or list of completions, in
 * presentation order.  I.e.
 * <p/>
 * <pre>
 * String[] onProvideCompletionsFromMyField(String input)
 * {
 *   return . . .;
 * }
 * </pre>
 */
@IncludeJavaScriptLibrary({ "${tapestry.scriptaculous}/controls.js", "autocomplete.js" })
@Events(EventConstants.PROVIDE_COMPLETIONS)
public class Autocomplete
{
    static final String EVENT_NAME = "autocomplete";

    private static final String PARAM_NAME = "t:input";

    /**
     * The field component to which this mixin is attached.
     */
    @InjectContainer
    private Field field;

    @Inject
    private ComponentResources resources;

    @Environmental
    private RenderSupport renderSupport;

    @Inject
    private Request request;

    @Inject
    private TypeCoercer coercer;

    @Inject
    private MarkupWriterFactory factory;

    @Inject
    @Path("${tapestry.spacer-image}")
    private Asset spacerImage;

    /**
     * Overwrites the default minimum characters to trigger a server round trip (the default is 1).
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private int minChars;

    @Inject
    private ResponseRenderer responseRenderer;


    /**
     * Overrides the default check frequency for determining whether to send a server request. The default is .4
     * seconds.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private double frequency;

    /**
     * If given, then the autocompleter will support multiple input values, seperated by any of the individual
     * characters in the string.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String tokens;

    /**
     * Mixin afterRender phrase occurs after the component itself. This is where we write the &lt;div&gt; element and
     * the JavaScript.
     *
     * @param writer
     */
    void afterRender(MarkupWriter writer)
    {
        String id = field.getClientId();

        String menuId = id + ":menu";
        String loaderId = id + ":loader";

        // The spacer image is used as a placeholder, allowing CSS to determine what image
        // is actually displayed.

        writer.element("img",

                       "src", spacerImage.toClientURL(),

                       "class", "t-autoloader-icon " + CSSClassConstants.INVISIBLE,

                       "alt", "",

                       "id", loaderId);
        writer.end();

        writer.element("div",

                       "id", menuId,

                       "class", "t-autocomplete-menu");
        writer.end();

        Link link = resources.createEventLink(EVENT_NAME);


        JSONObject config = new JSONObject();
        config.put("paramName", PARAM_NAME);
        config.put("indicator", loaderId);

        if (resources.isBound("minChars")) config.put("minChars", minChars);

        if (resources.isBound("frequency")) config.put("frequency", frequency);

        if (resources.isBound("tokens"))
        {
            for (int i = 0; i < tokens.length(); i++)
            {
                config.accumulate("tokens", tokens.substring(i, i + 1));
            }
        }

        // Let subclasses do more.
        configure(config);

        renderSupport.addInit("autocompleter", new JSONArray(id, menuId, link.toAbsoluteURI(), config));
    }

    Object onAutocomplete()
    {
        String input = request.getParameter(PARAM_NAME);

        final Holder<List> matchesHolder = Holder.create();

        // Default it to an empty list.

        matchesHolder.put(Collections.emptyList());

        ComponentEventCallback callback = new ComponentEventCallback()
        {
            public boolean handleResult(Object result)
            {
                List matches = coercer.coerce(result, List.class);

                matchesHolder.put(matches);

                return true;
            }
        };

        resources.triggerEvent(EventConstants.PROVIDE_COMPLETIONS, new Object[] { input }, callback);

        ContentType contentType = responseRenderer.findContentType(this);

        MarkupWriter writer = factory.newPartialMarkupWriter(contentType);

        generateResponseMarkup(writer, matchesHolder.get());

        return new TextStreamResponse(contentType.toString(), writer.toString());
    }

    /**
     * Invoked to allow subclasses to further configure the parameters passed to the JavaScript Ajax.Autocompleter
     * options. The values minChars, frequency and tokens my be pre-configured. Subclasses may override this method to
     * configure additional features of the Ajax.Autocompleter.
     * <p/>
     * <p/>
     * This implementation does nothing.
     *
     * @param config parameters object
     */
    protected void configure(JSONObject config)
    {
    }

    /**
     * Generates the markup response that will be returned to the client; this should be an &lt;ul&gt; element with
     * nested &lt;li&gt; elements. Subclasses may override this to produce more involved markup (including images and
     * CSS class attributes).
     *
     * @param writer  to write the list to
     * @param matches list of matching objects, each should be converted to a string
     */
    protected void generateResponseMarkup(MarkupWriter writer, List matches)
    {
        writer.element("ul");

        for (Object o : matches)
        {
            writer.element("li");
            writer.write(o.toString());
            writer.end();
        }

        writer.end(); // ul
    }
}
