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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;

/**
 * Implementation of multipart decoder for servlets.
 */
class MultipartDecoderImpl implements MultipartDecoder, ThreadCleanupListener
{
    private final Map<String, UploadedFileItem> _uploads = newMap();

    private final String _repositoryLocation;

    private final int _repositoryThreshold;

    private final long _maxRequestSize ;

    private final long _maxFileSize;

    public MultipartDecoderImpl(String repositoryLocation, int repositoryThreshold, long maxRequestSize, long maxFileSize)
    {
        _repositoryLocation = repositoryLocation;
        _repositoryThreshold = repositoryThreshold;
        _maxRequestSize = maxRequestSize;
        _maxFileSize = maxFileSize;
    }

    public UploadedFile getFileUpload(String parameterName)
    {
        return _uploads.get(parameterName);
    }

    public HttpServletRequest decode(HttpServletRequest request)
    {
        // String encoding = request.getCharacterEncoding();
        List<FileItem> fileItems = parseRequest(request);
        return processFileItems(request, fileItems);
    }

    public void threadDidCleanup()
    {
        for (UploadedFileItem uploaded : _uploads.values())
        {
            uploaded.cleanup();
        }
    }

    @SuppressWarnings("unchecked")
    protected List<FileItem> parseRequest(HttpServletRequest request)
    {
        try
        {
            return createFileUpload().parseRequest(request);
        }
        catch (FileUploadException e)
        {
            throw new RuntimeException(UploadMessages.unableToDecode(), e);
        }
    }

    protected ServletFileUpload createFileUpload()
    {
        FileItemFactory factory = new DiskFileItemFactory(_repositoryThreshold, new File(
                _repositoryLocation));
        ServletFileUpload upload = new ServletFileUpload(factory);

        // set maximum file upload size
        upload.setSizeMax(_maxRequestSize);
        upload.setFileSizeMax(_maxFileSize);

        return upload;
    }

    protected HttpServletRequest processFileItems(HttpServletRequest request,
            List<FileItem> fileItems)
    {
        if (fileItems == null || fileItems.isEmpty()) { return request; }

        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);

        for (FileItem item : fileItems)
        {
            if (item.isFormField())
            {
                wrapper.addParameter(item.getFieldName(), item.getString());
            }
            else
            {
                wrapper.addParameter(item.getFieldName(), item.getName());
                addUploadedFile(item.getFieldName(), new UploadedFileItem(item));
            }
        }

        return wrapper;
    }

    protected void addUploadedFile(String name, UploadedFileItem file)
    {
        _uploads.put(name, file);
    }
}
