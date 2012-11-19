// Copyright 2007, 2008, 2009, 2010, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

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
 * The container is responsible for providing an event handler for event "providecompletions". The context will be the
 * partial input string sent from the client. The return value should be an array or list of completions, in
 * presentation order. I.e.
 * <p/>
 * <p/>
 * <pre>
 * String[] onProvideCompletionsFromMyField(String input)
 * {
 *   return . . .;
 * }
 * </pre>
 *
 * @tapestrydoc
 */
@Events(EventConstants.PROVIDE_COMPLETIONS)
public class Autocomplete
{
    static final String EVENT_NAME = "autocomplete";

    /**
     * The field component to which this mixin is attached.
     */
    @InjectContainer
    private Field field;

    @Inject
    private ComponentResources resources;

    @Environmental
    private JavaScriptSupport jsSupport;

    @Inject
    private TypeCoercer coercer;

    /**
     * Overwrites the default minimum characters to trigger a server round trip (the default is 1).
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private int minChars = 1;

    /**
     * Overrides the default check frequency for determining whether to send a server request. The default is .4
     * seconds.
     *
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private double frequency;

    /**
     * If given, then the autocompleter will support multiple input values, seperated by any of the individual
     * characters in the string.
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String tokens;

    @Inject
    private DeprecationWarning deprecationWarning;

    void pageLoaded()
    {
        deprecationWarning.ignoredComponentParameters(resources, "frequency", "tokens");
    }

    void afterRender()
    {
        Link link = resources.createEventLink(EVENT_NAME);

        JSONObject spec = new JSONObject("id", field.getClientId(),
                "url", link.toString()).put("minChars", minChars);

        jsSupport.require("core/autocomplete").with(spec);
    }

    Object onAutocomplete(@RequestParameter("t:input")
                          String input)
    {
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

        resources.triggerEvent(EventConstants.PROVIDE_COMPLETIONS, new Object[]
                {input}, callback);

        JSONObject reply = new JSONObject();

        reply.put("matches", JSONArray.from(matchesHolder.get()));

        // A JSONObject response is always preferred, as that triggers the whole partial page render pipeline.
        return reply;
    }
}
