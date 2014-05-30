// Copyright 2006-2013 The Apache Software Foundation
//
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

package org.apache.tapestry5.corelib.base;

import java.io.Serializable;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidationSupport;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.Validator;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.mixins.DiscardBody;
import org.apache.tapestry5.corelib.mixins.RenderInformals;
import org.apache.tapestry5.internal.BeanValidationContext;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.PreSelectedFormNamesService;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Provides initialization of the clientId and elementName properties. In addition, adds the {@link RenderInformals},
 * and {@link DiscardBody} mixins.
 *
 * @tapestrydoc
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
    protected String label;

    /**
     * If true, then the field will render out with a disabled attribute
     * (to turn off client-side behavior). When the form is submitted, the
     * bound value is evaluated again and, if true, the field's value is
     * ignored (not even validated) and the component's events are not fired.
     */
    @Parameter("false")
    protected boolean disabled;

    @SuppressWarnings("unused")
    @Mixin
    private DiscardBody discardBody;

    @Environmental
    protected ValidationDecorator decorator;

    @Inject
    protected Environment environment;
    
    @Inject
    @Symbol(SymbolConstants.FORM_FIELD_CSS_CLASS)
    protected String cssClass;

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
     * The id used to generate a page-unique client-side identifier for the component. 
     * If this parameter is not bound and this component is rendered multiple times in a request
     * and <code>forceClientAllocation</code> is false (the default),
     * a suffix will be appended to the to id to ensure uniqueness. Either way, 
     * its value may be accessed via the {@link #getClientId() clientId property}.
     * When this parameter is bound, Tapestry considers the user (developer) is taking care of 
     * providing unique client-side identifiers. Special care should be taken when the
     * field is inside a Zone.
     * <br>
     * <strong>Default value: the component's t:id</strong>.
     * <br>
     * <em>This parameter will be ignored if it receives any of these values:</em>
     * <ul>
     *   <li>reset</li>
     *   <li>submit</li>
     *   <li>id</li>
     *   <li>method</li>
     *   <li>action</li>
     *   <li>onsubmit</li>
     *   <li>cancel</li>
     * </ul>
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    protected String clientId;
    
    /**
     * When true, it forces the clientId to be passed through the id allocator to avoid repeated ids
     * even when the clientId parameter is bound.
     */
    @Parameter("false")
    private boolean forceClientIdAllocation;

    private String assignedClientId;

    private String controlName;

    @Environmental(false)
    protected FormSupport formSupport;

    @Environmental
    protected JavaScriptSupport javaScriptSupport;

    @Environmental
    protected ValidationTracker validationTracker;

    @Inject
    protected ComponentResources resources;

    @Inject
    protected ComponentDefaultProvider defaultProvider;

    @Inject
    protected Request request;

    @Inject
    protected FieldValidationSupport fieldValidationSupport;
    
    @Inject
    private PreSelectedFormNamesService preSelectedFormNamesService;

    final String defaultLabel()
    {
        return defaultProvider.defaultLabel(resources);
    }

    final String defaultClientId()
    {
        return resources.getId();
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

        if (formSupport == null)
            throw new RuntimeException(String.format("Component %s must be enclosed by a Form component.",
                    resources.getCompleteId()));
        
        final boolean avoidAllocation = resources.isBound("clientId") && !forceClientIdAllocation && !preSelectedFormNamesService.isPreselected(clientId);
        assignedClientId = avoidAllocation ? clientId : javaScriptSupport.allocateClientId(id);
        
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
        if (!disabled)
            processSubmission(controlName);
    }

    /**
     * Method implemented by subclasses to actually do the work of processing the submission of the form. The element's
     * controlName property will already have been set. This method is only invoked if the field is <strong>not
     * {@link #isDisabled() disabled}</strong>.
     *
     * @param controlName
     *         the control name of the rendered element (used to find the correct parameter in the request)
     */
    protected abstract void processSubmission(String controlName);

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
     * In addition, may invoke the <code>core/fields:showValidationError</code> function to present
     * the field's error (if it has one) to the user.
     */
    @AfterRender
    final void afterDecorator()
    {
        decorator.afterField(this);

        String error = validationTracker.getError(this);

        if (error != null)
        {
            javaScriptSupport.require("t5/core/fields").invoke("showValidationError").with(assignedClientId, error);
        }
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

    // This is set to true for some unit test.
    private boolean beanValidationDisabled = false;

    protected void putPropertyNameIntoBeanValidationContext(String parameterName)
    {
        if (beanValidationDisabled) { return; }

        String propertyName = ((InternalComponentResources) resources).getPropertyName(parameterName);

        BeanValidationContext beanValidationContext = environment.peek(BeanValidationContext.class);

        if (beanValidationContext == null)
            return;

        // If field is inside BeanEditForm, then property is already set
        if (beanValidationContext.getCurrentProperty() == null)
        {
            beanValidationContext.setCurrentProperty(propertyName);
        }
    }

    protected void removePropertyNameFromBeanValidationContext()
    {
        if (beanValidationDisabled) { return; }

        BeanValidationContext beanValidationContext = environment.peek(BeanValidationContext.class);

        if (beanValidationContext == null)
            return;

        beanValidationContext.setCurrentProperty(null);
    }
}
