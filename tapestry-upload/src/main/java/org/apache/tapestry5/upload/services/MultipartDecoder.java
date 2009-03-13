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

package org.apache.tapestry5.upload.services;

import org.apache.commons.fileupload.FileUploadException;

import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for detecting and processing file upload requests, using Jakarta Commons FileUpload. Implementations of
 * this service typically use the threaded service lifecycle model.
 */
public interface MultipartDecoder
{

    /**
     * @param parameterName Name of the query parameter associated with the uploaded file
     * @return a file upload with the given name, or null if no such file upload was in the request.
     */
    UploadedFile getFileUpload(String parameterName);

    /**
     * Decodes the request, returning a new {@link javax.servlet.http.HttpServletRequest} implementation that will allow
     * access to the form fields submitted in the request (but omits uploaded files).
     *
     * @param request The incoming servlet request
     * @return decoded http request
     */
    HttpServletRequest decode(HttpServletRequest request);

    /**
     * Returns the exception the occured during the file upload, or null if the processing of the multipart upload
     * stream was succesful.
     */
    FileUploadException getUploadException();
}
