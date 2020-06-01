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
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.FormSupportAdapter;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.HiddenFieldLocationRules;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * A Zone is portion of the output page designed for easy dynamic updating via Ajax or other client-side effects. A
 * Zone renders out as a &lt;div&gt; element (or whatever is specified in the template) and may have content initially,
 * or may only get its content as a result of client side activity.
 *
 * When a user clicks an {@link org.apache.tapestry5.corelib.components.ActionLink} whose zone parameter is set triggers a
 * series of client-side behaviors, and an Ajax request to the server.
 *
 * The server side event handler can return a {@link org.apache.tapestry5.Block} or a component to render as the new
 * content on the client side. Often, re-rendering the Zone's {@linkplain #getBody() body} is useful. Multiple
 * client-side zones may be updated via the {@link org.apache.tapestry5.services.ajax.AjaxResponseRenderer} service.
 *
 * You will often want to specify the id parameter of the Zone, in addition to its Tapestry component id; this "locks
 * down" the client-side id, so the same value is used even in later partial renders of the page (essential if the Zone
 * is nested inside another Zone). When you specify the client-side id, it is used exactly as provided (meaning that you
 * are responsible for ensuring that there will not be an id conflict even in the face of multiple partial renders of
 * the page). Failure to provide an explicit id results in a new, and non-predictable, id being generated for each
 * partial render, which will often result in client-side failures to locate the element to update when the Zone is
 * triggered.
 *
 * In some cases, you may want to know (on the server side) the client id of the zone that was updated; this is passed
 * as part of the Ajax request, as the {@link QueryParameterConstants#ZONE_ID} parameter. An example use of this would
 * be to provide new content into a Zone that updates the same Zone, when the Zone's client-side id is dynamically
 * allocated (rather than statically defined). In most cases, however, the programmer is responsible for assigning a
 * specific client-side id, via the id parameter.
 *
 * A Zone starts and stops a {@link Heartbeat} when it renders (both normally, and when re-rendering).
 *
 * After the client-side content is updated, a client-side event is fired on the zone's element. The constant
 * <code>core/events:zone.didUpdate</code> can be used to listen to the event.
 *
 * @tapestrydoc
 * @see AjaxFormLoop
 * @see FormFragment
 */
@SupportsInformalParameters
@Import(module = "t5/core/zone")
public class Zone implements ClientBodyElement
{
    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to make the Zone's
     * &lt;div&gt; visible before being updated. If not specified, then the basic "show" method is used.
     *
     * @deprecated In 5.4, with no specific replacement, now does nothing (see notes on client-side JavaScript events, elsewhere)
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String show;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked after the Zone's content has
     * been updated. If not specified, then the basic "highlight" method is used, which performs a classic "yellow fade"
     * to indicate to the user that and update has taken place.
     *
     * @deprecated In 5.4, with no specific replacement, now does nothing (see notes on client-side JavaScript events, elsewhere)
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String update;

    /**
     * The element name to render for the zone; this defaults to the element actually used in the template, or "div" if
     * no specific element was specified.
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private String elementName;

    /**
     * If bound, then the id attribute of the rendered element will be this exact value. If not bound, then a unique id
     * is generated for the element.
     */
    @Parameter(name = "id", defaultPrefix = BindingConstants.LITERAL)
    private String idParameter;

    @Environmental
    private JavaScriptSupport javascriptSupport;

    @Inject
    private Environment environment;

    /**
     * In prior releases, this parameter could be overridden to false to force the outer element of the rendered
     * Zone to be non-visible. This behavior is no longer supported.
     *
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    @Parameter
    private boolean visible;

    /**
     * if set to true, then Ajax updates related to this Zone will, when rending, use simple IDs (not namespaced ids).
     * This is useful when the Zone contains a simple Form, as it (hopefully) ensures that the same ids used when
     * initially rendering, and when processing the submission, are also used when re-rendering the Form (to present
     * errors to the user).  The default is false, maintaining the same behavior as in Tapestry 5.3 and earlier.
     *
     * @since 5.4
     */
    @Parameter
    private boolean simpleIds;

    @Inject
    private ComponentResources resources;

    @Inject
    private Heartbeat heartbeat;

    @Inject
    private Logger logger;

    @Inject
    private ClientDataEncoder clientDataEncoder;

    @Inject
    private HiddenFieldLocationRules rules;

    private String clientId;

    private boolean insideForm;

    private HiddenFieldPositioner hiddenFieldPositioner;

    private ComponentActionSink actionSink;

    @Environmental(false)
    private FormSupport formSupport;

    @Inject
    private DeprecationWarning deprecationWarning;

    @Inject
    @Symbol(SymbolConstants.COMPACT_JSON)
    private boolean compactJSON;

    String defaultElementName()
    {
        return resources.getElementName("div");
    }

    void pageLoaded()
    {
        deprecationWarning.ignoredComponentParameters(resources, "show", "update", "visible");
    }

    void beginRender(MarkupWriter writer)
    {
        clientId = resources.isBound("id") ? idParameter : javascriptSupport.allocateClientId(resources);

        Element e = writer.element(elementName,
                "id", clientId,
                "data-container-type", "zone");

        if (simpleIds)
        {
            e.attribute("data-simple-ids", "true");
        }

        resources.renderInformalParameters(writer);

        insideForm = formSupport != null;

        if (insideForm)
        {
            JSONObject parameters = new JSONObject(RequestConstants.FORM_CLIENTID_PARAMETER, formSupport.getClientId(),
                    RequestConstants.FORM_COMPONENTID_PARAMETER, formSupport.getFormComponentId());

            e.attribute("data-zone-parameters",
                    parameters.toString(compactJSON));

            hiddenFieldPositioner = new HiddenFieldPositioner(writer, rules);

            actionSink = new ComponentActionSink(logger, clientDataEncoder);

            environment.push(FormSupport.class, new FormSupportAdapter(formSupport)
            {
                @Override
                public <T> void store(T component, ComponentAction<T> action)
                {
                    actionSink.store(component, action);
                }

                @Override
                public <T> void storeCancel(T component, ComponentAction<T> action)
                {
                    actionSink.storeCancel(component, action);
                }

                @Override
                public <T> void storeAndExecute(T component, ComponentAction<T> action)
                {
                    store(component, action);

                    action.execute(component);
                }

            });
        }

        heartbeat.begin();
    }

    void afterRender(MarkupWriter writer)
    {
        heartbeat.end();

        if (insideForm)
        {
            environment.pop(FormSupport.class);

            if (actionSink.isEmpty())
            {
                hiddenFieldPositioner.discard();
            } else
            {
                hiddenFieldPositioner.getElement().attributes("type", "hidden",

                        "name", Form.FORM_DATA,

                        "value", actionSink.getClientData());
            }
        }

        writer.end(); // div
    }

    /**
     * The client id of the Zone; this is set when the Zone renders and will either be the value bound to the id
     * parameter, or an allocated unique id. When the id parameter is bound, this value is always accurate.
     * When the id parameter is not bound, the clientId is set during the {@linkplain BeginRender begin render phase}
     * and will be null or inaccurate before then.
     *
     * @return client-side element id
     */
    public String getClientId()
    {
        if (resources.isBound("id"))
            return idParameter;

        // TAP4-2342. I know this won't work with a Zone with no given clientId and that was already 
        // via AJAX inside an outer Zone, but it's still better than nothing.
        if (clientId == null)
        {
            clientId = resources.getId();
        }

        return clientId;
    }

    /**
     * Returns the zone's body (the content enclosed by its start and end tags). This is often used as part of an Ajax
     * partial page render to update the client with a fresh render of the content inside the zone.
     *
     * @return the zone's body as a Block
     */
    public Block getBody()
    {
        return resources.getBody();
    }
}
