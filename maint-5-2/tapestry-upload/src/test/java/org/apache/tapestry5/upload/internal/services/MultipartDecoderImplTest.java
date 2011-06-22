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

package org.apache.tapestry5.upload.internal.services;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MultipartDecoderImplTest extends TapestryTestCase
{
    private final FileItemFactory fileItemFactory = new DiskFileItemFactory(888, new File("/tmp"));

    private static final String CHARSET = "UTF-8";

    @Test
    public void create_file_upload_gets_configuration_from_symbols() throws Exception
    {
        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, 7777, 6666, CHARSET);

        replay();

        ServletFileUpload servletFileUpload = decoder.createFileUpload();
        assertNotNull(servletFileUpload);

        verify();

        assertEquals(servletFileUpload.getFileSizeMax(), 6666);
        assertEquals(servletFileUpload.getSizeMax(), 7777);
    }

    @Test
    public void process_file_items_does_nothing_when_null_file_items() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, -1, -1, CHARSET);
        List<FileItem> items = Collections.emptyList();

        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, items);

        verify();

        assertSame(request, decodedRequest);
    }

    @Test
    public void process_file_items_does_nothing_when_empty_file_items() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, -1, -1, CHARSET);
        List<FileItem> fileItems = Collections.emptyList();

        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, fileItems);

        verify();

        assertSame(request, decodedRequest);
    }

    @Test
    public void process_file_items_creates_wrapped_request_and_sets_non_file_parameters() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();

        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, -1, -1, CHARSET);
        List<FileItem> fileItems = Arrays.asList(createValueItem("one", "first"), createValueItem("two", "second"));

        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, fileItems);

        assertNotSame(decodedRequest, request);

        assertEquals(decodedRequest.getParameter("one"), "first");
        assertEquals(decodedRequest.getParameter("two"), "second");

        verify();
    }

    @Test
    public void non_file_items_with_null_request_encoding() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();

        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, -1, -1, CHARSET);

        List<FileItem> fileItems = Arrays.asList(createValueItem("one", "first"), createValueItem("two", "second"));

        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, fileItems);

        assertNotSame(decodedRequest, request);

        assertEquals(decodedRequest.getParameter("one"), "first");
        assertEquals(decodedRequest.getParameter("two"), "second");

        verify();
    }

    @Test
    public void process_file_items_set_file_parameters_with_file_name() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, -1, -1, CHARSET);
        List<FileItem> fileItems = Arrays.asList(createFileItem("one", "first.txt"),
                                                 createFileItem("two", "second.txt"));

        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, fileItems);

        assertNotSame(decodedRequest, request);

        assertEquals(decodedRequest.getParameter("one"), "first.txt");
        assertEquals(decodedRequest.getParameter("two"), "second.txt");

        verify();
    }

    @Test
    public void uploaded_file_stored() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, -1, -1, CHARSET);
        List<FileItem> fileItems = Arrays.asList(createFileItem("one", "first.txt"),
                                                 createFileItem("two", "second.txt"));

        replay();

        decoder.processFileItems(request, fileItems);

        verify();

        assertNotNull(decoder.getFileUpload("one"));
        assertEquals(decoder.getFileUpload("one").getFileName(), "first.txt");
        assertNotNull(decoder.getFileUpload("two"));
        assertEquals(decoder.getFileUpload("two").getFileName(), "second.txt");
    }

    @Test
    public void file_items_cleaned_up() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl(fileItemFactory, -1, -1, CHARSET);
        StubFileItem firstItem = new StubFileItem("one");
        firstItem.setFormField(false);
        StubFileItem secondItem = new StubFileItem("two");
        secondItem.setFormField(false);

        List<FileItem> fileItems = new ArrayList<FileItem>();
        fileItems.add(firstItem);
        fileItems.add(secondItem);


        replay();

        decoder.processFileItems(request, fileItems);

        assertFalse(firstItem.isDeleted());
        assertFalse(secondItem.isDeleted());
        decoder.threadDidCleanup();
        assertTrue(firstItem.isDeleted());
        assertTrue(secondItem.isDeleted());

        verify();
    }

    private FileItem createValueItem(String name, String value)
    {
        StubFileItem item = new StubFileItem();
        item.setFieldName(name);
        item.setValue(value);
        item.setFormField(true);

        return item;
    }

    private FileItem createFileItem(String name, String fileName)
    {
        StubFileItem item = new StubFileItem();
        item.setFieldName(name);
        item.setFileName(fileName);
        item.setFormField(false);

        return item;
    }
}
