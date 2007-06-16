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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implentation of {@link UploadedFile} for FileItems.
 */
public class UploadedFileItem implements UploadedFile
{
    private final FileItem _item;

    public UploadedFileItem(FileItem item)
    {
        _item = item;
    }

    public String getContentType()
    {
        return _item.getContentType();
    }

    public String getFileName()
    {
        return FilenameUtils.getName(getFilePath());
    }

    public String getFilePath()
    {
        return _item.getName();
    }

    public long getSize()
    {
        return _item.getSize();
    }

    public InputStream getStream()
    {
        try
        {
            return _item.getInputStream();
        }
        catch (IOException e)
        {
            throw new RuntimeException(UploadMessages.unableToOpenContentFile(this), e);
        }
    }

    public boolean isInMemory()
    {
        return _item.isInMemory();
    }

    public void write(File file)
    {
        try
        {
            _item.write(file);
        }
        catch (Exception e)
        {
            throw new RuntimeException(UploadMessages.writeFailure(file), e);
        }
    }

    public void cleanup()
    {
        _item.delete();
    }
}
