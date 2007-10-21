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

package org.apache.tapestry.upload.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class MultipartDecoderImplTest extends TapestryTestCase
{

    @Test
    public void createFileUploadGetsConfigurationFromSymbols() throws Exception
    {
        MultipartDecoderImpl decoder = new MultipartDecoderImpl("/tmp", 888, 7777, 6666);

        replay();

        ServletFileUpload servletFileUpload = decoder.createFileUpload();
        assertNotNull(servletFileUpload);
        verify();

        assertEquals(servletFileUpload.getFileSizeMax(), 6666);
        assertEquals(servletFileUpload.getSizeMax(), 7777);
    }

    @Test
    public void processFileItemsDoesNothingWhenNullFileItems() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl("/tmp", 888, -1, -1);

        replay();
        HttpServletRequest decodedRequest = decoder.processFileItems(request, null);
        verify();

        assertSame(request, decodedRequest);
    }

    @Test
    public void processFileItemsDoesNothingWhenEmptyFileItems() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl("/tmp", 888, -1, -1);
        List<FileItem> fileItems = Collections.emptyList();
        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, fileItems);
        verify();

        assertSame(request, decodedRequest);
    }

    @Test
    public void processFileItemsCreatesWrappedRequestAndSetsNonFileParameters() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl("/tmp", 888, -1, -1);
        List<FileItem> fileItems = Arrays.asList(createValueItem("one", "first"), createValueItem(
                "two",
                "second"));
        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, fileItems);

        assertNotSame(decodedRequest, request);

        assertEquals(decodedRequest.getParameter("one"), "first");
        assertEquals(decodedRequest.getParameter("two"), "second");

        verify();
    }

    @Test
    public void processFileItemsSetsFileParametersWithFileName() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl("/tmp", 888, -1, -1);
        List<FileItem> fileItems = Arrays.asList(
                createFileItem("one", "first.txt"),
                createFileItem("two", "second.txt"));
        replay();

        HttpServletRequest decodedRequest = decoder.processFileItems(request, fileItems);

        assertNotSame(decodedRequest, request);

        assertEquals(decodedRequest.getParameter("one"), "first.txt");
        assertEquals(decodedRequest.getParameter("two"), "second.txt");

        verify();
    }

    @Test
    public void processFileItemsStoresUploadedFile() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl("/tmp", 888, -1, -1);
        List<FileItem> fileItems = Arrays.asList(
                createFileItem("one", "first.txt"),
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
    public void threadDidCleanupDeletesAllFileItems() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        MultipartDecoderImpl decoder = new MultipartDecoderImpl("/tmp", 888, -1, -1);
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
