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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;

public class RadioGroup implements Field
{
    /**
     * The property read and updated by the group as a whole.
     */
    @Parameter(required = true, principal = true)
    private Object value;

    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side behavior). Further, a
     * disabled field ignores any value in the request when the form is submitted.
     */
    @Parameter("false")
    private boolean disabled;

    /**
     * The user presentable label for the field. If not provided, a reasonable label is generated from the component's
     * id, first by looking for a message key named "id-label" (substituting the component's actual id), then by
     * converting the actual id to a presentable string (for example, "userId" to "User Id").
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String label;

    /**
     * Allows a specific implementation of {@link org.apache.tapestry5.ValueEncoder} to be supplied. This is used to
     * create client-side string values for the different radio button values.
     */
    @Parameter(required = true, allowNull = false)
    private ValueEncoder encoder;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private ComponentResources resources;

    @Environmental
    private FormSupport formSupport;

    @Inject
    private Environment environment;

    @Inject
    private Request request;

    @Environmental
    private ValidationTracker tracker;

    private String controlName;

    final Binding defaultValue()
    {
        return defaultProvider.defaultBinding("value", resources);
    }

    String defaultLabel()
    {
        return defaultProvider.defaultLabel(resources);
    }

    final ValueEncoder defaultEncoder()
    {
        return defaultProvider.defaultValueEncoder("value", resources);
    }

    private static class Setup implements ComponentAction<RadioGroup>
    {
        private static final long serialVersionUID = -7984673040135949374L;

        private final String controlName;

        Setup(String controlName)
        {
            this.controlName = controlName;
        }

        public void execute(RadioGroup component)
        {
            component.setup(controlName);
        }

        @Override
        public String toString()
        {
            return String.format("RadioGroup.Setup[%s]", controlName);
        }
    }

    private static final ComponentAction<RadioGroup> PROCESS_SUBMISSION = new ComponentAction<RadioGroup>()
    {
        private static final long serialVersionUID = -3857110108918776386L;

        public void execute(RadioGroup component)
        {
            component.processSubmission();
        }

        @Override
        public String toString()
        {
            return "RadioGroup.ProcessSubmission";
        }
    };

    private void setup(String elementName)
    {
        controlName = elementName;
    }

    private void processSubmission()
    {
        String clientValue = request.getParameter(controlName);

        tracker.recordInput(this, clientValue);

        value = encoder.toValue(clientValue);
    }

    /**
     * Obtains the element name for the group, and stores a {@link RadioContainer} into the {@link Environment} (so that
     * the {@link Radio} components can find it).
     */
    final void setupRender()
    {
        String name = formSupport.allocateControlName(resources.getId());

        ComponentAction<RadioGroup> action = new Setup(name);

        formSupport.storeAndExecute(this, action);

        String submittedValue = tracker.getInput(this);

        final String selectedValue = submittedValue != null ? submittedValue : encoder.toClient(value);


        environment.push(RadioContainer.class, new RadioContainer()
        {
            public String getElementName()
            {
                return controlName;
            }

            public boolean isDisabled()
            {
                return disabled;
            }

            @SuppressWarnings("unchecked")
            public String toClient(Object value)
            {
                // TODO: Ensure that value is of the expected type?

                return encoder.toClient(value);
            }

            public boolean isSelected(Object value)
            {
                return TapestryInternalUtils.isEqual(encoder.toClient(value), selectedValue);
            }

        });

        formSupport.store(this, PROCESS_SUBMISSION);
    }

    /**
     * Pops the {@link RadioContainer} off the Environment.
     */
    final void afterRender()
    {
        environment.pop(RadioContainer.class);
    }

    public String getControlName()
    {
        return controlName;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Returns null; the radio group does not render as a tag and so doesn't have an id to share.  RadioGroup implements
     * {@link org.apache.tapestry5.Field} only so it can interact with the {@link org.apache.tapestry5.ValidationTracker}.
     *
     * @return null
     */
    public String getClientId()
    {
        return null;
    }

    /**
     * Returns false; RadioGroup does not support declarative validation.
     */
    public boolean isRequired()
    {
        return false;
    }
}
