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

package org.apache.tapestry.corelib.base;

import java.io.Serializable;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Mixin;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SetupRender;
import org.apache.tapestry.corelib.mixins.DiscardBody;
import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.corelib.mixins.RenderInformals;
import org.apache.tapestry.services.ComponentDefaultProvider;
import org.apache.tapestry.services.FormSupport;

/**
 * Provides initialization of the clientId and elementName properties. In addition, adds the
 * {@link RenderInformals}, {@link RenderDisabled} and {@link DiscardBody} mixins.
 */
public abstract class AbstractField implements Field
{
    /**
     * The user presentable label for the field. If not provided, a reasonable label is generated
     * from the component's id, first by looking for a message key named "id-label" (substituting
     * the component's actual id), then by converting the actual id to a presentable string (for
     * example, "userId" to "User Id").
     */
    @Parameter(defaultPrefix = "literal")
    private String _label;

    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side
     * behavior). Further, a disabled field ignores any value in the request when the form is
     * submitted.
     */
    @Parameter("false")
    private boolean _disabled;

    @SuppressWarnings("unused")
    @Mixin
    private RenderInformals _renderInformals;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled _renderDisabled;

    @SuppressWarnings("unused")
    @Mixin
    private DiscardBody _discardBody;

    @Environmental
    private ValidationDecorator _decorator;

    protected static final FieldValidator NOOP_VALIDATOR = new FieldValidator()
    {
        public void validate(Object value) throws ValidationException
        {
            // Do nothing
        }

        public void render(MarkupWriter writer)
        {
        }
    };

    static class SetupAction implements ComponentAction<AbstractField>, Serializable
    {
        private static final long serialVersionUID = 2690270808212097020L;

        private final String _elementName;

        public SetupAction(final String elementName)
        {
            _elementName = elementName;
        }

        public void execute(AbstractField component)
        {
            component.setupElementName(_elementName);
        }
    }

    static class ProcessSubmissionAction implements ComponentAction<AbstractField>, Serializable
    {
        private static final long serialVersionUID = -4346426414137434418L;

        public void execute(AbstractField component)
        {
            component.processSubmission();
        }
    }

    /** Used a shared instance for all types of fields, for efficiency. */
    private static final ProcessSubmissionAction PROCESS_SUBMISSION_ACTION = new ProcessSubmissionAction();

    /**
     * The id used to generate a page-unique client-side identifier for the component. If a
     * component renders multiple times, a suffix will be appended to the to id to ensure
     * uniqueness. The uniqued value may be accessed via the
     * {@link #getClientId() clientId property}.
     */
    @Parameter(value = "prop:componentResources.id", defaultPrefix = "literal")
    private String _clientId;

    private String _assignedClientId;

    private String _elementName;

    @Environmental
    private FormSupport _formSupport;

    @Environmental
    private PageRenderSupport _pageRenderSupport;

    @Inject
    private ComponentResources _resources;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    final String defaultLabel()
    {
        return _defaultProvider.defaultLabel(_resources);
    }

    public final String getLabel()
    {
        return _label;
    }

    @SetupRender
    final void setup()
    {
        // By default, use the component id as the (base) client id. If the clientid
        // parameter is bound, then that is the value to use.

        String id = _clientId;

        // Often, these elementName and _clientId will end up as the same value. There are many
        // exceptions, including a form that renders inside a loop, or a form inside a component
        // that is used multiple times.

        _assignedClientId = _pageRenderSupport.allocateClientId(id);
        String elementName = _formSupport.allocateElementName(id);

        _formSupport.storeAndExecute(this, new SetupAction(elementName));
        _formSupport.store(this, PROCESS_SUBMISSION_ACTION);
    }

    public final String getClientId()
    {
        return _assignedClientId;
    }

    public final String getElementName()
    {
        return _elementName;
    }

    public final boolean isDisabled()
    {
        return _disabled;
    }

    /**
     * Invoked from within a ComponentCommand callback, to restore the component's elementName.
     */
    private void setupElementName(String elementName)
    {
        _elementName = elementName;
    }

    private void processSubmission()
    {
        if (!_disabled) processSubmission(_formSupport, _elementName);
    }

    /**
     * Used by subclasses to create a default binding to a property of the container matching the
     * component id.
     * 
     * @return a binding to the property, or null if the container does not have a corresponding
     *         property
     */
    protected final Binding createDefaultParameterBinding(String parameterName)
    {
        return _defaultProvider.defaultBinding(parameterName, _resources);
    }

    /**
     * Method implemented by subclasses to actually do the work of processing the submission of the
     * form. The element's elementName property will already have been set. This method is only
     * invoked if the field is <strong>not {@link #isDisabled() disabled}</strong>.
     * 
     * @param formSupport
     *            support for the form submission, used to
     *            {@link FormSupport#getParameterValue(String) obtain submitted parameter values}.
     *            Passing this value in saves subclasses from having to (re)inject it.
     * @param elementName
     *            the name of the element (used to find the correct parameter in the request)
     */
    protected abstract void processSubmission(FormSupport formSupport, String elementName);

    @BeginRender
    final void beforeDecorator(MarkupWriter writer)
    {
        _decorator.beforeField(this);
    }

    @AfterRender
    final void afterDecorator(MarkupWriter writer)
    {
        _decorator.afterField(this);
    }

    protected final ValidationDecorator getValidationDecorator()
    {
        return _decorator;
    }
}
