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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.data.BlankOption;
import org.apache.tapestry5.corelib.data.SecureOption;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.AbstractEventContext;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.util.CaptureResultCallback;
import org.apache.tapestry5.internal.util.SelectModelRenderer;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.util.EnumSelectModel;

import java.util.Collections;
import java.util.List;

/**
 * Select an item from a list of values, using an [X]HTML &lt;select&gt; element on the client side. Any validation
 * decorations will go around the entire &lt;select&gt; element.
 *
 * A core part of this component is the {@link ValueEncoder} (the encoder parameter) that is used to convert between
 * server-side values and unique client-side strings. In some cases, a {@link ValueEncoder} can be generated automatically from
 * the type of the value parameter. The {@link ValueEncoderSource} service provides an encoder in these situations; it
 * can be overridden by binding the encoder parameter, or extended by contributing a {@link ValueEncoderFactory} into the
 * service's configuration.
 *
 * @tapestrydoc
 */
@Events(
        {EventConstants.VALIDATE, EventConstants.VALUE_CHANGED + " when 'zone' parameter is bound"})
public class Select extends AbstractField
{
    public static final String CHANGE_EVENT = "change";

    private class Renderer extends SelectModelRenderer
    {

        public Renderer(MarkupWriter writer)
        {
            super(writer, encoder, raw);
        }

        @Override
        protected boolean isOptionSelected(OptionModel optionModel, String clientValue)
        {
            return isSelected(clientValue);
        }
    }

    /**
     * A ValueEncoder used to convert the server-side object provided by the
     * "value" parameter into a unique client-side string (typically an ID) and
     * back. Note: this parameter may be OMITTED if Tapestry is configured to
     * provide a ValueEncoder automatically for the type of property bound to
     * the "value" parameter.
     *
     * @see ValueEncoderSource
     */
    @Parameter
    private ValueEncoder encoder;

    /**
     * Controls whether the submitted value is validated to be one of the values in
     * the {@link SelectModel}. If "never", then no such validation is performed,
     * theoretically allowing a selection to be made that was not presented to
     * the user.  Note that an "always" value here requires the SelectModel to
     * still exist (or be created again) when the form is submitted, whereas a
     * "never" value does not.  Defaults to "auto", which causes the validation
     * to occur only if the SelectModel is present (not null) when the form is
     * submitted.
     *
     * @since 5.4
     */
    @Parameter(value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.VALIDATE_WITH_MODEL, defaultPrefix = BindingConstants.LITERAL)
    private SecureOption secure;

    /**
     * If true, then the provided {@link org.apache.tapestry5.SelectModel} labels will be written raw (no escaping of
     * embedded HTML entities); it becomes the callers responsibility to escape any such entities.
     *
     * @since 5.4
     */
    @Parameter(value = "false")
    private boolean raw;

    /**
     * The model used to identify the option groups and options to be presented to the user. This can be generated
     * automatically for Enum types.
     */
    @Parameter(required = true, allowNull = false)
    private SelectModel model;

    /**
     * Controls whether an additional blank option is provided. The blank option precedes all other options and is never
     * selected. The value for the blank option is always the empty string, the label may be the blank string; the
     * label is from the blankLabel parameter (and is often also the empty string).
     */
    @Parameter(value = "auto", defaultPrefix = BindingConstants.LITERAL)
    private BlankOption blankOption;

    /**
     * The label to use for the blank option, if rendered. If not specified, the container's message catalog is
     * searched for a key, <code><em>id</em>-blanklabel</code>.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String blankLabel;

    @Inject
    private Request request;

    @Environmental
    private ValidationTracker tracker;

    /**
     * Performs input validation on the value supplied by the user in the form submission.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    private FieldValidator<Object> validate;

    /**
     * The value to read or update.
     */
    @Parameter(required = true, principal = true, autoconnect = true)
    private Object value;

    /**
     * Binding the zone parameter will cause any change of Select's value to be handled as an Ajax request that updates
     * the
     * indicated zone. The component will trigger the event {@link EventConstants#VALUE_CHANGED} to inform its
     * container that Select's value has changed.
     *
     * @since 5.2.0
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    /**
     * The context for the "valueChanged" event triggered by this component (optional parameter).
     * This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods. The first parameter of the context passed to "valueChanged" event handlers will
     * still be the selected value chosen by the user, so the context passed through this parameter
     * will be added from the second position on.
     *
     * @since 5.4
     */
    @Parameter
    private Object[] context;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    @Environmental
    private FormSupport formSupport;

    @Inject
    private JavaScriptSupport javascriptSupport;

    @Inject
    private TypeCoercer typeCoercer;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    private String selectedClientValue;

    private boolean isSelected(String clientValue)
    {
        return TapestryInternalUtils.isEqual(clientValue, selectedClientValue);
    }

    @SuppressWarnings(
            {"unchecked"})
    @Override
    protected void processSubmission(String controlName)
    {
        String submittedValue = request.getParameter(controlName);

        tracker.recordInput(this, submittedValue);

        Object selectedValue;

        try
        {
            selectedValue = toValue(submittedValue);
        } catch (ValidationException ex)
        {
            // Really, this will just be the logic related to the new (in 5.4) secure
            // parameter:

            tracker.recordError(this, ex.getMessage());
            return;
        }

        putPropertyNameIntoBeanValidationContext("value");

        try
        {
            fieldValidationSupport.validate(selectedValue, resources, validate);

            value = selectedValue;
        } catch (ValidationException ex)
        {
            tracker.recordError(this, ex.getMessage());
        }

        removePropertyNameFromBeanValidationContext();
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    void beginRender(MarkupWriter writer)
    {
        writer.element("select",
                "name", getControlName(),
                "id", getClientId(),
                "class", cssClass);

        putPropertyNameIntoBeanValidationContext("value");

        validate.render(writer);

        removePropertyNameFromBeanValidationContext();

        resources.renderInformalParameters(writer);

        decorateInsideField();

        // Disabled is via a mixin

        if (this.zone != null)
        {
            javaScriptSupport.require("t5/core/select");

            Link link = resources.createEventLink(CHANGE_EVENT, context);

            writer.attributes(
                    "data-update-zone", zone,
                    "data-update-url", link);
        }
    }

    Object onChange(final EventContext context,
                    @RequestParameter(value = "t:selectvalue", allowBlank = true) final String selectValue)
            throws ValidationException
    {
        final Object newValue = toValue(selectValue);

        CaptureResultCallback<Object> callback = new CaptureResultCallback<Object>();


        EventContext newContext = new AbstractEventContext() {

          @Override
          public int getCount() {
            return context.getCount() + 1;
          }

          @Override
          public <T> T get(Class<T> desiredType, int index) {
            if (index == 0)
            {
                return typeCoercer.coerce(newValue, desiredType);
            }
            return context.get(desiredType, index-1);
          }
        };

        this.resources.triggerContextEvent(EventConstants.VALUE_CHANGED, newContext, callback);

        this.value = newValue;

        return callback.getResult();
    }

    protected Object toValue(String submittedValue) throws ValidationException
    {
        if (InternalUtils.isBlank(submittedValue))
        {
            return null;
        }

        // can we skip the check for the value being in the model?

        SelectModel selectModel = typeCoercer.coerce(((InternalComponentResources) resources)
            .getBinding("model").get(), SelectModel.class);
        if (secure == SecureOption.NEVER || (secure == SecureOption.AUTO && selectModel == null))
        {
            return encoder.toValue(submittedValue);
        }

        // for entity types the SelectModel may be unintentionally null when the form is submitted
        if (selectModel == null)
        {
            throw new ValidationException("Model is null when validating submitted option." +
                    " To fix: persist the SeletModel or recreate it upon form submission," +
                    " or change the 'secure' parameter.");
        }

        return findValueInModel(submittedValue);
    }

    private Object findValueInModel(String submittedValue) throws ValidationException
    {

        Object asSubmitted = encoder.toValue(submittedValue);

        // The visitor would be nice if it had the option to abort the visit
        // early.

        if (findInOptions(model.getOptions(), asSubmitted))
        {
            return asSubmitted;
        }

        if (model.getOptionGroups() != null)
        {
            for (OptionGroupModel og : model.getOptionGroups())
            {
                if (findInOptions(og.getOptions(), asSubmitted))
                {
                    return asSubmitted;
                }
            }
        }

        throw new ValidationException("Selected option is not listed in the model.");
    }

    private boolean findInOptions(List<OptionModel> options, Object asSubmitted)
    {
        if (options == null)
        {
            return false;
        }

        // See TAP5-2184: Sometimes the SelectModel option values are Strings even though the
        // submitted value (decoded by the ValueEncoder) are another type (e.g., numeric). In that case,
        // pass each OptionModel value through the ValueEncoder for a comparison.
        boolean alsoCompareDecodedModelValue = !(asSubmitted instanceof String);

        for (OptionModel om : options)
        {
            Object modelValue = om.getValue();
            if (modelValue.equals(asSubmitted))
            {
                return true;
            }

            if (alsoCompareDecodedModelValue && (modelValue instanceof String))
            {
                Object decodedModelValue = encoder.toValue(modelValue.toString());

                if (decodedModelValue.equals(asSubmitted))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static <T> List<T> orEmpty(List<T> list)
    {
        if (list == null)
        {
            return Collections.emptyList();
        }

        return list;
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

        if (valueType == null)
            return null;

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

        if (containerMessages.contains(key))
            return containerMessages.get(key);

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

        if (selectedClientValue == null)
            selectedClientValue = value == null ? null : encoder.toClient(value);

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

    void setRaw(boolean b)
    {
        raw = b;
    }
}
