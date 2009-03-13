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
import org.apache.commons.io.FilenameUtils;
import org.apache.tapestry5.upload.services.UploadedFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implentation of {@link org.apache.tapestry5.upload.services.UploadedFile} for FileItems.
 */
public class UploadedFileItem implements UploadedFile
{
    private final FileItem item;

    public UploadedFileItem(FileItem item)
    {
        this.item = item;
    }

    public String getContentType()
    {
        return item.getContentType();
    }

    public String getFileName()
    {
        return FilenameUtils.getName(getFilePath());
    }

    public String getFilePath()
    {
        return item.getName();
    }

    public long getSize()
    {
        return item.getSize();
    }

    public InputStream getStream()
    {
        try
        {
            return item.getInputStream();
        }
        catch (IOException e)
        {
            throw new RuntimeException(UploadMessages.unableToOpenContentFile(this), e);
        }
    }

    public boolean isInMemory()
    {
        return item.isInMemory();
    }

    public void write(File file)
    {
        try
        {
            item.write(file);
        }
        catch (Exception e)
        {
            throw new RuntimeException(UploadMessages.writeFailure(file), e);
        }
    }

    public void cleanup()
    {
        item.delete();
    }
}
