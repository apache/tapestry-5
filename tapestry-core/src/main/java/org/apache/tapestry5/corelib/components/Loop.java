// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.corelib.LoopFormState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Basic looping class; loops over a number of items (provided by its source parameter), rendering its body for each
 * one. It turns out that gettting the component to <em>not</em> store its state in the Form is very tricky and, in
 * fact, a series of commands for starting and ending heartbeats, and advancing through the iterator, are still stored.
 * For a non-volatile Loop inside the form, the Loop stores a series of commands that start and end heartbeats and store
 * state (either as full objects when there the encoder parameter is not bound, or as client-side objects when there is
 * an encoder). For a Loop that doesn't need to be aware of the enclosing Form (if any), the formState parameter should
 * be bound to 'none'.
 * <p/>
 * When the Loop is used inside a Form, it will generate an {@link org.apache.tapestry5.EventConstants#SYNCHRONIZE_VALUES}
 * event to inform its container what values were submitted and in what order.
 */
@SupportsInformalParameters
@Events(EventConstants.SYNCHRONIZE_VALUES)
public class Loop
{
    /**
     * Setup command for non-volatile rendering.
     */
    private static final ComponentAction<Loop> RESET_INDEX = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = 6477493424977597345L;

        public void execute(Loop component)
        {
            component.resetIndex();
        }

        @Override
        public String toString()
        {
            return "Loop.ResetIndex";
        }
    };

    /**
     * Setup command for volatile rendering. Volatile rendering relies on re-acquiring the Iterator and working our way
     * through it (and hoping for the best!).
     */
    private static final ComponentAction<Loop> SETUP_FOR_VOLATILE = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = -977168791667037377L;

        public void execute(Loop component)
        {
            component.setupForVolatile();
        }

        @Override
        public String toString()
        {
            return "Loop.SetupForVolatile";
        }
    };

    /**
     * Advances to next value in a volatile way. So, the <em>number</em> of steps is intrinsically stored in the Form
     * (as the number of ADVANCE_VOLATILE commands), but the actual values are expressly stored only on the server.
     */
    private static final ComponentAction<Loop> ADVANCE_VOLATILE = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = -4600281573714776832L;

        public void execute(Loop component)
        {
            component.advanceVolatile();
        }

        @Override
        public String toString()
        {
            return "Loop.AdvanceVolatile";
        }
    };

    /**
     * Used in both volatile and non-volatile mode to end the current heartbeat (started by either ADVANCE_VOLATILE or
     * one of the RestoreState commands). Also increments the index.
     */
    private static final ComponentAction<Loop> END_HEARTBEAT = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = -977168791667037377L;

        public void execute(Loop component)
        {
            component.endHeartbeat();
        }

        @Override
        public String toString()
        {
            return "Loop.EndHeartbeat";
        }
    };

    /**
     * Restores a state value (this is the case when there is no encoder and the complete value is stored).
     */
    static class RestoreState implements ComponentAction<Loop>
    {
        private static final long serialVersionUID = -3926831611368720764L;

        private final Object storedValue;

        public RestoreState(final Object storedValue)
        {
            this.storedValue = storedValue;
        }

        public void execute(Loop component)
        {
            component.restoreState(storedValue);
        }

        @Override
        public String toString()
        {
            return String.format("Loop.RestoreState[%s]", storedValue);
        }
    }

    /**
     * Restores the value using a stored primary key via {@link PrimaryKeyEncoder#toValue(Serializable)}.
     */
    static class RestoreStateFromStoredClientValue implements ComponentAction<Loop>
    {
        private final String clientValue;

        public RestoreStateFromStoredClientValue(final String clientValue)
        {
            this.clientValue = clientValue;
        }

        public void execute(Loop component)
        {
            component.restoreStateFromStoredClientValue(clientValue);
        }

        @Override
        public String toString()
        {
            return String.format("Loop.RestoreStateFromStoredClientValue[%s]", clientValue);
        }
    }

    /**
     * Start of processing event that allows the Loop to set up internal bookeeping, to track which values have come up
     * in the form submission.
     */
    static final ComponentAction<Loop> PREPARE_FOR_SUBMISSION = new ComponentAction<Loop>()
    {
        public void execute(Loop component)
        {
            component.prepareForSubmission();
        }

        @Override
        public String toString()
        {
            return "Loop.PrepareForSubmission";
        }
    };

    static final ComponentAction<Loop> NOTIFY_CONTAINER = new ComponentAction<Loop>()
    {
        public void execute(Loop component)
        {
            component.notifyContainer();
        }

        @Override
        public String toString()
        {
            return "Loop.NotifyContainer";
        }
    };

    /**
     * Defines the collection of values for the loop to iterate over. If not specified, defaults to a property of the
     * container whose name matches the Loop cmponent's id.
     */
    @Parameter(required = true, principal = true, autoconnect = true)
    private Iterable<?> source;

    /**
     * Optional value converter; if provided (or defaulted) and inside a form and not volatile, then each iterated value
     * is converted and stored into the form. A default for this is calculated from the type of the property bound to
     * the value parameter.
     */
    @Parameter
    private ValueEncoder<Object> encoder;

    /**
     * If true and the Loop is enclosed by a Form, then the normal state saving logic is turned off. Defaults to false,
     * enabling state saving logic within Forms. With the addition of the formState parameter, volatile simply sets a
     * default for formState is formState is not specified.
     *
     * @deprecated in release 5.1.0.4, use the formState parameter instead.
     */
    @Parameter(name = "volatile", principal = true)
    @Deprecated
    private boolean volatileState;

    /**
     * Controls what information, if any, is encoded into an enclosing Form. The default value for this is set by the
     * deprecated volatile parameter. The normal default is {@link org.apache.tapestry5.corelib.LoopFormState#VALUES},
     * but changes to {@link org.apache.tapestry5.corelib.LoopFormState#ITERATION} if volatile is true. This parameter
     * is only used if the component is enclosed by a Form.
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private LoopFormState formState;

    @Environmental(false)
    private FormSupport formSupport;

    /**
     * The element to render. If not null, then the loop will render the indicated element around its body (on each pass
     * through the loop). The default is derived from the component template.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String element;

    /**
     * The current value, set before the component renders its body.
     */
    @Parameter(principal = true)
    private Object value;

    /**
     * The index into the source items.
     */
    @Parameter
    private int index;

    /**
     * A Block to render instead of the loop when the source is empty.  The default is to render nothing.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private Block empty;

    private Iterator<?> iterator;

    @Environmental
    private Heartbeat heartbeat;

    private boolean storeValuesInForm, storeIncrementsInForm, storeHeartbeatsInForm;

    @Inject
    private ComponentResources resources;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    private Block cleanupBlock;

    /**
     * Objects that have been recovered via {@link org.apache.tapestry5.ValueEncoder#toValue(String)} during the
     * processing of the loop. These are sent to the container via an event.
     */
    private List<Object> synchonizedValues;


    LoopFormState defaultFormState()
    {
        return volatileState ? LoopFormState.ITERATION : LoopFormState.VALUES;
    }

    String defaultElement()
    {
        return resources.getElementName();
    }

    ValueEncoder defaultEncoder()
    {
        return defaultProvider.defaultValueEncoder("value", resources);
    }

    @SetupRender
    boolean setup()
    {
        index = 0;

        iterator = source == null ? null : source.iterator();

        boolean insideForm = formSupport != null;


        storeValuesInForm = insideForm && formState == LoopFormState.VALUES;
        storeIncrementsInForm = insideForm && formState == LoopFormState.ITERATION;

        storeHeartbeatsInForm = insideForm && formState != LoopFormState.NONE;

        if (storeValuesInForm)
            formSupport.store(this, PREPARE_FOR_SUBMISSION);

        // Only render the body if there is something to iterate over

        boolean hasContent = iterator != null && iterator.hasNext();

        if (insideForm && hasContent)
        {
            if (storeValuesInForm) formSupport.store(this, RESET_INDEX);
            if (storeIncrementsInForm) formSupport.store(this, SETUP_FOR_VOLATILE);
        }

        cleanupBlock = hasContent ? null : empty;

        // Jump directly to cleanupRender if there is no content

        return hasContent;
    }


    /**
     * Returns the empty block, or null, after the render has finished. It will only be the empty block (which itself
     * may be null) if the source was null or empty.
     */
    Block cleanupRender()
    {
        if (storeValuesInForm)
            formSupport.store(this, NOTIFY_CONTAINER);

        return cleanupBlock;
    }

    private void setupForVolatile()
    {
        index = 0;
        iterator = source.iterator();
    }

    private void advanceVolatile()
    {
        value = iterator.next();

        startHeartbeat();
    }

    /**
     * Begins a new heartbeat.
     */
    @BeginRender
    void begin(MarkupWriter writer)
    {
        value = iterator.next();

        if (storeValuesInForm)
        {
            if (encoder == null)
            {
                formSupport.store(this, new RestoreState(value));
            }
            else
            {
                String clientValue = encoder.toClient(value);

                formSupport.store(this, new RestoreStateFromStoredClientValue(clientValue));
            }
        }

        if (storeIncrementsInForm)
        {
            formSupport.store(this, ADVANCE_VOLATILE);
        }

        startHeartbeat();

        if (element != null)
        {
            writer.element(element);
            resources.renderInformalParameters(writer);
        }
    }

    private void startHeartbeat()
    {
        heartbeat.begin();
    }

    /**
     * Ends the current heartbeat.
     */
    @AfterRender
    boolean after(MarkupWriter writer)
    {
        if (element != null) writer.end();

        endHeartbeat();

        if (storeHeartbeatsInForm)
        {
            formSupport.store(this, END_HEARTBEAT);
        }

        return !iterator.hasNext();
    }

    private void endHeartbeat()
    {
        heartbeat.end();

        index++;
    }

    private void resetIndex()
    {
        index = 0;
    }

    /**
     * Restores state previously stored by the Loop into a Form.
     */
    private void restoreState(Object storedValue)
    {
        value = storedValue;

        startHeartbeat();
    }

    /**
     * Restores state previously encoded by the Loop and stored into the Form.
     */
    private void restoreStateFromStoredClientValue(String clientValue)
    {
        // We assume that if an encoder is available when we rendered, that one will be available
        // when the form is submitted.

        Object restoredValue = encoder.toValue(clientValue);

        restoreState(restoredValue);

        synchonizedValues.add(restoredValue);
    }

    private void prepareForSubmission()
    {
        synchonizedValues = CollectionFactory.newList();
    }

    private void notifyContainer()
    {
        Object[] values = synchonizedValues.toArray();

        resources.triggerEvent(EventConstants.SYNCHRONIZE_VALUES, values, null);
    }

    // For testing:

    int getIndex()
    {
        return index;
    }

    Object getValue()
    {
        return value;
    }

    void setSource(Iterable<?> source)
    {
        this.source = source;
    }

    void setHeartbeat(Heartbeat heartbeat)
    {
        this.heartbeat = heartbeat;
    }
}
