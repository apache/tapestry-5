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
import org.apache.commons.io.input.NullInputStream;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;

public class UploadedFileItemTest extends TapestryTestCase
{
    @Test
    public void contentTypeIsFileItemContentType() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        UploadedFileItem uploadedFile = new UploadedFileItem(item);

        expect(item.getContentType()).andReturn("foo");

        replay();

        assertEquals(uploadedFile.getContentType(), "foo");

        verify();
    }

    @Test
    public void fileNameExtractedFromFileItemName() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        UploadedFileItem uploadedFile = new UploadedFileItem(item);

        expect(item.getName()).andReturn("foo/blah.txt");

        replay();

        assertEquals(uploadedFile.getFileName(), "blah.txt");

        verify();
    }

    @Test
    public void filePathIsFileItemName() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        UploadedFileItem uploadedFile = new UploadedFileItem(item);

        expect(item.getName()).andReturn("foo/blah.txt");

        replay();

        assertEquals(uploadedFile.getFilePath(), "foo/blah.txt");

        verify();
    }

    @Test
    public void sizeIsFileItemSize() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        UploadedFileItem uploadedFile = new UploadedFileItem(item);

        expect(item.getSize()).andReturn(66l);

        replay();

        assertEquals(uploadedFile.getSize(), 66);

        verify();
    }

    @Test
    public void inMemoryIsFileItemInMemory() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        UploadedFileItem uploadedFile = new UploadedFileItem(item);

        expect(item.isInMemory()).andReturn(true);

        replay();

        assertTrue(uploadedFile.isInMemory());

        verify();
    }

    @Test
    public void streamIsFileItemStream() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        InputStream stream = new NullInputStream(3);
        UploadedFileItem uploadedFile = new UploadedFileItem(item);

        expect(item.getInputStream()).andReturn(stream);

        replay();

        assertSame(uploadedFile.getStream(), stream);

        verify();
    }

    @Test
    public void writeUsesFileItemWrite() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        File out = new File("");
        UploadedFileItem uploadedFile = new UploadedFileItem(item);

        item.write(out);

        replay();

        uploadedFile.write(out);

        verify();

    }

    @Test
    public void cleanupCallsFileItemDelete() throws Exception
    {
        FileItem item = newMock(FileItem.class);
        UploadedFileItem uploadedFile = new UploadedFileItem(item);
        item.delete();

        replay();
        uploadedFile.cleanup();
        verify();
    }

}
