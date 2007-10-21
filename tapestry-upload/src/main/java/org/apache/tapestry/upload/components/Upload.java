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

package org.apache.tapestry.upload.components;

import java.util.Locale;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.FieldValidatorDefaultSource;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.upload.services.MultipartDecoder;
import org.apache.tapestry.upload.services.UploadedFile;

/**
 * A component to upload a file.
 */
public class Upload extends AbstractField
{
    public static final String MULTIPART_ENCTYPE = "multipart/form-data";

    /**
     * The uploaded file. Note: This is only guaranteed to be valid while processing the form
     * submission. Subsequently the content may have been cleaned up.
     */
    @Parameter(required = true, principal = true)
    private UploadedFile _value;

    /**
     * The object that will perform input validation. The "validate:" binding prefix is generally
     * used to provide this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = "validate")
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> _validate = NOOP_VALIDATOR;

    @Environmental
    private ValidationTracker _tracker;

    @Inject
    private MultipartDecoder _decoder;

    @Environmental
    private FormSupport _formSupport;

    @Inject
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    @Inject
    private ComponentResources _resources;

    @Inject
    private Locale _locale;

    /**
     * Computes a default value for the "validate" parameter using
     * {@link FieldValidatorDefaultSource}.
     */
    final FieldValidator defaultValidate()
    {
        Class type = _resources.getBoundType("value");

        if (type == null) return null;

        return _fieldValidatorDefaultSource.createDefaultValidator(
                this,
                _resources.getId(),
                _resources.getContainerMessages(),
                _locale,
                type,
                _resources.getAnnotationProvider("value"));
    }

    public Upload()
    {
    }

    // For testing
    Upload(UploadedFile value, FieldValidator<Object> validate, MultipartDecoder decoder,
            ValidationTracker tracker, ComponentResources resources)
    {
        _value = value;
        if (validate != null) _validate = validate;
        _decoder = decoder;
        _tracker = tracker;
        _resources = resources;
    }

    @Override
    protected void processSubmission(FormSupport formSupport, String elementName)
    {
        UploadedFile uploaded = _decoder.getFileUpload(elementName);

        if (uploaded != null)
        {
            if (uploaded.getFileName() == null || uploaded.getFileName().length() == 0)
                uploaded = null;
        }

        try
        {
            _validate.validate(uploaded);
        }
        catch (ValidationException ex)
        {
            _tracker.recordError(this, ex.getMessage());
        }

        _value = uploaded;
    }

    /**
     * Render the upload tags.
     * 
     * @param writer
     *            Writer to output markup
     */
    protected void beginRender(MarkupWriter writer)
    {
        _formSupport.setEncodingType(MULTIPART_ENCTYPE);

        writer.element("input", "type", "file", "name", getElementName(), "id", getClientId());

        _validate.render(writer);

        _resources.renderInformalParameters(writer);

        decorateInsideField();
    }

    public void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    public UploadedFile getValue()
    {
        return _value;
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
        _formSupport = formSupport;

        // As does AbstractField
        setFormSupport(formSupport);
    }
}
