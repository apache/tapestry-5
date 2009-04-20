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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.internal.InternalMessages;
import org.apache.tapestry5.corelib.mixins.DiscardBody;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.corelib.mixins.RenderInformals;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.FormSupport;

import java.io.Serializable;

/**
 * Provides initialization of the clientId and elementName properties. In addition, adds the {@link RenderInformals},
 * {@link RenderDisabled} and {@link DiscardBody} mixins.
 */
@SupportsInformalParameters
public abstract class AbstractField implements Field
{
    /**
     * The user presentable label for the field. If not provided, a reasonable label is generated from the component's
     * id, first by looking for a message key named "id-label" (substituting the component's actual id), then by
     * converting the actual id to a presentable string (for example, "userId" to "User Id").
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String label;

    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side behavior). Further, a
     * disabled field ignores any value in the request when the form is submitted.
     */
    @Parameter("false")
    private boolean disabled;

    @SuppressWarnings("unused")
    @Mixin
    private DiscardBody discardBody;

    @Environmental
    private ValidationDecorator decorator;


    static class Setup implements ComponentAction<AbstractField>, Serializable
    {
        private static final long serialVersionUID = 2690270808212097020L;

        private final String controlName;

        public Setup(String controlName)
        {
            this.controlName = controlName;
        }

        public void execute(AbstractField component)
        {
            component.setupControlName(controlName);
        }

        @Override
        public String toString()
        {
            return String.format("AbstractField.Setup[%s]", controlName);
        }
    }

    static class ProcessSubmission implements ComponentAction<AbstractField>, Serializable
    {
        private static final long serialVersionUID = -4346426414137434418L;

        public void execute(AbstractField component)
        {
            component.processSubmission();
        }

        @Override
        public String toString()
        {
            return "AbstractField.ProcessSubmission";
        }
    }

    /**
     * Used a shared instance for all types of fields, for efficiency.
     */
    private static final ProcessSubmission PROCESS_SUBMISSION_ACTION = new ProcessSubmission();

    /**
     * The id used to generate a page-unique client-side identifier for the component. If a component renders multiple
     * times, a suffix will be appended to the to id to ensure uniqueness. The uniqued value may be accessed via the
     * {@link #getClientId() clientId property}.
     */
    @Parameter(value = "prop:componentResources.id", defaultPrefix = BindingConstants.LITERAL)
    private String clientId;

    private String assignedClientId;

    private String controlName;

    @Environmental(false)
    private FormSupport formSupport;

    @Environmental
    private RenderSupport renderSupport;

    @Inject
    private ComponentResources resources;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    final String defaultLabel()
    {
        return defaultProvider.defaultLabel(resources);
    }

    public final String getLabel()
    {
        return label;
    }

    @SetupRender
    final void setup()
    {
        // By default, use the component id as the (base) client id. If the clientid
        // parameter is bound, then that is the value to use.

        String id = clientId;

        // Often, these controlName and clientId will end up as the same value. There are many
        // exceptions, including a form that renders inside a loop, or a form inside a component
        // that is used multiple times.

        if (formSupport == null) throw new RuntimeException(InternalMessages.formFieldOutsideForm(getLabel()));

        assignedClientId = renderSupport.allocateClientId(id);
        String controlName = formSupport.allocateControlName(id);

        formSupport.storeAndExecute(this, new Setup(controlName));
        formSupport.store(this, PROCESS_SUBMISSION_ACTION);
    }

    public final String getClientId()
    {
        return assignedClientId;
    }

    public final String getControlName()
    {
        return controlName;
    }

    public final boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Invoked from within a ComponentCommand callback, to restore the component's elementName.
     */
    private void setupControlName(String controlName)
    {
        this.controlName = controlName;
    }

    private void processSubmission()
    {
        if (!disabled) processSubmission(controlName);
    }

    /**
     * Used by subclasses to create a default binding to a property of the container matching the component id.
     *
     * @return a binding to the property, or null if the container does not have a corresponding property
     */
    protected final Binding createDefaultParameterBinding(String parameterName)
    {
        return defaultProvider.defaultBinding(parameterName, resources);
    }

    /**
     * Method implemented by subclasses to actually do the work of processing the submission of the form. The element's
     * elementName property will already have been set. This method is only invoked if the field is <strong>not {@link
     * #isDisabled() disabled}</strong>.
     *
     * @param elementName the name of the element (used to find the correct parameter in the request)
     */
    protected abstract void processSubmission(String elementName);

    /**
     * Allows the validation decorator to write markup before the field itself writes markup.
     */
    @BeginRender
    final void beforeDecorator()
    {
        decorator.beforeField(this);
    }

    /**
     * Allows the validation decorator to write markup after the field has written all of its markup.
     */
    @AfterRender
    final void afterDecorator()
    {
        decorator.afterField(this);
    }

    /**
     * Invoked from subclasses after they have written their tag and (where appropriate) their informal parameters
     * <em>and</em> have allowed their {@link Validator} to write markup as well.
     */
    protected final void decorateInsideField()
    {
        decorator.insideField(this);
    }

    protected final void setDecorator(ValidationDecorator decorator)
    {
        this.decorator = decorator;
    }

    protected final void setFormSupport(FormSupport formSupport)
    {
        this.formSupport = formSupport;
    }

    /**
     * Returns false; most components do not support declarative validation.
     */
    public boolean isRequired()
    {
        return false;
    }
}
