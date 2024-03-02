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

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemHeaders;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;

public class StubFileItem implements FileItem
{
    private static final long serialVersionUID = -7041417646464173208L;

    private String fileName;

    private String value;

    private String fieldName;

    private boolean formField;

    private boolean isDeleted;

    public StubFileItem()
    {
    }

    public StubFileItem(String fieldName)
    {
        this.fieldName = fieldName;
    }

    @Override
    public InputStream getInputStream()
    {
        return null;
    }

    @Override
    public String getContentType()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return fileName;
    }

    @Override
    public boolean isInMemory()
    {
        return true;
    }

    @Override
    public long getSize()
    {
        return 10;
    }

    @Override
    public byte[] get()
    {
        return new byte[0]; // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public String getString(Charset charset)
    {
        return getString();
    }

    @Override
    public String getString()
    {
        return value;
    }

    @Override
    public FileItem write(Path file)
    {
        return this;
    }

    @Override
    public FileItem delete()
    {
        isDeleted = true;
        return this;
    }

    @Override
    public String getFieldName()
    {
        return fieldName; // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    @Override
    public FileItem setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
        return this;
    }

    @Override
    public boolean isFormField()
    {
        return formField;
    }

    @Override
    public FileItem setFormField(boolean formField)
    {
        this.formField = formField;
        return this;
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return null;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public boolean isDeleted()
    {
        return isDeleted;
    }

    /* unused method but required by FileItem interface */
    @Override
    public FileItemHeaders getHeaders()
    {
        return null;
    }

    /* unused method but required by FileItem interface */
    @Override
    public FileItem setHeaders(FileItemHeaders headers)
    {
        return this;
    }
}
