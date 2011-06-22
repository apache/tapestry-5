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

import java.io.*;

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

    public InputStream getInputStream() throws IOException
    {
        return null;
    }

    public String getContentType()
    {
        return null;
    }

    public String getName()
    {
        return fileName;
    }

    public boolean isInMemory()
    {
        return true;
    }

    public long getSize()
    {
        return 10;
    }

    public byte[] get()
    {
        return new byte[0]; // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public String getString(String string) throws UnsupportedEncodingException
    {
        return getString();
    }

    public String getString()
    {
        return value;
    }

    public void write(File file) throws Exception
    {
    }

    public void delete()
    {
        isDeleted = true;
    }

    public String getFieldName()
    {
        return fieldName; // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public boolean isFormField()
    {
        return formField;
    }

    public void setFormField(boolean formField)
    {
        this.formField = formField;
    }

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
}
