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

package org.apache.tapestry.upload.internal.services;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tapestry.ioc.annotation.Inject;
import org.apache.tapestry.ioc.annotation.Symbol;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;
import org.apache.tapestry.upload.services.MultipartDecoder;
import org.apache.tapestry.upload.services.UploadSymbols;
import org.apache.tapestry.upload.services.UploadedFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of multipart decoder for servlets.  This implementation is perthread scope.
 */
public class MultipartDecoderImpl implements MultipartDecoder, ThreadCleanupListener
{
    private final Map<String, UploadedFileItem> uploads = CollectionFactory.newMap();

    private final String repositoryLocation;

    private final int repositoryThreshold;

    private final long maxRequestSize;

    private final long maxFileSize;

    public MultipartDecoderImpl(

            @Inject @Symbol(UploadSymbols.REPOSITORY_LOCATION)
            String repositoryLocation,

            @Symbol(UploadSymbols.REPOSITORY_THRESHOLD)
            int repositoryThreshold,

            @Symbol(UploadSymbols.REQUESTSIZE_MAX)
            long maxRequestSize,

            @Symbol(UploadSymbols.FILESIZE_MAX)
            long maxFileSize)
    {
        this.repositoryLocation = repositoryLocation;
        this.repositoryThreshold = repositoryThreshold;
        this.maxRequestSize = maxRequestSize;
        this.maxFileSize = maxFileSize;
    }

    public UploadedFile getFileUpload(String parameterName)
    {
        return uploads.get(parameterName);
    }

    public HttpServletRequest decode(HttpServletRequest request)
    {
        List<FileItem> fileItems = parseRequest(request);

        return processFileItems(request, fileItems);
    }

    public void threadDidCleanup()
    {
        for (UploadedFileItem uploaded : uploads.values())
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
        FileItemFactory factory = new DiskFileItemFactory(repositoryThreshold, new File(repositoryLocation));
        ServletFileUpload upload = new ServletFileUpload(factory);

        // set maximum file upload size
        upload.setSizeMax(maxRequestSize);
        upload.setFileSizeMax(maxFileSize);

        return upload;
    }

    protected HttpServletRequest processFileItems(HttpServletRequest request, List<FileItem> fileItems)
    {
        if (fileItems == null || fileItems.isEmpty())
        {
            return request;
        }

        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);

        String encoding = request.getCharacterEncoding();

        for (FileItem item : fileItems)
        {
            if (item.isFormField())
            {
                String fieldValue;

                try
                {

                    fieldValue = encoding == null ? item.getString() : item.getString(encoding);
                }
                catch (UnsupportedEncodingException e)
                {
                    // TODO maybe log exception with level warn
                    fieldValue = item.getString();
                }

                wrapper.addParameter(item.getFieldName(), fieldValue);
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
        uploads.put(name, file);
    }
}
