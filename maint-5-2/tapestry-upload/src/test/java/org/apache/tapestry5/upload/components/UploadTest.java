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

package org.apache.tapestry5.upload.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.test.TapestryTestCase;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadedFile;
import static org.easymock.EasyMock.expectLastCall;
import org.testng.annotations.Test;

public class UploadTest extends TapestryTestCase
{

    @Test
    public void upload_is_field() throws Exception
    {
        assertTrue(Field.class.isAssignableFrom(Upload.class));
    }

    @Test
    public void begin_render_writes_input_tag() throws Exception
    {
        MarkupWriter writer = createMarkupWriter();
        writer.element("form");
        FormSupport formSupport = mockFormSupport();
        ComponentResources resources = mockComponentResources();
        FieldValidator validator = mockFieldValidator();
        Request request = mockRequest();

        train_isXHR(request, false);

        formSupport.setEncodingType(Upload.MULTIPART_ENCTYPE);

        validator.render(writer);

        resources.renderInformalParameters(writer);

        replay();

        Upload component = new Upload(null, null, null, null, resources, null);

        component.injectDecorator(new BaseValidationDecorator()).injectFormSupport(formSupport).injectFieldValidator(
                validator).injectRequest(request);

        component.beginRender(writer);


        Element element = writer.getElement();
        assertNotNull(element);
        assertEquals(element.getName(), "input");
        assertEquals(element.getAttribute("type"), "file");
        // assertEquals(element.getAttribute("name"),null);
        // assertEquals(element.getAttribute("id"),null);

        verify();
    }

    @Test
    public void validation_decorator_invoked_inside_begin_render() throws Exception
    {
        getMocksControl().checkOrder(true);

        ComponentResources resources = mockComponentResources();
        Upload component = new Upload(null, null, null, null, resources, null);
        MarkupWriter writer = createMarkupWriter();
        writer.element("form");
        FieldValidator validator = mockFieldValidator();
        Request request = mockRequest();

        FormSupport formSupport = mockFormSupport();
        formSupport.setEncodingType(Upload.MULTIPART_ENCTYPE);

        component.injectFormSupport(formSupport).injectRequest(request);

        ValidationDecorator decorator = mockValidationDecorator();

        component.injectDecorator(decorator).injectFieldValidator(validator);

        validator.render(writer);

        resources.renderInformalParameters(writer);
        decorator.insideField(component);

        train_isXHR(request, false);

        replay();

        component.beginRender(writer);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void begin_render_invokes_field_validator() throws Exception
    {
        getMocksControl().checkOrder(true);

        FieldValidator<Object> validate = mockFieldValidator();
        ComponentResources resources = mockComponentResources();
        Upload component = new Upload(null, validate, null, null, resources, null);
        MarkupWriter writer = createMarkupWriter();
        writer.element("form");
        Request request = mockRequest();

        FormSupport formSupport = mockFormSupport();
        formSupport.setEncodingType(Upload.MULTIPART_ENCTYPE);


        ValidationDecorator decorator = mockValidationDecorator();

        component.injectDecorator(decorator).injectRequest(request).injectFormSupport(formSupport);

        validate.render(writer);

        resources.renderInformalParameters(writer);

        decorator.insideField(component);

        train_isXHR(request, false);

        replay();

        component.beginRender(writer);

        verify();
    }

    @Test
    public void after_render_closes_element() throws Exception
    {
        Upload component = new Upload();
        MarkupWriter writer = mockMarkupWriter();

        expect(writer.end()).andReturn(null);

        replay();

        component.afterRender(writer);

        verify();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void process_submission_extracts_value_from_decoder() throws Exception
    {
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();
        ComponentResources resources = mockComponentResources();
        FieldValidationSupport support = mockFieldValidationSupport();
        FieldValidator validate = mockFieldValidator();

        Upload component = new Upload(null, validate, decoder, null, resources, support);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("foo").anyTimes();

        support.validate(uploadedFile, resources, validate);

        replay();

        component.processSubmission("test");

        verify();

        assertSame(component.getValue(), uploadedFile);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void process_submission_ignores_null_value() throws Exception
    {
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();
        ComponentResources resources = mockComponentResources();
        FieldValidationSupport support = mockFieldValidationSupport();
        FieldValidator validate = mockFieldValidator();

        Upload component = new Upload(null, validate, decoder, null, resources, support);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("").atLeastOnce();

        support.validate(null, resources, validate);


        replay();

        component.processSubmission("test");

        verify();

        assertNull(component.getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void process_submission_calls_validator() throws Exception
    {
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();
        FieldValidator<Object> validate = mockFieldValidator();
        ComponentResources resources = mockComponentResources();
        FieldValidationSupport support = mockFieldValidationSupport();

        Upload component = new Upload(null, validate, decoder, null, resources, support);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("test").atLeastOnce();

        support.validate(uploadedFile, resources, validate);

        replay();

        component.processSubmission("test");

        verify();
    }

    @SuppressWarnings({"unchecked", "ThrowableInstanceNeverThrown"})
    @Test
    public void process_submission_tracks_validator_errors() throws Exception
    {
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();
        FieldValidator<Object> validate = mockFieldValidator();
        ValidationTracker tracker = mockValidationTracker();
        ComponentResources resources = mockComponentResources();
        FieldValidationSupport support = mockFieldValidationSupport();

        Upload component = new Upload(null, validate, decoder, tracker, resources, support);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("test").atLeastOnce();

        support.validate(uploadedFile, resources, validate);
        expectLastCall().andThrow(new ValidationException("an error"));

        tracker.recordError(component, "an error");
        replay();

        component.processSubmission("test");

        verify();
    }

    protected final UploadedFile mockUploadedFile()
    {
        return newMock(UploadedFile.class);
    }

    protected final MultipartDecoder mockMultipartDecoder()
    {
        return newMock(MultipartDecoder.class);
    }
}
