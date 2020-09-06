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
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.corelib.internal.AjaxFormLoopContext;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.Collections;
import java.util.Iterator;

/**
 * A special form of the {@link org.apache.tapestry5.corelib.components.Loop}
 * component that adds Ajax support to handle adding new rows and removing
 * existing rows dynamically.
 *
 * This component expects that the values being iterated over are entities that
 * can be identified via a {@link org.apache.tapestry5.ValueEncoder}, therefore
 * you must either bind the "encoder" parameter to a ValueEncoder or use an
 * entity type for the "value" parameter for which Tapestry can provide a
 * ValueEncoder automatically.
 *
 * Works with {@link org.apache.tapestry5.corelib.components.AddRowLink} and
 * {@link org.apache.tapestry5.corelib.components.RemoveRowLink} components.
 *
 * The addRow event will receive the context specified by the context parameter.
 *
 * The removeRow event will receive the client-side value for the row being iterated.
 *
 * @tapestrydoc
 * @see EventConstants#ADD_ROW
 * @see EventConstants#REMOVE_ROW
 * @see AddRowLink
 * @see RemoveRowLink
 * @see Loop
 */
@Events(
        {EventConstants.ADD_ROW, EventConstants.REMOVE_ROW})
@Import(module = "t5/core/ajaxformloop")
@SupportsInformalParameters
public class AjaxFormLoop
{
    /**
     * The element to render for each iteration of the loop. The default comes from the template, or "div" if the
     * template did not specify an element.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    @Property(write = false)
    private String element;

    /**
     * The objects to iterate over (passed to the internal Loop component).
     */
    @Parameter(required = true, autoconnect = true)
    private Iterable source;

    /**
     * The current value from the source.
     */
    @Parameter(required = true)
    private Object value;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to make added content
     * visible. This was used by the FormInjector component (remove in 5.4), when adding a new row to the loop. Leaving as
     * null uses the default function, "highlight".
     *
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String show;

    /**
     * The context for the form loop (optional parameter). This list of values will be converted into strings and
     * included in the URI. The strings will be coerced back to whatever their values are and made available to event
     * handler methods. Note that the context is only encoded and available to the {@linkplain EventConstants#ADD_ROW addRow}
     * event; for the {@linkplain EventConstants#REMOVE_ROW} event, the context passed to event handlers
     * is simply the decoded value for the row that is to be removed.
     */
    @Parameter
    private Object[] context;

    /**
     * A block to render after the loo
     * This typically contains a {@link org.apache.tapestry5.corelib.components.AddRowLink}.
     */
    @Parameter(value = "block:defaultAddRow", defaultPrefix = BindingConstants.LITERAL)
    @Property(write = false)
    private Block addRow;

    /**
     * The block that contains the form injector (it is rendered last, as the "tail" of the AjaxFormLoop). This, in
     * turn, references the addRow block (from a parameter, or a default).
     */
    @Inject
    private Block tail;

    /**
     * A ValueEncoder used to convert server-side objects (provided by the
     * "source" parameter) into unique client-side strings (typically IDs) and
     * back. Note: this parameter may be OMITTED if Tapestry is configured to
     * provide a ValueEncoder automatically for the type of property bound to
     * the "value" parameter.
     */
    @Parameter(required = true, allowNull = false)
    private ValueEncoder<Object> encoder;

    @InjectComponent
    private FormFragment fragment;

    @Inject
    private Block ajaxResponse;

    @Inject
    private ComponentResources resources;

    @Environmental
    private FormSupport formSupport;

    @Environmental
    private Heartbeat heartbeat;

    @Inject
    private Environment environment;

    @Inject
    private JavaScriptSupport jsSupport;

    private Iterator iterator;

    private Element wrapper;

    @Inject
    private TypeCoercer typeCoercer;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    private DeprecationWarning deprecationWarning;

    void pageLoaded()
    {
        deprecationWarning.ignoredComponentParameters(resources, "show");
    }

    ValueEncoder defaultEncoder()
    {
        return defaultProvider.defaultValueEncoder("value", resources);
    }

    private final AjaxFormLoopContext formLoopContext = new AjaxFormLoopContext()
    {
        public String encodedRowValue()
        {
            return encoder.toClient(value);
        }
    };

    String defaultElement()
    {
        return resources.getElementName("div");
    }

    /**
     * Action for synchronizing the current element of the loop by recording its client value.
     */
    static class SyncValue implements ComponentAction<AjaxFormLoop>
    {
        private final String clientValue;

        public SyncValue(String clientValue)
        {
            this.clientValue = clientValue;
        }

        public void execute(AjaxFormLoop component)
        {
            component.syncValue(clientValue);
        }

        @Override
        public String toString()
        {
            return String.format("AjaxFormLoop.SyncValue[%s]", clientValue);
        }
    }

    private static final ComponentAction<AjaxFormLoop> BEGIN_HEARTBEAT = new ComponentAction<AjaxFormLoop>()
    {
        public void execute(AjaxFormLoop component)
        {
            component.beginHeartbeat();
        }

        @Override
        public String toString()
        {
            return "AjaxFormLoop.BeginHeartbeat";
        }
    };

    @Property(write = false)
    private final Renderable beginHeartbeat = new Renderable()
    {
        public void render(MarkupWriter writer)
        {
            formSupport.storeAndExecute(AjaxFormLoop.this, BEGIN_HEARTBEAT);
        }
    };

    private static final ComponentAction<AjaxFormLoop> END_HEARTBEAT = new ComponentAction<AjaxFormLoop>()
    {
        public void execute(AjaxFormLoop component)
        {
            component.endHeartbeat();
        }

        @Override
        public String toString()
        {
            return "AjaxFormLoop.EndHeartbeat";
        }
    };

    @Property(write = false)
    private final Renderable endHeartbeat = new Renderable()
    {
        public void render(MarkupWriter writer)
        {
            formSupport.storeAndExecute(AjaxFormLoop.this, END_HEARTBEAT);
        }
    };

    @Property(write = false)
    private final Renderable beforeBody = new Renderable()
    {
        public void render(MarkupWriter writer)
        {
            beginHeartbeat();
            syncCurrentValue();
        }
    };

    @Property(write = false)
    private final Renderable afterBody = new Renderable()
    {
        public void render(MarkupWriter writer)
        {
            endHeartbeat();
        }
    };

    @SuppressWarnings(
            {"unchecked"})
    private void syncValue(String clientValue)
    {
        Object value = encoder.toValue(clientValue);

        if (value == null)
            throw new RuntimeException(String.format(
                    "Unable to convert client value '%s' back into a server-side object.", clientValue));

        this.value = value;
    }

    @Property(write = false)
    private final Renderable syncValue = new Renderable()
    {
        public void render(MarkupWriter writer)
        {
            syncCurrentValue();
        }
    };

    private void syncCurrentValue()
    {
        String id = toClientValue();

        // Add the command that restores value from the value clientValue,
        // when the form is submitted.

        formSupport.store(this, new SyncValue(id));
    }

    /**
     * Uses the {@link org.apache.tapestry5.ValueEncoder} to convert the current server-side value to a client-side
     * value.
     */
    @SuppressWarnings(
            {"unchecked"})
    private String toClientValue()
    {
        return encoder.toClient(value);
    }

    void setupRender(MarkupWriter writer)
    {
        pushContext();

        iterator = source == null ? Collections.EMPTY_LIST.iterator() : source.iterator();

        Link removeRowLink = resources.createEventLink("triggerRemoveRow", context);
        Link injectRowLink = resources.createEventLink("injectRow", context);

        injectRowLink.addParameter(RequestConstants.FORM_CLIENTID_PARAMETER, formSupport.getClientId());
        injectRowLink.addParameter(RequestConstants.FORM_COMPONENTID_PARAMETER, formSupport.getFormComponentId());

        // Fix for TAP5-227 - AjaxFormLoop dont work well inside a table tag
        Element element = writer.getElement();
        this.wrapper = element.getAttribute("data-container-type") != null
                || element.getAttribute("data-remove-row-url") != null
                || element.getAttribute("data-inject-row-url") != null ? writer.element("div") : null;

        writer.attributes("data-container-type", "core/AjaxFormLoop",
                "data-remove-row-url", removeRowLink,
                "data-inject-row-url", injectRowLink);
    }

    private void pushContext()
    {
        environment.push(AjaxFormLoopContext.class, formLoopContext);
    }

    boolean beginRender(MarkupWriter writer)
    {
        if (!iterator.hasNext())
        {
            return false;
        }

        value = iterator.next();

        // Return true: render the body for this value; that ends up being a form-fragment.

        return true;
    }

    Object afterRender(MarkupWriter writer)
    {
        // When out of source items to render, switch over to the addRow block (either the default,
        // or from the addRow parameter) before proceeding to cleanup render.

        if (!iterator.hasNext())
        {
            return tail;
        }

        // There's more to come, loop back to begin render.

        return false;
    }

    // Capture BeginRender event from the formfragment or the addRowWrapper, and render the informal parameters
    // into the row.
    boolean onBeginRender(MarkupWriter writer)
    {
        resources.renderInformalParameters(writer);

        return true;
    }

    void cleanupRender(MarkupWriter writer)
    {
        if (wrapper != null)
            writer.end();

        popContext();
    }

    private void popContext()
    {
        environment.pop(AjaxFormLoopContext.class);
    }

    Object onInjectRow(EventContext context)
    {
        ComponentEventCallback callback = new ComponentEventCallback()
        {
            public boolean handleResult(Object result)
            {
                value = result;

                return true;
            }
        };

        resources.triggerContextEvent(EventConstants.ADD_ROW, context, callback);

        if (value == null)
            throw new IllegalArgumentException(String.format(
                    "Event handler for event 'addRow' from %s should have returned a non-null value.",
                    resources.getCompleteId()));

        ajaxResponseRenderer.addFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                pushContext();

                renderer.renderMarkup(writer, reply);

                popContext();
            }
        });

        return ajaxResponse;
    }

    Object onTriggerRemoveRow(@RequestParameter("t:rowvalue") String encodedValue)
    {
        syncValue(encodedValue);

        resources.triggerEvent(EventConstants.REMOVE_ROW, new Object[]
                {value}, null);

        return new JSONObject();
    }

    private void beginHeartbeat()
    {
        heartbeat.begin();
    }

    private void endHeartbeat()
    {
        heartbeat.end();
    }
}
