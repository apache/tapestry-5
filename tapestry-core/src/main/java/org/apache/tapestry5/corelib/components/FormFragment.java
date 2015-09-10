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
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.FormSupportAdapter;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.corelib.mixins.TriggerFragment;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.HiddenFieldLocationRules;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * A FormFragment is a portion of a Form that may be selectively displayed. Form elements inside a FormFragment will
 * automatically bypass validation when the fragment is invisible. The trick is to also bypass server-side form
 * processing for such fields when the form is submitted; client-side logic "removes" the
 * {@link org.apache.tapestry5.corelib.components.Form#FORM_DATA form data} for the fragment if it is invisible when the
 * form is submitted (e.g., the hidden form field is disabled);
 * alternately, client-side logic can simply remove the form fragment element (including its visible and
 * hidden fields) to prevent server-side processing.
 *
 * The client-side element will now listen to two new events defined by client-side constants:
 * <dl>
 * <dt>core/events.formfragment.changeVisibility or Tapestry.CHANGE_VISIBILITY_EVENT</dt>
 * <dd>Change the visibility as per the event memo's visibility property. When the visibility changes, the correct
 * animation is executed.</dd>
 * <dt>core/events.formfragment.remove or Tapestry.HIDE_AND_REMOVE_EVENT</dt>
 * <dd>Hides the element, then removes it from the DOM entirely.
 * </dl>
 *
 * @tapestrydoc
 * @see TriggerFragment
 * @see Form
 */
@SupportsInformalParameters
@Import(module = "t5/core/form-fragment")
public class FormFragment implements ClientElement
{
    /**
     * Determines if the fragment is initially visible or initially invisible (the default). This is only used when
     * rendering; when the form is submitted, the hidden field value is used to determine whether the elements within
     * the fragment should be processed (or ignored if still invisible).
     */
    @Parameter
    private boolean visible;

    /**
     * If true, then the fragment submits the values from fields it contains <em>even if</em> the fragment is not
     * visible.
     * The default is to omit values from fields when the enclosing fragment is non visible.
     *
     * @since 5.2.0
     */
    @Parameter
    private boolean alwaysSubmit;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to make the fragment visible.
     * This is no longer used.
     *
     * @deprecated Deprecated in 5.4; clients that wish to animate should handle the <code>events.element.didShow</code> client-side event.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String show;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked when the fragment is to be
     * hidden.  This is no longer used.
     *
     * @deprecated Deprecated in 5.4; clients that wish to animate should handle the <code>events.element.didHide</code> client-side event.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String hide;

    /**
     * The element to render for each iteration of the loop. The default comes from the template, or "div" if the
     * template did not specific an element.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String element;

    /**
     * If bound, then the id attribute of the rendered element will be this exact value. If not bound, then a unique id
     * is generated for the element.
     */
    @Parameter(name = "id", defaultPrefix = BindingConstants.LITERAL)
    private String idParameter;

    /**
     * The name of a javascript function that overrides the default visibility search bound.
     * Tapestry normally ensures that not only the form fragment but all parent elements up to the containing body
     * are visible when determining whether to submit the contents of a form fragment.  This behavior can be modified by
     * supplying a javascript function that receives the "current" element in the chain.  Returning true will stop the
     * search (and report ElementWrapper.deepVisible() as true).  Returning false will continue the search up the chain.
     *
     * @since 5.3
     * @deprecated Deprecated in 5.4 with no current replacement.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL, allowNull = false)
    private String visibleBound;

    @Inject
    private Environment environment;

    @Environmental
    private JavaScriptSupport javascriptSupport;

    @Inject
    private ComponentResources resources;

    private String clientId;

    private ComponentActionSink componentActions;

    @Inject
    private Logger logger;

    @Inject
    private HiddenFieldLocationRules rules;

    private HiddenFieldPositioner hiddenFieldPositioner;

    @Inject
    private ClientDataEncoder clientDataEncoder;

    String defaultElement()
    {
        return resources.getElementName("div");
    }

    /**
     * Renders a &lt;div&gt; tag and provides an override of the {@link org.apache.tapestry5.services.FormSupport}
     * environmental.
     */
    void beginRender(MarkupWriter writer)
    {
        FormSupport formSupport = environment.peekRequired(FormSupport.class);

        String clientId = getClientId();

        hiddenFieldPositioner = new HiddenFieldPositioner(writer, rules);

        Element element = writer.element(this.element,
                "id", clientId,
                "data-component-type", "core/FormFragment");

        if (alwaysSubmit) {
            element.attribute("data-always-submit", "true");
        }

        resources.renderInformalParameters(writer);

        if (!visible)
        {
            element.attribute("style", "display: none;");

            if (!alwaysSubmit)
            {
                javascriptSupport.require("t5/core/form-fragment").invoke("hide").with(clientId);
            }
        }

        componentActions = new ComponentActionSink(logger, clientDataEncoder);

        // Here's the magic of environmentals ... we can create a wrapper around
        // the normal FormSupport environmental that intercepts some of the behavior.
        // Here we're setting aside all the actions inside the FormFragment so that we
        // can control whether those actions occur when the form is submitted.

        FormSupport override = new FormSupportAdapter(formSupport)
        {
            @Override
            public <T> void store(T component, ComponentAction<T> action)
            {
                componentActions.store(component, action);
            }

            @Override
            public <T> void storeCancel(T component, ComponentAction<T> action)
            {
                componentActions.storeCancel(component, action);
            }

            @Override
            public <T> void storeAndExecute(T component, ComponentAction<T> action)
            {
                componentActions.store(component, action);

                action.execute(component);
            }
        };

        // Tada! Now all the enclosed components will use our override of FormSupport,
        // until we pop it off.

        environment.push(FormSupport.class, override);

    }

    /**
     * Closes the &lt;div&gt; tag and pops off the {@link org.apache.tapestry5.services.FormSupport} environmental
     * override.
     *
     * @param writer
     */
    void afterRender(MarkupWriter writer)
    {
        Element hidden = hiddenFieldPositioner.getElement();

        hidden.attributes("type", "hidden",

                "name", Form.FORM_DATA,

                "value", componentActions.getClientData());

        writer.end(); // div


        environment.pop(FormSupport.class);

        resetClientId();
    }

    @HeartbeatDeferred
    void resetClientId()
    {
        clientId = null;
    }

    public String getClientId()
    {
        if (clientId == null)
        {
            clientId = resources.isBound("id") ? idParameter : javascriptSupport.allocateClientId(resources);
        }
        return clientId;
    }
}
