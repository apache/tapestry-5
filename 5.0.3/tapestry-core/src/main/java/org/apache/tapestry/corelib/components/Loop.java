// Copyright 2006, 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PrimaryKeyEncoder;
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SetupRender;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Heartbeat;

/**
 * Basic looping class; loops over a number of items (provided by its source parameter), rendering
 * its body for each one. It turns out that gettting the component to <em>not</em> store its state
 * in the Form is very tricky and, in fact, a series of commands for starting and ending heartbeats,
 * and advancing through the iterator, are still stored. For a non-volatile Loop inside the form,
 * the Loop stores a series of commands that start and end heartbeats and store state (either as
 * full objects when there is not encoder, or as client-side objects when there is an encoder).
 */
@SupportsInformalParameters
public class Loop
{
    /** Setup command for non-volatile rendering. */
    private static final ComponentAction<Loop> RESET_INDEX = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = 6477493424977597345L;

        public void execute(Loop component)
        {
            component.resetIndex();
        }
    };

    /**
     * Setup command for volatile rendering. Volatile rendering relies on re-acquiring the Iterator
     * and working our way through it (and hoping for the best!).
     */
    private static final ComponentAction<Loop> SETUP_FOR_VOLATILE = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = -977168791667037377L;

        public void execute(Loop component)
        {
            component.setupForVolatile();
        };
    };

    /**
     * Advances to next value in a volatile way. So, the <em>number</em> of steps is intrinsically
     * stored in the Form (as the number of ADVANCE_VOLATILE commands), but the actual values are
     * expressly stored only on the server.
     */
    private static final ComponentAction<Loop> ADVANCE_VOLATILE = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = -4600281573714776832L;

        public void execute(Loop component)
        {
            component.advanceVolatile();
        }
    };

    /**
     * Used in both volatile and non-volatile mode to end the current heartbeat (started by either
     * ADVANCE_VOLATILE or one of the RestoreState commands). Also increments the index.
     */
    private static final ComponentAction<Loop> END_HEARTBEAT = new ComponentAction<Loop>()
    {
        private static final long serialVersionUID = -977168791667037377L;

        public void execute(Loop component)
        {
            component.endHeartbeat();
        };
    };

    /**
     * Restores a state value (this is the case when there is no encoder and the complete value is
     * stored).
     */
    static class RestoreState implements ComponentAction<Loop>
    {
        private static final long serialVersionUID = -3926831611368720764L;

        private final Object _storedValue;

        public RestoreState(final Object storedValue)
        {
            _storedValue = storedValue;
        }

        public void execute(Loop component)
        {
            component.restoreState(_storedValue);
        }
    };

    /**
     * Restores the value using a stored primary key via
     * {@link PrimaryKeyEncoder#toValue(Serializable)}.
     */
    static class RestoreStateViaEncodedPrimaryKey implements ComponentAction<Loop>
    {
        private static final long serialVersionUID = -2422790241589517336L;

        private final Serializable _primaryKey;

        public RestoreStateViaEncodedPrimaryKey(final Serializable primaryKey)
        {
            _primaryKey = primaryKey;
        }

        public void execute(Loop component)
        {
            component.restoreStateViaEncodedPrimaryKey(_primaryKey);
        }
    };

    /**
     * Stores a list of keys to be passed to {@link PrimaryKeyEncoder#prepareForKeys(List)}.
     */
    static class PrepareForKeys implements ComponentAction<Loop>
    {
        private static final long serialVersionUID = -6515255627142956828L;

        /** The variable is final, the contents are mutable while the Loop renders. */
        private final List<Serializable> _keys;

        public PrepareForKeys(final List<Serializable> keys)
        {
            _keys = keys;
        }

        public void execute(Loop component)
        {
            component.prepareForKeys(_keys);
        }
    };

    /**
     * Defines the collection of values for the loop to iterate over.
     */
    @Parameter(required = true)
    private Iterable<?> _source;

    /**
     * Optional primary key converter; if provided and inside a form and not volatile, then each
     * iterated value is converted and stored into the form.
     */
    @Parameter
    private PrimaryKeyEncoder<Serializable, Object> _encoder;

    /**
     * If true and the Loop is enclosed by a Form, then the normal state saving logic is turned off.
     * Defaults to false, enabling state saving logic within Forms.
     */
    @Parameter
    private boolean _volatile;

    @Environmental(false)
    private FormSupport _formSupport;

    /**
     * The element to render. If not null, then the loop will render the indicated element around
     * its body (on each pass through the loop). The default is derived from the component template.
     */
    @Parameter(value = "prop:componentResources.elementName", defaultPrefix = "literal")
    private String _elementName;

    /**
     * The current value, set before the component renders its body.
     */
    @Parameter
    private Object _value;

    /**
     * The index into the source items.
     */
    @Parameter
    private int _index;

    private Iterator<?> _iterator;

    @Environmental
    private Heartbeat _heartbeat;

    private boolean _storeRenderStateInForm;

    @Inject
    private ComponentResources _resources;

    private List<Serializable> _keyList;

    @SetupRender
    boolean setup()
    {
        _index = 0;

        if (_source == null)
            return false;

        _iterator = _source.iterator();

        _storeRenderStateInForm = _formSupport != null && !_volatile;

        // Only render the body if there is something to iterate over

        boolean result = _iterator.hasNext();

        if (_formSupport != null && result)
        {

            _formSupport.store(this, _volatile ? SETUP_FOR_VOLATILE : RESET_INDEX);

            if (_encoder != null)
            {
                _keyList = newList();

                // We'll keep updating the _keyList while the Loop renders, the values will "lock
                // down" when the Form serializes all the data.

                _formSupport.store(this, new PrepareForKeys(_keyList));
            }
        }

        return result;
    }

    private void prepareForKeys(List<Serializable> keys)
    {
        // Again, the encoder existed when we rendered, we better have another available
        // when the enclosing Form is submitted.

        _encoder.prepareForKeys(keys);
    }

    private void setupForVolatile()
    {
        _index = 0;
        _iterator = _source.iterator();
    }

    private void advanceVolatile()
    {
        _value = _iterator.next();

        startHeartbeat();
    }

    /** Begins a new heartbeat. */
    @BeginRender
    void begin()
    {
        _value = _iterator.next();

        if (_storeRenderStateInForm)
        {
            if (_encoder == null)
            {
                _formSupport.store(this, new RestoreState(_value));
            }
            else
            {
                Serializable primaryKey = _encoder.toKey(_value);
                _formSupport.store(this, new RestoreStateViaEncodedPrimaryKey(primaryKey));
            }
        }

        if (_formSupport != null && _volatile)
            _formSupport.store(this, ADVANCE_VOLATILE);

        startHeartbeat();
    }

    private void startHeartbeat()
    {
        _heartbeat.begin();
    }

    void beforeRenderBody(MarkupWriter writer)
    {
        if (_elementName != null)
        {
            writer.element(_elementName);
            _resources.renderInformalParameters(writer);
        }
    }

    void afterRenderBody(MarkupWriter writer)
    {
        if (_elementName != null)
            writer.end();
    }

    /** Ends the current heartbeat. */
    @AfterRender
    boolean after()
    {
        endHeartbeat();

        if (_formSupport != null)
            _formSupport.store(this, END_HEARTBEAT);

        return ! _iterator.hasNext();
    }

    private void endHeartbeat()
    {
        _heartbeat.end();

        _index++;
    }

    private void resetIndex()
    {
        _index = 0;
    }

    /** Restores state previously stored by the Loop into a Form. */
    private void restoreState(Object storedValue)
    {
        _value = storedValue;

        startHeartbeat();
    }

    /** Restores state previously encoded by the Loop and stored into the Form. */
    private void restoreStateViaEncodedPrimaryKey(Serializable primaryKey)
    {
        // We assume that if a encoder is available when we rendered, that one will be available
        // when the form is submitted. TODO: Check for this.

        Object restoredValue = _encoder.toValue(primaryKey);

        restoreState(restoredValue);
    }

    // For testing:

    int getIndex()
    {
        return _index;
    }

    Object getValue()
    {
        return _value;
    }

    void setSource(Iterable<?> source)
    {
        _source = source;
    }

    void setHeartbeat(Heartbeat heartbeat)
    {
        _heartbeat = heartbeat;
    }
}
