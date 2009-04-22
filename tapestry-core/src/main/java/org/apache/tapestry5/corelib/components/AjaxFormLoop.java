// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.internal.AjaxFormLoopContext;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;

import java.util.Collections;
import java.util.Iterator;

/**
 * A special form of the {@link org.apache.tapestry5.corelib.components.Loop} component that adds  Ajax support to
 * handle adding new rows and removing existing rows dynamically.  Expects that the values being iterated over are
 * entities that can be identified via a {@link org.apache.tapestry5.ValueEncoder}.
 * <p/>
 * Works with {@link org.apache.tapestry5.corelib.components.AddRowLink} and {@link
 * org.apache.tapestry5.corelib.components.RemoveRowLink} components.
 * <p/>
 * The addRow event will receive the context specified by the context parameter.
 * <p/>
 * The removeRow event will receive the client-side value for the row being iterated.
 *
 * @see EventConstants#ADD_ROW
 * @see EventConstants#REMOVE_ROW
 */
@Events({ EventConstants.ADD_ROW, EventConstants.REMOVE_ROW })
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
     * visible.  This is used with the {@link FormInjector} component, when adding a new row to the loop. Leaving as
     * null uses the default function, "highlight".
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String show;

    /**
     * The context for the form loop (optional parameter). This list of values will be converted into strings and
     * included in the URI. The strings will be coerced back to whatever their values are and made available to event
     * handler methods.
     */
    @Parameter
    private Object[] context;


    /**
     * A block to render after the loop as the body of the {@link org.apache.tapestry5.corelib.components.FormInjector}.
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
     * Required parameter used to convert server-side objects (provided from the source) into client-side ids and back.
     * A default encoder may be calculated from the type of property bound to the value parameter.
     */
    @Parameter(required = true, allowNull = false)
    private ValueEncoder<Object> encoder;

    @InjectComponent
    private ClientElement rowInjector;

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
    private RenderSupport renderSupport;

    private JSONArray addRowTriggers;

    private Iterator iterator;

    @Inject
    private TypeCoercer typeCoercer;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private PageRenderQueue pageRenderQueue;

    private boolean renderingInjector;

    ValueEncoder defaultEncoder()
    {
        return defaultProvider.defaultValueEncoder("value", resources);
    }


    private final AjaxFormLoopContext formLoopContext = new AjaxFormLoopContext()
    {
        public void addAddRowTrigger(String clientId)
        {
            Defense.notBlank(clientId, "clientId");

            addRowTriggers.put(clientId);
        }

        private String currentFragmentId()
        {
            ClientElement element = renderingInjector ? rowInjector : fragment;

            return element.getClientId();
        }

        public void addRemoveRowTrigger(String clientId)
        {
            Link link = resources.createEventLink("triggerRemoveRow", toClientValue());

            String asURI = link.toAbsoluteURI();

            JSONObject spec = new JSONObject();
            spec.put("link", clientId);
            spec.put("fragment", currentFragmentId());
            spec.put("url", asURI);

            renderSupport.addInit("formLoopRemoveLink", spec);
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

    @SuppressWarnings({ "unchecked" })
    @Log
    private void syncValue(String clientValue)
    {
        Object value = encoder.toValue(clientValue);

        if (value == null)
            throw new RuntimeException(
                    String.format("Unable to convert client value '%s' back into a server-side object.", clientValue));

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
    @SuppressWarnings({ "unchecked" })
    private String toClientValue()
    {
        return encoder.toClient(value);
    }


    void setupRender()
    {
        addRowTriggers = new JSONArray();

        pushContext();

        iterator = source == null
                   ? Collections.EMPTY_LIST.iterator()
                   : source.iterator();

        renderingInjector = false;
    }

    private void pushContext()
    {
        environment.push(AjaxFormLoopContext.class, formLoopContext);
    }

    boolean beginRender(MarkupWriter writer)
    {
        if (!iterator.hasNext()) return false;

        value = iterator.next();

        return true;  // Render body, etc.
    }

    Object afterRender(MarkupWriter writer)
    {
        // When out of source items to render, switch over to the addRow block (either the default,
        // or from the addRow parameter) before proceeding to cleanup render.

        if (!iterator.hasNext())
        {
            renderingInjector = true;
            return tail;
        }

        // There's more to come, loop back to begin render.

        return false;
    }

    void cleanupRender()
    {
        popContext();

        JSONObject spec = new JSONObject();

        spec.put("rowInjector", rowInjector.getClientId());
        spec.put("addRowTriggers", addRowTriggers);

        renderSupport.addInit("ajaxFormLoop", spec);
    }

    private void popContext()
    {
        environment.pop(AjaxFormLoopContext.class);
    }

    /**
     * When the action event arrives from the FormInjector, we fire our own event, "addRow" to tell the container to add
     * a new row, and to return that new entity for rendering.
     */
    @Log
    Object onActionFromRowInjector(EventContext context)
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
            throw new IllegalArgumentException(
                    String.format("Event handler for event 'addRow' from %s should have returned a non-null value.",
                                  resources.getCompleteId()));


        renderingInjector = true;

        pageRenderQueue.addPartialMarkupRendererFilter(new PartialMarkupRendererFilter()
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

    @Log
    Object onTriggerRemoveRow(String rowId)
    {
        Object value = encoder.toValue(rowId);

        resources.triggerEvent(EventConstants.REMOVE_ROW, new Object[] { value }, null);

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
