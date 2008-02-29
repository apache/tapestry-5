// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.corelib.internal.FormSupportAdapter;
import org.apache.tapestry.corelib.internal.WrappedComponentAction;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.services.ClientBehaviorSupport;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentSource;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Request;

import java.util.List;

/**
 * A SubForm is a portion of a Form that may be selectively displayed.  Form elements inside a FormFragment
 * will automatically bypass validation when the fragment is invisible.  The trick is to also bypass server-side
 * form processing for such fields when the form is submitted; the fragment uses a hidden field
 * to track its client-side visibility and will bypass field component submission logic for
 * the components it encloses.
 *
 * @see org.apache.tapestry.corelib.mixins.TriggerFragment
 */
@SupportsInformalParameters
public class FormFragment implements ClientElement
{
    /**
     * Determines if the fragment is intially visible or initially invisible (the default). This is
     * only used when rendering; when the form is submitted, the hidden field value
     * is used to determine whether the elements within the fragment should be processed (or ignored
     * if still invisible).
     */
    @Parameter
    private boolean _visible;


    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to
     * make the fragment  visible.  If not specified, then
     * the default "slidedown" function is used.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String _show;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked
     * when the fragment is to be hidden. If not specified, the default "slideup" function is used.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String _hide;

    @Inject
    private Environment _environment;

    @Environmental
    private PageRenderSupport _pageRenderSupport;


    @Inject
    private ComponentSource _componentSource;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private ClientBehaviorSupport _clientBehaviorSupport;

    private String _clientId;

    private String _controlName;

    private List<WrappedComponentAction> _componentActions;

    @Inject
    private Request _request;

    static class HandleSubmission implements ComponentAction<FormFragment>
    {
        private final String _controlName;

        private final List<WrappedComponentAction> _actions;

        public HandleSubmission(String controlName, List<WrappedComponentAction> actions)
        {
            _controlName = controlName;
            _actions = actions;
        }

        public void execute(FormFragment component)
        {
            component.handleSubmission(_controlName, _actions);
        }
    }


    private void handleSubmission(String elementName, List<WrappedComponentAction> actions)
    {
        String value = _request.getParameter(elementName);

        boolean visible = Boolean.parseBoolean(value);

        if (!visible) return;

        // Note that we DON'T update the visible parameter, it is read only.

        for (WrappedComponentAction action : actions)
        {
            action.execute(_componentSource);
        }
    }

    /**
     * Renders a &lt;div&gt; tag and provides an override of the {@link org.apache.tapestry.services.FormSupport} environmental.
     */
    void beginRender(MarkupWriter writer)
    {
        FormSupport formSupport = _environment.peekRequired(FormSupport.class);

        String id = _resources.getId();

        _controlName = formSupport.allocateControlName(id);
        _clientId = _pageRenderSupport.allocateClientId(id);

        Element element = writer.element("div", "id", _clientId);

        _resources.renderInformalParameters(writer);

        if (!_visible)
            element.addClassName(TapestryConstants.INVISIBLE_CLASS);


        writer.element("input",

                       "type", "hidden",

                       "name", _controlName,

                       "id", _clientId + ":hidden",

                       "value", String.valueOf(_visible));
        writer.end();


        _clientBehaviorSupport.addFormFragment(_clientId, _show, _hide);

        _componentActions = CollectionFactory.newList();

        // Here's the magic of environmentals ... we can create a wrapper around
        // the normal FormSupport environmental that intercepts some of the behavior.
        // Here we're setting aside all the actions inside the FormFragment so that we
        // can control whether those actions occur when the form is submitted.

        FormSupport override = new FormSupportAdapter(formSupport)
        {
            @Override
            public <T> void store(T component, ComponentAction<T> action)
            {
                Component asComponent = Defense.cast(component, Component.class, "component");

                _componentActions.add(new WrappedComponentAction(asComponent, action));
            }

            @Override
            public <T> void storeAndExecute(T component, ComponentAction<T> action)
            {
                store(component, action);

                action.execute(component);
            }
        };

        // Tada!  Now all the enclosed components will use our override of FormSupport,
        // until we pop it off.

        _environment.push(FormSupport.class, override);

    }

    /**
     * Closes the &lt;div&gt; tag and pops off the {@link org.apache.tapestry.services.FormSupport} environmental override.
     *
     * @param writer
     */
    void afterRender(MarkupWriter writer)
    {
        writer.end(); // div

        _environment.pop(FormSupport.class);

        _environment.peek(FormSupport.class).store(this, new HandleSubmission(_controlName, _componentActions));
    }

    public String getClientId()
    {
        return _clientId;
    }
}
