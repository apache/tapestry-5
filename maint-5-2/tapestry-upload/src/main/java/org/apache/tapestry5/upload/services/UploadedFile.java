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

package org.apache.tapestry5.upload.services;

import org.apache.tapestry5.upload.components.Upload;

import java.io.File;
import java.io.InputStream;

/**
 * Represents an uploaded file.
 *
 * @see Upload
 */
public interface UploadedFile
{
    /**
     * @return the MIME type specified when the file was uploaded.
     */
    String getContentType();

    /**
     * @return the name of the file that was uploaded.
     */
    String getFileName();

    /**
     * @return the complete path, as reported by the client browser.
     */
    String getFilePath();

    /**
     * @return the size, in bytes, of the uploaded content.
     */
    long getSize();

    /**
     * @return an input stream of the content of the file.
     */
    InputStream getStream();

    /**
     * @return true if the uploaded content is in memory.
     */
    boolean isInMemory();

    /**
     * Writes the content of the file to a known location.
     *
     * @param file Location to write file to
     */
    void write(File file);
}
