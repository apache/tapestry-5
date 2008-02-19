// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.*;

public class RadioGroup implements Field
{
    /**
     * The property read and updated by the group as a whole.
     */
    @Parameter(required = true, principal = true)
    private Object _value;

    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side behavior). Further, a
     * disabled field ignores any value in the request when the form is submitted.
     */
    @Parameter("false")
    private boolean _disabled;

    /**
     * The user presentable label for the field. If not provided, a reasonable label is generated from the component's
     * id, first by looking for a message key named "id-label" (substituting the component's actual id), then by
     * converting the actual id to a presentable string (for example, "userId" to "User Id").
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String _label;

    /**
     * Allows a specific implementation of {@link ValueEncoder} to be supplied. This is used to create client-side
     * string values for the different radio button values.
     *
     * @see ValueEncoderSource
     */
    @Parameter(required = true)
    private ValueEncoder _encoder;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private FormSupport _formSupport;

    @Inject
    private Environment _environment;

    @Inject
    private Request _request;

    @Environmental
    private ValidationTracker _tracker;

    private String _controlName;

    final Binding defaultValue()
    {
        return _defaultProvider.defaultBinding("value", _resources);
    }

    String defaultLabel()
    {
        return _defaultProvider.defaultLabel(_resources);
    }

    final ValueEncoder defaultEncoder()
    {
        return _defaultProvider.defaultValueEncoder("value", _resources);
    }

    private static class Setup implements ComponentAction<RadioGroup>
    {
        private static final long serialVersionUID = -7984673040135949374L;

        private final String _controlName;

        Setup(String controlName)
        {
            _controlName = controlName;
        }

        public void execute(RadioGroup component)
        {
            component.setup(_controlName);
        }
    }

    private static final ComponentAction<RadioGroup> PROCESS_SUBMISSION = new ComponentAction<RadioGroup>()
    {
        private static final long serialVersionUID = -3857110108918776386L;

        public void execute(RadioGroup component)
        {
            component.processSubmission();
        }
    };

    private void setup(String elementName)
    {
        _controlName = elementName;
    }

    private void processSubmission()
    {
        String clientValue = _request.getParameter(_controlName);

        _tracker.recordInput(this, clientValue);

        _value = _encoder.toValue(clientValue);
    }

    /**
     * Obtains the element name for the group, and stores a {@link RadioContainer} into the {@link Environment} (so that
     * the {@link Radio} components can find it).
     */
    final void setupRender()
    {
        String name = _formSupport.allocateControlName(_resources.getId());

        ComponentAction<RadioGroup> action = new Setup(name);

        _formSupport.storeAndExecute(this, action);

        String submittedValue = _tracker.getInput(this);

        final String selectedValue = submittedValue != null ? submittedValue : _encoder.toClient(_value);


        _environment.push(RadioContainer.class, new RadioContainer()
        {
            public String getElementName()
            {
                return _controlName;
            }

            public boolean isDisabled()
            {
                return _disabled;
            }

            @SuppressWarnings("unchecked")
            public String toClient(Object value)
            {
                // TODO: Ensure that value is of the expected type?

                return _encoder.toClient(value);
            }

            public boolean isSelected(Object value)
            {
                return TapestryInternalUtils.isEqual(_encoder.toClient(value), selectedValue);
            }

        });

        _formSupport.store(this, PROCESS_SUBMISSION);
    }

    /**
     * Pops the {@link RadioContainer} off the Environment.
     */
    final void afterRender()
    {
        _environment.pop(RadioContainer.class);
    }

    public String getControlName()
    {
        return _controlName;
    }

    public String getLabel()
    {
        return _label;
    }

    public boolean isDisabled()
    {
        return _disabled;
    }

    /**
     * Returns null; the radio group does not render as a tag and so doesn't have an id to share.  RadioGroup implements
     * {@link org.apache.tapestry.Field} only so it can interact with the {@link org.apache.tapestry.ValidationTracker}.
     *
     * @return null
     */
    public String getClientId()
    {
        return null;
    }
}
