// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.RadioContainer;
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.services.ComponentDefaultProvider;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.ValueEncoderSource;

public class RadioGroup
{
    /**
     * The property read and updated by the group as a whole.
     */
    @Parameter(required = true, principal=true)
    private Object _value;

    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side
     * behavior). Further, a disabled field ignores any value in the request when the form is
     * submitted.
     */
    @Parameter("false")
    private boolean _disabled;

    /**
     * Allows a specific implementation of {@link ValueEncoder} to be supplied. This is used to
     * create client-side string values for the different radio button values.
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
    private ValueEncoderSource _valueEncoderSource;

    @Inject
    private Request _request;

    private String _elementName;

    final Binding defaultValue()
    {
        return _defaultProvider.defaultBinding("value", _resources);
    }

    final ValueEncoder defaultEncoder()
    {
        return _valueEncoderSource.createEncoder("value", _resources);
    }

    private static class Setup implements ComponentAction<RadioGroup>
    {
        private static final long serialVersionUID = -7984673040135949374L;

        private final String _elementName;

        Setup(String elementName)
        {
            _elementName = elementName;
        }

        public void execute(RadioGroup component)
        {
            component.setup(_elementName);
        }
    };

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
        _elementName = elementName;
    }

    private void processSubmission()
    {
        String clientValue = _request.getParameter(_elementName);

        Object value = _encoder.toValue(clientValue);

        _value = value;
    }

    /**
     * Obtains the element name for the group, and stores a {@link RadioContainer} into the
     * {@link Environment} (so that the {@link Radio} components can find it).
     */
    final void setupRender()
    {
        String name = _formSupport.allocateElementName(_resources.getId());

        ComponentAction<RadioGroup> action = new Setup(name);

        _formSupport.storeAndExecute(this, action);

        _environment.push(RadioContainer.class, new RadioContainer()
        {
            public String getElementName()
            {
                return _elementName;
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
                return TapestryInternalUtils.isEqual(value, _value);
            }

        });

        _formSupport.store(this, PROCESS_SUBMISSION);
    }

    /**
     * Pops the {@link RadioContainer}.
     */
    final void afterRender()
    {
        _environment.pop(RadioContainer.class);
    }

}
