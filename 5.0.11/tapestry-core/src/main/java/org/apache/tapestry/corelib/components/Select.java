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
import org.apache.tapestry.annotations.BeforeRenderTemplate;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Mixin;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.corelib.data.BlankOption;
import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.util.SelectModelRenderer;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.*;
import org.apache.tapestry.util.EnumSelectModel;

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
public final class Select extends AbstractField
{
    private class Renderer extends SelectModelRenderer
    {

        public Renderer(MarkupWriter writer)
        {
            super(writer, _encoder);
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
    private ValueEncoder _encoder;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    @Inject
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    @Inject
    private Locale _locale;

    // Maybe this should default to property "<componentId>Model"?
    /**
     * The model used to identify the option groups and options to be presented to the user. This can be generated
     * automatically for Enum types.
     */
    @Parameter(required = true)
    private SelectModel _model;

    /**
     * Controls whether an additional blank option is provided. The blank option precedes all other options and is never
     * selected.  The value for the blank option is always the empty string, the label may be the blank string; the
     * label is from the blankLabel parameter (and is often also the empty string).
     */
    @Parameter(value = "auto", defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private BlankOption _blankOption;

    /**
     * The label to use for the blank option, if rendered.  If not specified, the container's message catalog is
     * searched for a key, <code><em>id</em>-blanklabel</code>.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String _blankLabel;

    @Inject
    private Request _request;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private ValidationTracker _tracker;

    /**
     * Performs input validation on the value supplied by the user in the form submission.
     */
    @Parameter(defaultPrefix = "validate")
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> _validate = NOOP_VALIDATOR;

    /**
     * The value to read or update.
     */
    @Parameter(required = true, principal = true)
    private Object _value;

    @Inject
    private FieldValidationSupport _fieldValidationSupport;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled _renderDisabled;

    private String _selectedClientValue;

    private boolean isSelected(String clientValue)
    {
        return TapestryInternalUtils.isEqual(clientValue, _selectedClientValue);
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void processSubmission(String elementName)
    {
        String submittedValue = _request.getParameter(elementName);

        _tracker.recordInput(this, submittedValue);

        Object selectedValue = _encoder.toValue(submittedValue);

        try
        {
            _fieldValidationSupport.validate(selectedValue, _resources, _validate);

            _value = selectedValue;
        }
        catch (ValidationException ex)
        {
            _tracker.recordError(this, ex.getMessage());
        }
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    void beginRender(MarkupWriter writer)
    {
        writer.element("select", "name", getControlName(), "id", getClientId());

        _resources.renderInformalParameters(writer);

        // Disabled is via a mixin
    }

    @SuppressWarnings("unchecked")
    ValueEncoder defaultEncoder()
    {
        return _defaultProvider.defaultValueEncoder("value", _resources);
    }

    @SuppressWarnings("unchecked")
    SelectModel defaultModel()
    {
        Class valueType = _resources.getBoundType("value");

        if (valueType == null) return null;

        if (Enum.class.isAssignableFrom(valueType))
            return new EnumSelectModel(valueType, _resources.getContainerMessages());

        return null;
    }

    /**
     * Computes a default value for the "validate" parameter using {@link FieldValidatorDefaultSource}.
     */
    FieldValidator defaultValidate()
    {
        Class type = _resources.getBoundType("value");

        if (type == null) return null;

        return _fieldValidatorDefaultSource.createDefaultValidator(this, _resources.getId(),
                                                                   _resources.getContainerMessages(), _locale, type,
                                                                   _resources.getAnnotationProvider("value"));
    }

    Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    Object defaultBlankLabel()
    {
        Messages containerMessages = _resources.getContainerMessages();

        String key = _resources.getId() + "-blanklabel";

        if (containerMessages.contains(key)) return containerMessages.get(key);

        return null;
    }

    /**
     * Renders the options, including the blank option.
     */
    @BeforeRenderTemplate
    void options(MarkupWriter writer)
    {
        _selectedClientValue = _tracker.getInput(this);

        // Use the value passed up in the form submission, if available.
        // Failing that, see if there is a current value (via the value parameter), and
        // convert that to a client value for later comparison.

        if (_selectedClientValue == null) _selectedClientValue = _value == null ? null : _encoder.toClient(_value);

        if (showBlankOption())
        {
            writer.element("option", "value", "");
            writer.write(_blankLabel);
            writer.end();
        }


        SelectModelVisitor renderer = new Renderer(writer);

        _model.visit(renderer);
    }

    @Override
    public boolean isRequired()
    {
        return _validate.isRequired();
    }

    private boolean showBlankOption()
    {
        switch (_blankOption)
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
        _model = model;
        _blankOption = BlankOption.NEVER;
    }

    void setValue(Object value)
    {
        _value = value;
    }

    void setValueEncoder(ValueEncoder encoder)
    {
        _encoder = encoder;
    }

    void setValidationTracker(ValidationTracker tracker)
    {
        _tracker = tracker;
    }

    void setBlankOption(BlankOption option, String label)
    {
        _blankOption = option;
        _blankLabel = label;
    }


}
