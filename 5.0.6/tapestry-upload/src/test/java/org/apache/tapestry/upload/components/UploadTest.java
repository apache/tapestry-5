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

import static org.easymock.EasyMock.expectLastCall;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.test.TapestryTestCase;
import org.apache.tapestry.upload.services.MultipartDecoder;
import org.apache.tapestry.upload.services.UploadedFile;
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

        formSupport.setEncodingType(Upload.MULTIPART_ENCTYPE);

        resources.renderInformalParameters(writer);

        replay();

        Upload component = new Upload(null, null, null, null, resources);

        component.injectDecorator(new StubValidationDecorator());
        component.injectFormSupport(formSupport);

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
        Upload component = new Upload(null, null, null, null, resources);
        MarkupWriter writer = createMarkupWriter();
        writer.element("form");

        FormSupport formSupport = mockFormSupport();
        formSupport.setEncodingType(Upload.MULTIPART_ENCTYPE);
        
        component.injectFormSupport(formSupport);

        ValidationDecorator decorator = mockValidationDecorator();

        component.injectDecorator(decorator);

        resources.renderInformalParameters(writer);
        decorator.insideField(component);

        replay();

        component.beginRender(writer);

        verify();
    }

    private ValidationDecorator mockValidationDecorator()
    {
        return newMock(ValidationDecorator.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void begin_render_invokes_field_validator() throws Exception
    {
        getMocksControl().checkOrder(true);

        FieldValidator<Object> validate = mockFieldValidator();
        ComponentResources resources = mockComponentResources();
        Upload component = new Upload(null, validate, null, null, resources);
        MarkupWriter writer = createMarkupWriter();
        writer.element("form");

        FormSupport formSupport = mockFormSupport();
        formSupport.setEncodingType(Upload.MULTIPART_ENCTYPE);
        component.injectFormSupport(formSupport);

        ValidationDecorator decorator = mockValidationDecorator();

        component.injectDecorator(decorator);

        validate.render(writer);
        resources.renderInformalParameters(writer);
        decorator.insideField(component);

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

    @Test
    public void process_submission_extracts_value_from_decoder() throws Exception
    {
        FormSupport formSupport = mockFormSupport();
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();

        Upload component = new Upload(null, null, decoder, null, null);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("foo").anyTimes();

        replay();

        component.processSubmission(formSupport, "test");

        verify();

        assertSame(component.getValue(), uploadedFile);
    }

    @Test
    public void process_submission_ignores_null_value() throws Exception
    {
        FormSupport formSupport = mockFormSupport();
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();

        Upload component = new Upload(null, null, decoder, null, null);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("").atLeastOnce();

        replay();

        component.processSubmission(formSupport, "test");

        verify();

        assertNull(component.getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void process_submission_calls_validator() throws Exception
    {
        FormSupport formSupport = mockFormSupport();
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();
        FieldValidator<Object> validate = mockFieldValidator();

        Upload component = new Upload(null, validate, decoder, null, null);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("test").atLeastOnce();
        validate.validate(uploadedFile);
        replay();

        component.processSubmission(formSupport, "test");

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void process_submission_tracks_validator_errors() throws Exception
    {
        FormSupport formSupport = mockFormSupport();
        MultipartDecoder decoder = mockMultipartDecoder();
        UploadedFile uploadedFile = mockUploadedFile();
        FieldValidator<Object> validate = mockFieldValidator();
        ValidationTracker tracker = mockValidationTracker();

        Upload component = new Upload(null, validate, decoder, tracker, null);

        expect(decoder.getFileUpload("test")).andReturn(uploadedFile);
        expect(uploadedFile.getFileName()).andReturn("test").atLeastOnce();
        validate.validate(uploadedFile);
        expectLastCall().andThrow(new ValidationException("an error"));
        tracker.recordError(component, "an error");
        replay();

        component.processSubmission(formSupport, "test");

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
