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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.CSSClassConstants;
import org.apache.tapestry5.ClientBodyElement;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ComponentParameterConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.QueryParameterConstants;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.FormSupportAdapter;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.HiddenFieldLocationRules;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * A Zone is portion of the output page designed for easy dynamic updating via Ajax or other client-side effects. A
 * Zone renders out as a &lt;div&gt; element (or whatever is specified in the template) and may have content initially,
 * or may only get its content as a result of client side activity.
 * <p/>
 * Often, Zones are initially invisible, in which case the visible parameter may be set to false (it defaults to true).
 * <p/>
 * When a user clicks an {@link org.apache.tapestry5.corelib.components.ActionLink} whose zone parameter is set, the
 * corresponding client-side Tapestry.ZoneManager object is located. It will update the content of the Zone's
 * &lt;div&gt; and then invoke either a show method (if the div is not visible) or an update method (if the div is
 * visible). The show and update parameters are the <em>names</em> of functions attached to the Tapestry.ElementEffect
 * object. Likewise, a {@link org.apache.tapestry5.corelib.components.Form} component may also trigger an update of a
 * client-side Zone.
 * <p/>
 * The server side event handler can return a {@link org.apache.tapestry5.Block} or a component to render as the new
 * content on the client side. Often, re-rendering the Zone's {@linkplain #getBody() body} is useful. Multiple
 * client-side zones may be updated by returning a {@link org.apache.tapestry5.ajax.MultiZoneUpdate}.
 * <p/>
 * Renders informal parameters, adding CSS class "t-zone" and possibly, "t-invisible".
 * <p/>
 * You will often want to specify the id parameter of the Zone, in addition to it's Tapestry component id; this "locks
 * down" the client-side id, so the same value is used even in later partial renders of the page (essential if the Zone
 * is nested inside another Zone). When you specify the client-side id, it is used exactly as provided (meaning that you
 * are responsible for ensuring that there will not be an id conflict even in the face of multiple partial renders of
 * the page). Failure to provide an explicit id results in a new, and non-predictable, id being generated for each
 * partial render, which will often result in client-side failures to locate the element to update when the Zone is
 * triggered.
 * <p>
 * In some cases, you may want to know (on the server side) the client id of the zone that was updated; this is passed
 * as part of the Ajax request, as the {@link QueryParameterConstants#ZONE_ID} parameter. An example use of this would
 * be to provide new content into a Zone that updates the same Zone, when the Zone's client-side id is dynamically
 * allocated (rather than statically defined). In most cases, however, the programmer is responsible for assigning a
 * specific client-side id, via the id parameter.
 * <p/>
 * A Zone starts and stops a {@link Heartbeat} when it renders (both normally, and when re-rendering).
 * <p/>
 * After the client-side content is updated, a client-side event is fired on the zone's element. The constant
 * Tapestry.ZONE_UPDATED_EVENT can be used to listen to the event.
 * 
 * @tapestrydoc
 * @see AjaxFormLoop
 * @see FormFragment
 */
@SupportsInformalParameters
public class Zone implements ClientBodyElement
{
    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to make the Zone's
     * &lt;div&gt; visible before being updated. If not specified, then the basic "show" method is used.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL,
        value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.ZONE_SHOW_METHOD)
    private String show;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked after the Zone's content has
     * been updated. If not specified, then the basic "highlight" method is used, which performs a classic "yellow fade"
     * to indicate to the user that and update has taken place.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL,
        value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.ZONE_UPDATE_METHOD)
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

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    @Inject
    private Environment environment;

    /**
     * If true (the default) then the zone will render normally. If false, then the "t-invisible" CSS class is added,
     * which will make the zone initially invisible.
     */
    @Parameter
    private boolean visible = true;

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

    String defaultElementName()
    {
        return resources.getElementName("div");
    }

    void beginRender(MarkupWriter writer)
    {
        clientId = resources.isBound("id") ? idParameter : javascriptSupport.allocateClientId(resources);

        Element e = writer.element(elementName, "id", clientId);

        resources.renderInformalParameters(writer);

        e.addClassName("t-zone");

        if (!visible)
            e.addClassName(CSSClassConstants.INVISIBLE);

        clientBehaviorSupport.addZone(clientId, show, update);

        FormSupport existingFormSupport = environment.peek(FormSupport.class);

        insideForm = existingFormSupport != null;

        if (insideForm)
        {
            hiddenFieldPositioner = new HiddenFieldPositioner(writer, rules);

            actionSink = new ComponentActionSink(logger, clientDataEncoder);

            environment.push(FormSupport.class, new FormSupportAdapter(existingFormSupport)
            {
                @Override
                public <T> void store(T component, ComponentAction<T> action)
                {
                    actionSink.store(component, action);
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
            }
            else
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
