// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
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
 * an encoder).
 */
@SupportsInformalParameters
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
    static class RestoreStateViaEncodedPrimaryKey implements ComponentAction<Loop>
    {
        private static final long serialVersionUID = -2422790241589517336L;

        private final Serializable primaryKey;

        public RestoreStateViaEncodedPrimaryKey(final Serializable primaryKey)
        {
            this.primaryKey = primaryKey;
        }

        public void execute(Loop component)
        {
            component.restoreStateViaEncodedPrimaryKey(primaryKey);
        }

        @Override
        public String toString()
        {
            return String.format("Loop.RestoreStateViaEncodedPrimaryKey[%s]", primaryKey);
        }
    }

    /**
     * Stores a list of keys to be passed to {@link PrimaryKeyEncoder#prepareForKeys(List)}.
     */
    static class PrepareForKeys implements ComponentAction<Loop>
    {
        private static final long serialVersionUID = -6515255627142956828L;

        /**
         * The variable is final, the contents are mutable while the Loop renders.
         */
        private final List<Serializable> keys;

        public PrepareForKeys(final List<Serializable> keys)
        {
            this.keys = keys;
        }

        public void execute(Loop component)
        {
            component.prepareForKeys(keys);
        }

        @Override
        public String toString()
        {
            return "Loop.PrepareForKeys" + keys;
        }
    }

    /**
     * Defines the collection of values for the loop to iterate over. If not specified, defaults to a property of the
     * container whose name matches the Loop cmponent's id.
     */
    @Parameter(required = true, principal = true, autoconnect = true)
    private Iterable<?> source;

    /**
     * Optional primary key converter; if provided and inside a form and not volatile, then each iterated value is
     * converted and stored into the form.
     */
    @Parameter
    private PrimaryKeyEncoder<Serializable, Object> encoder;

    /**
     * If true and the Loop is enclosed by a Form, then the normal state saving logic is turned off. Defaults to false,
     * enabling state saving logic within Forms.
     */
    @Parameter(name = "volatile")
    private boolean volatileState;

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
    @Parameter
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

    private boolean storeRenderStateInForm;

    @Inject
    private ComponentResources resources;

    private Block cleanupBlock;


    String defaultElement()
    {
        return resources.getElementName();
    }

    @SetupRender
    boolean setup()
    {
        index = 0;

        iterator = source == null ? null : source.iterator();

        storeRenderStateInForm = formSupport != null && !volatileState;

        // Only render the body if there is something to iterate over

        boolean hasContent = iterator != null && iterator.hasNext();

        if (formSupport != null && hasContent)
        {
            formSupport.store(this, volatileState ? SETUP_FOR_VOLATILE : RESET_INDEX);

            if (encoder != null)
            {
                List<Serializable> keyList = CollectionFactory.newList();

                // We'll keep updating the _keyList while the Loop renders, the values will "lock
                // down" when the Form serializes all the data.

                formSupport.store(this, new PrepareForKeys(keyList));
            }
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
        return cleanupBlock;
    }

    private void prepareForKeys(List<Serializable> keys)
    {
        // Again, the encoder existed when we rendered, we better have another available
        // when the enclosing Form is submitted.

        encoder.prepareForKeys(keys);
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
    void begin()
    {
        value = iterator.next();

        if (storeRenderStateInForm)
        {
            if (encoder == null)
            {
                formSupport.store(this, new RestoreState(value));
            }
            else
            {
                Serializable primaryKey = encoder.toKey(value);
                formSupport.store(this, new RestoreStateViaEncodedPrimaryKey(primaryKey));
            }
        }

        if (formSupport != null && volatileState) formSupport.store(this, ADVANCE_VOLATILE);

        startHeartbeat();
    }

    private void startHeartbeat()
    {
        heartbeat.begin();
    }

    void beforeRenderBody(MarkupWriter writer)
    {
        if (element != null)
        {
            writer.element(element);
            resources.renderInformalParameters(writer);
        }
    }

    void afterRenderBody(MarkupWriter writer)
    {
        if (element != null) writer.end();
    }

    /**
     * Ends the current heartbeat.
     */
    @AfterRender
    boolean after()
    {
        endHeartbeat();

        if (formSupport != null) formSupport.store(this, END_HEARTBEAT);

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
    private void restoreStateViaEncodedPrimaryKey(Serializable primaryKey)
    {
        // We assume that if a encoder is available when we rendered, that one will be available
        // when the form is submitted. TODO: Check for this.

        Object restoredValue = encoder.toValue(primaryKey);

        restoreState(restoredValue);
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
