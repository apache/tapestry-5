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

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.corelib.mixins.DiscardBody;
import org.apache.tapestry5.corelib.mixins.RenderInformals;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.BeanValidationContext;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.FormControlNameManager;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.io.Serializable;

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
     * Used to explicitly set the client-side id of the element for this component. Normally this is not
     * bound (or null) and {@link org.apache.tapestry5.services.javascript.JavaScriptSupport#allocateClientId(org.apache.tapestry5.ComponentResources)}
     * is used to generate a unique client-id based on the component's id. In some cases, when creating client-side
     * behaviors, it is useful to explicitly set a unique id for an element using this parameter.
     * 
     * Certain values, such as "submit", "method", "reset", etc., will cause client-side conflicts and are not allowed; using such will
     * cause a runtime exception.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String clientId;

    /**
     * A rarely used option that indicates that the actual client id should start with the clientId parameter (if non-null)
     * but should still pass that Id through {@link org.apache.tapestry5.services.javascript.JavaScriptSupport#allocateClientId(String)}
     * to generate the final id.
     * 
     * An example of this are the components used inside a {@link org.apache.tapestry5.corelib.components.BeanEditor} which
     * will specify a clientId (based on the property name) but still require that it be unique.
     * 
     * Defaults to false.
     *
     * @since 5.4
     */
    @Parameter
    private boolean ensureClientIdUnique;


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
    private FormControlNameManager formControlNameManager;

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
        // Often, these controlName and clientId will end up as the same value. There are many
        // exceptions, including a form that renders inside a loop, or a form inside a component
        // that is used multiple times.

        if (formSupport == null)
            throw new RuntimeException(String.format("Component %s must be enclosed by a Form component.",
                    resources.getCompleteId()));

        assignedClientId = allocateClientId();

        String controlName = formSupport.allocateControlName(assignedClientId);

        formSupport.storeAndExecute(this, new Setup(controlName));
        formSupport.store(this, PROCESS_SUBMISSION_ACTION);
    }

    private String allocateClientId()
    {
        if (clientId == null)
        {
            return javaScriptSupport.allocateClientId(resources);
        }


        if (ensureClientIdUnique)
        {
            return javaScriptSupport.allocateClientId(clientId);
        } else
        {
            // See https://issues.apache.org/jira/browse/TAP5-1632
            // Basically, on the client, there can be a convenience lookup inside a HTMLFormElement
            // by id OR name; so an id of "submit" (for example) will mask the HTMLFormElement.submit()
            // function.

            if (formControlNameManager.isReserved(clientId))
            {
                throw new TapestryException(String.format(
                        "The value '%s' for parameter clientId is not allowed as it causes a naming conflict in the client-side DOM. " +
                                "Select an id not in the list: %s.",
                        clientId,
                        InternalUtils.joinSorted(formControlNameManager.getReservedNames())), this, null);
            }
        }

        return clientId;
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
        if (beanValidationDisabled)
        {
            return;
        }

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
        if (beanValidationDisabled)
        {
            return;
        }

        BeanValidationContext beanValidationContext = environment.peek(BeanValidationContext.class);

        if (beanValidationContext == null)
            return;

        beanValidationContext.setCurrentProperty(null);
    }
}
