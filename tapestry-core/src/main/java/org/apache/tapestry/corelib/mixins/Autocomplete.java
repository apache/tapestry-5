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

package org.apache.tapestry.corelib.mixins;

import org.apache.tapestry.*;
import org.apache.tapestry.ContentType;
import org.apache.tapestry.annotations.*;
import org.apache.tapestry.internal.services.ResponseRenderer;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.util.TextStreamResponse;

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
@IncludeJavaScriptLibrary("${tapestry.scriptaculous}/controls.js")
public class Autocomplete
{
    static final String EVENT_NAME = "autocomplete";

    private static final String PARAM_NAME = "t:input";

    /**
     * The field component to which this mixin is attached.
     */
    @InjectContainer
    private Field _field;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private PageRenderSupport _pageRenderSupport;

    @Inject
    private Request _request;

    @Inject
    private TypeCoercer _coercer;

    @Inject
    private MarkupWriterFactory _factory;

    @Inject
    @Path("classpath:org/apache/tapestry/ajax-loader.gif")
    private Asset _loader;

    /**
     * Overwrites the default minimum characters to trigger a server round trip (the default is 1).
     */
    @Parameter(defaultPrefix = "literal")
    private int _minChars;

    @Inject
    private ResponseRenderer _responseRenderer;


    /**
     * Overrides the default check frequency for determining whether to send a server request. The default is .4
     * seconds.
     */
    @Parameter(defaultPrefix = "literal")
    private double _frequency;

    /**
     * If given, then the autocompleter will support multiple input values, seperated by any of the individual
     * characters in the string.
     */
    @Parameter(defaultPrefix = "literal")
    private String _tokens;

    /**
     * Mixin afterRender phrase occurs after the component itself. This is where we write the &lt;div&gt; element and
     * the JavaScript.
     *
     * @param writer
     */
    void afterRender(MarkupWriter writer)
    {
        String id = _field.getClientId();

        String menuId = id + ":menu";
        String loaderId = id + ":loader";

        // This image is made visible while the request is being processed.
        // To be honest, I think Prototype hides it too soon, it should wait
        // until the <div> is fully positioned and updated.

        writer.element("img",

                       "src", _loader.toClientURL(),

                       "class", TapestryConstants.INVISIBLE_CLASS,

                       "id", loaderId);
        writer.end();

        writer.element("div",

                       "id", menuId,

                       "class", "t-autocomplete-menu");
        writer.end();

        Link link = _resources.createActionLink(EVENT_NAME, false);


        JSONObject config = new JSONObject();
        config.put("paramName", PARAM_NAME);
        config.put("indicator", loaderId);

        if (_resources.isBound("minChars")) config.put("minChars", _minChars);

        if (_resources.isBound("frequency")) config.put("frequency", _frequency);

        if (_resources.isBound("tokens"))
        {
            for (int i = 0; i < _tokens.length(); i++)
            {
                config.accumulate("tokens", _tokens.substring(i, i + 1));
            }
        }

        // Let subclasses do more.
        configure(config);

        _pageRenderSupport.addScript("new Ajax.Autocompleter('%s', '%s', '%s', %s);", id, menuId, link.toAbsoluteURI(),
                                     config);
    }

    Object onAutocomplete()
    {
        String input = _request.getParameter(PARAM_NAME);

        final Holder<List> matchesHolder = Holder.create();

        // Default it to an empty list.

        matchesHolder.put(Collections.emptyList());

        ComponentEventCallback callback = new ComponentEventCallback()
        {
            public boolean handleResult(Object result)
            {
                List matches = _coercer.coerce(result, List.class);

                matchesHolder.put(matches);

                return true;
            }
        };

        _resources.triggerEvent("providecompletions", new Object[] { input }, callback);

        ContentType contentType = _responseRenderer.findContentType(this);

        MarkupWriter writer = _factory.newMarkupWriter(contentType);

        generateResponseMarkup(writer, matchesHolder.get());

        return new TextStreamResponse(contentType.getMimeType(), writer.toString());
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
