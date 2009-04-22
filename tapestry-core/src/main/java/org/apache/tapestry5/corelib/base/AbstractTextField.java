// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.beaneditor.Width;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.Request;

import java.lang.annotation.Annotation;
import java.util.Locale;

/**
 * Abstract class for a variety of components that render some variation of a text field. Most of the hooks for user
 * input validation are in this class.
 * <p/>
 * In particular, all subclasses support the "toclient" and "parseclient" events.  These two events allow the normal
 * {@link Translator} (specified by the translate parameter, but often automatically derived by Tapestry) to be
 * augmented.
 * <p/>
 * If the component container (i.e., the page) provides an event handler method for the "toclient" event, and that
 * handler returns a non-null string, that will be the string value sent to the client. The context passed to the event
 * handler method is t he current value of the value parameter.
 * <p/>
 * Likewise, on a form submit, the "parseclient" event handler method will be passed the string provided by the client,
 * and may provide a non-null value as the parsed value.  Returning null allows the normal translator to operate.  The
 * event handler may also throw {@link org.apache.tapestry5.ValidationException}.
 */
@Events({ EventConstants.TO_CLIENT, EventConstants.VALIDATE, EventConstants.PARSE_CLIENT })
public abstract class AbstractTextField extends AbstractField
{
    /**
     * The value to be read and updated. This is not necessarily a string, a translator may be provided to convert
     * between client side and server side representations. If not bound, a default binding is made to a property of the
     * container matching the component's id. If no such property exists, then you will see a runtime exception due to
     * the unbound value parameter.
     */
    @Parameter(required = true, principal = true)
    private Object value;

    /**
     * The object which will perform translation between server-side and client-side representations. If not specified,
     * a value will usually be generated based on the type of the value parameter.
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.TRANSLATE)
    private FieldTranslator<Object> translate;

    /**
     * The object that will perform input validation (which occurs after translation). The validate binding prefix is
     * generally used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate;

    /**
     * Provider of annotations used for some defaults.  Annotation are usually provided in terms of the value parameter
     * (i.e., from the getter and/or setter bound to the value parameter).
     *
     * @see org.apache.tapestry5.beaneditor.Width
     */
    @Parameter
    private AnnotationProvider annotationProvider;

    /**
     * Defines how nulls on the server side, or sent from the client side, are treated. The selected strategy may
     * replace the nulls with some other value. The default strategy leaves nulls alone.  Another built-in strategy,
     * zero, replaces nulls with the value 0.
     */
    @Parameter(defaultPrefix = BindingConstants.NULLFIELDSTRATEGY, value = "default")
    private NullFieldStrategy nulls;

    @Environmental
    private ValidationTracker tracker;

    @Inject
    private ComponentResources resources;

    @Inject
    private Locale locale;

    @Inject
    private Request request;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    /**
     * Computes a default value for the "translate" parameter using {@link org.apache.tapestry5.services.ComponentDefaultProvider#defaultTranslator(String,
     * org.apache.tapestry5.ComponentResources)}.
     */
    final Binding defaultTranslate()
    {
        return defaultProvider.defaultTranslatorBinding("value", resources);
    }

    final AnnotationProvider defaultAnnotationProvider()
    {
        return new AnnotationProvider()
        {
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return resources.getParameterAnnotation("value", annotationClass);
            }
        };
    }

    /**
     * Computes a default value for the "validate" parameter using {@link org.apache.tapestry5.services.FieldValidatorDefaultSource}.
     */
    final Binding defaultValidate()
    {
        return defaultProvider.defaultValidatorBinding("value", resources);
    }

    /**
     * The default value is a property of the container whose name matches the component's id. May return null if the
     * container does not have a matching property.
     *
     * @deprecated Likely to be removed in the future, use {@link org.apache.tapestry5.annotations.Parameter#autoconnect()}
     *             instead
     */
    final Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    @SuppressWarnings({ "unchecked" })
    @BeginRender
    void begin(MarkupWriter writer)
    {
        String value = tracker.getInput(this);

        // If this is a response to a form submission, and the user provided a value.
        // then send that exact value back at them.

        if (value == null)
        {
            // Otherwise, get the value from the parameter ...
            // Then let the translator and or various triggered events get it into
            // a format ready to be sent to the client.

            value = fieldValidationSupport.toClient(this.value, resources, translate, nulls);
        }

        writeFieldTag(writer, value);

        translate.render(writer);
        validate.render(writer);

        resources.renderInformalParameters(writer);

        decorateInsideField();
    }

    /**
     * Invoked from {@link #begin(MarkupWriter)} to write out the element and attributes (typically, &lt;input&gt;). The
     * {@linkplain AbstractField#getControlName() controlName} and {@linkplain AbstractField#getClientId() clientId}
     * properties will already have been set or updated.
     * <p/>
     * Generally, the subclass will invoke {@link MarkupWriter#element(String, Object[])}, and will be responsible for
     * including an {@link AfterRender} phase method to invoke {@link MarkupWriter#end()}.
     *
     * @param writer markup write to send output to
     * @param value  the value (either obtained and translated from the value parameter, or obtained from the tracker)
     */
    protected abstract void writeFieldTag(MarkupWriter writer, String value);

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void processSubmission(String elementName)
    {
        String rawValue = request.getParameter(elementName);

        tracker.recordInput(this, rawValue);

        try
        {
            Object translated = fieldValidationSupport.parseClient(rawValue, resources, translate, nulls);

            fieldValidationSupport.validate(translated, resources, validate);

            // If the value provided is blank and we're ignoring blank input (i.e. PasswordField),
            // then don't update the value parameter.

            if (!(ignoreBlankInput() && InternalUtils.isBlank(rawValue)))
                value = translated;
        }
        catch (ValidationException ex)
        {
            tracker.recordError(this, ex.getMessage());
        }
    }

    /**
     * Should blank input be ignored (after validation)?  This will be true for {@link
     * org.apache.tapestry5.corelib.components.PasswordField}.
     */
    protected boolean ignoreBlankInput()
    {
        return false;
    }

    @Override
    public boolean isRequired()
    {
        return validate.isRequired();
    }

    /**
     * Looks for a {@link org.apache.tapestry5.beaneditor.Width} annotation and, if present, returns its value as a
     * string.
     *
     * @return the indicated width, or null if the annotation is not present
     */
    protected final String getWidth()
    {
        Width width = annotationProvider.getAnnotation(Width.class);

        if (width == null) return null;

        return Integer.toString(width.value());
    }
}
