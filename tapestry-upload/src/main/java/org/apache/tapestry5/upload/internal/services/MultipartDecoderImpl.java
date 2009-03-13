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
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadSymbols;
import org.apache.tapestry5.upload.services.UploadedFile;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of multipart decoder for servlets.  This implementation is perthread scope.
 */
public class MultipartDecoderImpl implements MultipartDecoder, ThreadCleanupListener
{
    private final Map<String, UploadedFileItem> uploads = CollectionFactory.newMap();

    private final FileItemFactory fileItemFactory;

    private final long maxRequestSize;

    private final long maxFileSize;

    private final String requestEncoding;

    private FileUploadException uploadException;

    public MultipartDecoderImpl(

            FileItemFactory fileItemFactory,

            @Symbol(UploadSymbols.REQUESTSIZE_MAX)
            long maxRequestSize,

            @Symbol(UploadSymbols.FILESIZE_MAX)
            long maxFileSize,

            @Inject @Symbol(SymbolConstants.CHARSET)
            String requestEncoding)
    {
        this.fileItemFactory = fileItemFactory;
        this.maxRequestSize = maxRequestSize;
        this.maxFileSize = maxFileSize;
        this.requestEncoding = requestEncoding;
    }

    public UploadedFile getFileUpload(String parameterName)
    {
        return uploads.get(parameterName);
    }

    public HttpServletRequest decode(HttpServletRequest request)
    {
        try
        {
            request.setCharacterEncoding(requestEncoding);
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }

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
        catch (FileUploadException ex)
        {
            uploadException = ex;

            return Collections.emptyList();
        }
    }

    protected ServletFileUpload createFileUpload()
    {
        ServletFileUpload upload = new ServletFileUpload(fileItemFactory);

        // set maximum file upload size
        upload.setSizeMax(maxRequestSize);
        upload.setFileSizeMax(maxFileSize);

        return upload;
    }

    protected HttpServletRequest processFileItems(HttpServletRequest request, List<FileItem> fileItems)
    {
        if (uploadException == null && fileItems.isEmpty())
        {
            return request;
        }

        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);

        for (FileItem item : fileItems)
        {
            if (item.isFormField())
            {
                String fieldValue;

                try
                {

                    fieldValue = item.getString(requestEncoding);
                }
                catch (UnsupportedEncodingException ex)
                {
                    throw new RuntimeException(ex);
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

    public FileUploadException getUploadException()
    {
        return uploadException;
    }
}
