// Copyright 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.data.BlankOption;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.internal.util.SelectModelRenderer;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.util.EnumSelectModel;
import org.slf4j.Logger;

import java.util.Locale;

/**
 * Select an item from a list of values, using an [X]HTML &lt;select&gt; element on the client side. An validation
 * decorations will go around the entire &lt;select&gt; element.
 * <p/>
 * A core part of this component is the {@link ValueEncoder} (the encoder parameter) that is used to convert between
 * server-side values and client-side strings. In many cases, a {@link ValueEncoder} can be generated automatically from
 * the type of the value parameter. The {@link ValueEncoderSource} service provides an encoder in these situations; it
 * can be overriden by binding the encoder parameter, or extended by contributing a {@link ValueEncoderFactory} into the
 * service's configuration.
 */
@Events({EventConstants.VALIDATE, EventConstants.VALUE_CHANGED + " when 'zone' parameter is bound"})
public class Select extends AbstractField
{
    public static final String FORM_COMPONENTID_PARAMETER = "t:formcomponentid";
    public static final String CHANGE_EVENT = "change";
    
    private class Renderer extends SelectModelRenderer
    {

        public Renderer(MarkupWriter writer)
        {
            super(writer, encoder);
        }

        @Override
        protected boolean isOptionSelected(OptionModel optionModel, String clientValue)
        {
            return isSelected(clientValue);
        }
    }

    /**
     * Allows a specific implementation of {@link ValueEncoder} to be supplied. This is used to create client-side
     * string values for the different options.
     *
     * @see ValueEncoderSource
     */
    @Parameter
    private ValueEncoder encoder;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private Locale locale;

    // Maybe this should default to property "<componentId>Model"?
    /**
     * The model used to identify the option groups and options to be presented to the user. This can be generated
     * automatically for Enum types.
     */
    @Parameter(required = true, allowNull = false)
    private SelectModel model;

    /**
     * Controls whether an additional blank option is provided. The blank option precedes all other options and is never
     * selected.  The value for the blank option is always the empty string, the label may be the blank string; the
     * label is from the blankLabel parameter (and is often also the empty string).
     */
    @Parameter(value = "auto", defaultPrefix = BindingConstants.LITERAL)
    private BlankOption blankOption;

    /**
     * The label to use for the blank option, if rendered.  If not specified, the container's message catalog is
     * searched for a key, <code><em>id</em>-blanklabel</code>.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String blankLabel;

    @Inject
    private Request request;

    @Inject
    private ComponentResources resources;

    @Environmental
    private ValidationTracker tracker;

    /**
     * Performs input validation on the value supplied by the user in the form submission.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate;

    /**
     * The value to read or update.
     */
    @Parameter(required = true, principal = true, autoconnect = true)
    private Object value;
    
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    @Inject
    private FieldValidationSupport fieldValidationSupport;
    
    @Environmental
    private FormSupport formSupport;

    @Inject
    private Environment environment;

    @Inject
    private RenderSupport renderSupport;

    @Inject
    private ComponentResources componentResources;

    @Inject
    private HiddenFieldLocationRules rules;

    @Inject
    private Logger logger;

    @Inject
    private ClientDataEncoder clientDataEncoder;

    @Inject
    private ComponentSource componentSource;

    @Inject
    private PageRenderQueue pageRenderQueue;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    private String selectedClientValue;

    private boolean isSelected(String clientValue)
    {
        return TapestryInternalUtils.isEqual(clientValue, selectedClientValue);
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void processSubmission(String elementName)
    {
        String submittedValue = request.getParameter(elementName);

        tracker.recordInput(this, submittedValue);

        Object selectedValue = toValue(submittedValue);

        try
        {
            fieldValidationSupport.validate(selectedValue, resources, validate);

            value = selectedValue;
        }
        catch (ValidationException ex)
        {
            tracker.recordError(this, ex.getMessage());
        }
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    void beginRender(MarkupWriter writer)
    {
        writer.element("select", "name", getControlName(), "id", getClientId());

        validate.render(writer);

        resources.renderInformalParameters(writer);

        // Disabled is via a mixin
        
        if (this.zone != null) 
        {
            final Link link = this.resources.createEventLink(CHANGE_EVENT);

            link.addParameter(FORM_COMPONENTID_PARAMETER, this.formSupport.getFormComponentId());

            final JSONArray spec = new JSONArray();
            spec.put("change");
            spec.put(getClientId());
            spec.put(this.zone);
            spec.put(link.toAbsoluteURI());

            this.renderSupport.addInit("updateZoneOnEvent", spec);
        }
    }
    
    Object onChange() 
    {
        final String formId = this.request.getParameter(FORM_COMPONENTID_PARAMETER);

        final String changedValue = this.request.getParameter("t:selectvalue");

        final Object newValue = toValue(changedValue);

        final Holder<Object> holder = Holder.create();

        final ComponentEventCallback callback = new ComponentEventCallback() 
        {
            public boolean handleResult(final Object result) 
            {

                holder.put(result);

                Select.this.value = newValue;

                return true;
            }
        };

        this.componentResources.triggerEvent(EventConstants.VALUE_CHANGED, new Object[] { newValue }, callback);

        final PartialMarkupRendererFilter filter = new PartialMarkupRendererFilter() 
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer) 
            {

                HiddenFieldPositioner hiddenFieldPositioner = new HiddenFieldPositioner(writer, Select.this.rules);

                final ComponentActionSink actionSink = new ComponentActionSink(Select.this.logger, Select.this.clientDataEncoder);

                final Form form = (Form) Select.this.componentSource.getComponent(formId);

                FormSupport formSupport = form.createRenderTimeFormSupport(form.getClientId(), actionSink, new IdAllocator());

                Select.this.environment.push(FormSupport.class, formSupport);
                Select.this.environment.push(ValidationTracker.class, new ValidationTrackerImpl());

                renderer.renderMarkup(writer, reply);

                Select.this.environment.pop(ValidationTracker.class);
                Select.this.environment.pop(FormSupport.class);

                hiddenFieldPositioner.getElement().attributes("type", "hidden", "name", Form.FORM_DATA, 
                        "value", actionSink.getClientData());

            }
        };

        this.pageRenderQueue.addPartialMarkupRendererFilter(filter);

        return holder.get();
    }

    protected Object toValue(final String submittedValue) 
    {
        return InternalUtils.isBlank(submittedValue) ? null : this.encoder.toValue(submittedValue);
    }

    @SuppressWarnings("unchecked")
    ValueEncoder defaultEncoder()
    {
        return defaultProvider.defaultValueEncoder("value", resources);
    }

    @SuppressWarnings("unchecked")
    SelectModel defaultModel()
    {
        Class valueType = resources.getBoundType("value");

        if (valueType == null) return null;

        if (Enum.class.isAssignableFrom(valueType))
            return new EnumSelectModel(valueType, resources.getContainerMessages());

        return null;
    }

    /**
     * Computes a default value for the "validate" parameter using {@link FieldValidatorDefaultSource}.
     */
    Binding defaultValidate()
    {
        return defaultProvider.defaultValidatorBinding("value", resources);
    }

    Object defaultBlankLabel()
    {
        Messages containerMessages = resources.getContainerMessages();

        String key = resources.getId() + "-blanklabel";

        if (containerMessages.contains(key)) return containerMessages.get(key);

        return null;
    }

    /**
     * Renders the options, including the blank option.
     */
    @BeforeRenderTemplate
    void options(MarkupWriter writer)
    {
        selectedClientValue = tracker.getInput(this);

        // Use the value passed up in the form submission, if available.
        // Failing that, see if there is a current value (via the value parameter), and
        // convert that to a client value for later comparison.

        if (selectedClientValue == null) selectedClientValue = value == null ? null : encoder.toClient(value);

        if (showBlankOption())
        {
            writer.element("option", "value", "");
            writer.write(blankLabel);
            writer.end();
        }


        SelectModelVisitor renderer = new Renderer(writer);

        model.visit(renderer);
    }

    @Override
    public boolean isRequired()
    {
        return validate.isRequired();
    }

    private boolean showBlankOption()
    {
        switch (blankOption)
        {
            case ALWAYS:
                return true;

            case NEVER:
                return false;

            default:
                return !isRequired();
        }
    }

    // For testing.

    void setModel(SelectModel model)
    {
        this.model = model;
        blankOption = BlankOption.NEVER;
    }

    void setValue(Object value)
    {
        this.value = value;
    }

    void setValueEncoder(ValueEncoder encoder)
    {
        this.encoder = encoder;
    }

    void setValidationTracker(ValidationTracker tracker)
    {
        this.tracker = tracker;
    }

    void setBlankOption(BlankOption option, String label)
    {
        blankOption = option;
        blankLabel = label;
    }
}
