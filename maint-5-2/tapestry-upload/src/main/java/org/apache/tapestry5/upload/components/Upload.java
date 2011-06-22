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

package org.apache.tapestry5.upload.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadedFile;

import java.util.Locale;

/**
 * A component to upload a file.
 */
@SuppressWarnings({ "UnusedDeclaration" })
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

    @Environmental
    private ValidationTracker tracker;

    @Inject
    private MultipartDecoder decoder;

    @Environmental
    private FormSupport formSupport;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private ComponentResources resources;

    @Inject
    private Locale locale;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    @Inject
    @Path("upload.js")
    private Asset uploadScript;

    @Inject
    private Request request;

    @Environmental
    private RenderSupport renderSupport;

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

        // TAPESTRY-2453
        if (request.isXHR())
        {
            renderSupport.addScriptLink(uploadScript);
            renderSupport.addInit("injectedUpload", getClientId());
        }
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

    Upload injectFieldValidator(FieldValidator validator)
    {
        this.validate = validator;

        return this;
    }
}
