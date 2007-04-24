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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.OptionGroupModel;
import org.apache.tapestry.OptionModel;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.annotations.BeforeRenderTemplate;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.util.EnumSelectModel;
import org.apache.tapestry.util.EnumValueEncoder;

/**
 * Select an item from a list of values, using an [X]HTML &lt;select&gt; element on the client side.
 * An validation decorations will go around the entire &lt;select&gt; element.
 */
public final class Select extends AbstractField
{
    /** The value to read or update. */
    @Parameter(required = true, principal = true)
    private Object _value;

    /**
     * The default encoder encodes strings, passing them to the client and back unchanged.
     */
    @Parameter
    private ValueEncoder _encoder = new ValueEncoder<String>()
    {
        public String toClient(String value)
        {
            return value;
        }

        public String toValue(String primaryKey)
        {
            // We don't do a conversion here, so it stays a String. When that String is assigned to
            // _value, it will be coerced to the appropriate type (if possible) or an exception
            // will be thrown.

            return primaryKey;
        }
    };

    // Maybe this should default to property "<componentId>Model"?

    /**
     * The model used to identify the option groups and options to be presented to the user. This
     * can be generated automatically for Enum types.
     */
    @Parameter(required = true)
    private SelectModel _model;

    /** Performs input validation on the value supplied by the user in the form submission. */
    @Parameter(defaultPrefix = "validate")
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> _validate = NOOP_VALIDATOR;

    @Inject("infrastructure:FieldValidatorDefaultSource")
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    @Environmental
    private ValidationTracker _tracker;

    @Inject
    private ComponentResources _resources;

    @Inject
    private Locale _locale;

    Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    /**
     * Computes a default value for the "validate" parameter using
     * {@link FieldValidatorDefaultSource}.
     */
    FieldValidator defaultValidate()
    {
        Class type = _resources.getBoundType("value");

        if (type == null)
            return null;

        return _fieldValidatorDefaultSource.createDefaultValidator(
                this,
                _resources.getId(),
                _resources.getContainerMessages(),
                _locale,
                type,
                _resources.getAnnotationProvider("value"));
    }

    @SuppressWarnings("unchecked")
    ValueEncoder defaultEncoder()
    {
        Class valueType = _resources.getBoundType("value");

        if (valueType == null)
            return null;

        if (Enum.class.isAssignableFrom(valueType))
            return new EnumValueEncoder(valueType);

        return null;
    }

    @SuppressWarnings("unchecked")
    SelectModel defaultModel()
    {
        Class valueType = _resources.getBoundType("value");

        if (valueType == null)
            return null;

        if (Enum.class.isAssignableFrom(valueType))
            return new EnumSelectModel(valueType, _resources.getContainerMessages());

        return null;
    }

    void beginRender(MarkupWriter writer)
    {
        writer.element("select", "name", getElementName(), "id", getClientId());

        // Disabled, informals via mixins
    }

    @BeforeRenderTemplate
    void options(MarkupWriter writer)
    {
        if (_model.getOptionGroups() != null)
        {
            for (OptionGroupModel group : _model.getOptionGroups())
            {
                writeOptionGroup(writer, group);
            }
        }

        writeOptions(writer, _model.getOptions());
    }

    private void writeOptionGroup(MarkupWriter writer, OptionGroupModel model)
    {
        writer.element("optgroup", "label", model.getLabel());

        writeDisabled(writer, model.isDisabled());
        writeAttributes(writer, model.getAttributes());

        writeOptions(writer, model.getOptions());

        writer.end(); // optgroup
    }

    @SuppressWarnings("unchecked")
    private void writeOptions(MarkupWriter writer, List<OptionModel> optionModels)
    {
        if (optionModels == null)
            return;

        for (OptionModel model : optionModels)
        {
            Object optionValue = model.getValue();

            String clientValue = _encoder.toClient(optionValue);

            writer.element("option", "value", clientValue);

            if (isOptionValueSelected(optionValue))
                writer.attributes("selected", "selected");

            writeDisabled(writer, model.isDisabled());
            writeAttributes(writer, model.getAttributes());

            writer.write(model.getLabel());

            writer.end(); // option
        }
    }

    boolean isOptionValueSelected(Object optionValue)
    {
        return _value == optionValue || (_value != null && _value.equals(optionValue));
    }

    private void writeDisabled(MarkupWriter writer, boolean disabled)
    {
        if (disabled)
            writer.attributes("disabled", "disabled");
    }

    private void writeAttributes(MarkupWriter writer, Map<String, String> attributes)
    {
        if (attributes == null)
            return;

        for (Map.Entry<String, String> e : attributes.entrySet())
            writer.attributes(e.getKey(), e.getValue());
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    @Override
    protected void processSubmission(FormSupport formSupport, String elementName)
    {
        String primaryKey = formSupport.getParameterValue(elementName);

        Object selectedValue = _encoder.toValue(primaryKey);

        try
        {
            _validate.validate(selectedValue);

            _value = selectedValue;
        }
        catch (ValidationException ex)
        {
            _tracker.recordError(this, ex.getMessage());
            return;
        }
    }

    // For testing.

    void setValue(Object value)
    {
        _value = value;
    }

    void setModel(SelectModel model)
    {
        _model = model;
    }
}
