// Copyright 2007-2013 The Apache Software Foundation
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

package org.apache.tapestry5.upload.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadedFile;

import java.util.Locale;

/**
 * A component to upload a file.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Events(EventConstants.VALIDATE)
public class Upload extends AbstractField
{
    public static final String MULTIPART_ENCTYPE = "multipart/form-data";

    /**
     * The uploaded file. Note: This is only guaranteed to be valid while processing the form submission. Subsequently
     * the content may have been cleaned up.
     */
    @Parameter(required = true, principal = true, autoconnect = true)
    private UploadedFile value;

    /**
     * The object that will perform input validation. The "validate:" binding prefix is generally used to provide this
     * object in a declarative fashion.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate;

    @Inject
    private MultipartDecoder decoder;

    @Inject
    private Locale locale;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    /**
     * Computes a default value for the "validate" parameter using {@link FieldValidatorDefaultSource}.
     */
    final Binding defaultValidate()
    {
        return defaultProvider.defaultValidatorBinding("value", resources);
    }

    public Upload()
    {
    }

    // For testing
    Upload(UploadedFile value, FieldValidator<Object> validate, MultipartDecoder decoder, ValidationTracker tracker,
           ComponentResources resources, FieldValidationSupport fieldValidationSupport)
    {
        this.value = value;
        if (validate != null)
        {
            this.validate = validate;
        }
        this.decoder = decoder;
        this.validationTracker = tracker;
        this.resources = resources;
        this.fieldValidationSupport = fieldValidationSupport;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void processSubmission(String controlName)
    {
        UploadedFile uploaded = decoder.getFileUpload(controlName);

        if (uploaded != null && (uploaded.getFileName() == null || uploaded.getFileName().length() == 0))
        {
            uploaded = null;
        }

        try
        {
            fieldValidationSupport.validate(uploaded, resources, validate);
        } catch (ValidationException ex)
        {
            validationTracker.recordError(this, ex.getMessage());
        }

        value = uploaded;
    }

    /**
     * Render the upload tags.
     *
     * @param writer
     *         Writer to output markup
     */
    protected void beginRender(MarkupWriter writer)
    {
        formSupport.setEncodingType(MULTIPART_ENCTYPE);

        writer.element("input", "type", "file", "name", getControlName(), "id", getClientId(), "class", cssClass);

        validate.render(writer);

        resources.renderInformalParameters(writer);

        decorateInsideField();

        // TAPESTRY-2453
        if (request.isXHR())
        {
            javaScriptSupport.require("t5/core/injected-upload").with(getClientId());
        }
    }

    /** @since 5.4 */
    @Override
    public boolean isRequired()
    {
        return validate.isRequired();
    }

    public void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    public UploadedFile getValue()
    {
        return value;
    }

    Upload injectDecorator(ValidationDecorator decorator)
    {
        setDecorator(decorator);

        return this;
    }

    Upload injectRequest(Request request)
    {
        this.request = request;

        return this;
    }

    Upload injectFormSupport(FormSupport formSupport)
    {
        // We have our copy ...
        this.formSupport = formSupport;

        // As does AbstractField
        setFormSupport(formSupport);

        return this;
    }

    Upload injectFieldValidator(FieldValidator<Object> validator)
    {
        this.validate = validator;

        return this;
    }
}
