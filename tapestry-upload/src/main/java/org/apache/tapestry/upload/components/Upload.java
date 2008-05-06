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

package org.apache.tapestry.upload.components;

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Mixin;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.upload.services.MultipartDecoder;
import org.apache.tapestry.upload.services.UploadedFile;

import java.util.Locale;

/**
 * A component to upload a file.
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class Upload extends AbstractField
{
    public static final String MULTIPART_ENCTYPE = "multipart/form-data";

    /**
     * The uploaded file. Note: This is only guaranteed to be valid while processing the form submission. Subsequently
     * the content may have been cleaned up.
     */
    @Parameter(required = true, principal = true)
    private UploadedFile value;

    /**
     * The object that will perform input validation. The "validate:" binding prefix is generally used to provide this
     * object in a declarative fashion.
     */
    @Parameter(defaultPrefix = "validate")
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate = NOOP_VALIDATOR;

    @Environmental
    private ValidationTracker tracker;

    @Inject
    private MultipartDecoder decoder;

    @Environmental
    private FormSupport formSupport;

    @Inject
    private FieldValidatorDefaultSource fieldValidatorDefaultSource;

    @Inject
    private ComponentResources resources;

    @Inject
    private Locale locale;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    /**
     * Computes a default value for the "validate" parameter using {@link FieldValidatorDefaultSource}.
     */
    final FieldValidator defaultValidate()
    {
        Class type = resources.getBoundType("value");

        if (type == null) return null;

        return fieldValidatorDefaultSource.createDefaultValidator(this, resources.getId(),
                                                                  resources.getContainerMessages(), locale, type,
                                                                  resources.getAnnotationProvider("value"));
    }

    public Upload()
    {
    }

    // For testing
    Upload(UploadedFile value, FieldValidator<Object> validate, MultipartDecoder decoder, ValidationTracker tracker,
           ComponentResources resources, FieldValidationSupport fieldValidationSupport)
    {
        this.value = value;
        if (validate != null) this.validate = validate;
        this.decoder = decoder;
        this.tracker = tracker;
        this.resources = resources;
        this.fieldValidationSupport = fieldValidationSupport;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    protected void processSubmission(String elementName)
    {
        UploadedFile uploaded = decoder.getFileUpload(elementName);

        if (uploaded != null)
        {
            if (uploaded.getFileName() == null || uploaded.getFileName().length() == 0) uploaded = null;
        }

        try
        {
            fieldValidationSupport.validate(uploaded, resources, validate);
        }
        catch (ValidationException ex)
        {
            tracker.recordError(this, ex.getMessage());
        }

        value = uploaded;
    }

    /**
     * Render the upload tags.
     *
     * @param writer Writer to output markup
     */
    protected void beginRender(MarkupWriter writer)
    {
        formSupport.setEncodingType(MULTIPART_ENCTYPE);

        writer.element("input", "type", "file", "name", getControlName(), "id", getClientId());

        validate.render(writer);

        resources.renderInformalParameters(writer);

        decorateInsideField();
    }

    public void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    public UploadedFile getValue()
    {
        return value;
    }

    Binding defaultValue()
    {
        return createDefaultParameterBinding("value");
    }

    void injectDecorator(ValidationDecorator decorator)
    {
        setDecorator(decorator);
    }

    void injectFormSupport(FormSupport formSupport)
    {
        // We have our copy ...
        this.formSupport = formSupport;

        // As does AbstractField
        setFormSupport(formSupport);
    }
}
