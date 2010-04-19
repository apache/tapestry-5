// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class ResourceStreamerImpl implements ResourceStreamer
{
    static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    private final ResourceCache resourceCache;

    private final Request request;

    private final Response response;

    private final Context context;

    private final ResponseCompressionAnalyzer analyzer;

    private final Map<String, String> configuration;

    private final int compressionCutoff;

    private final boolean productionMode;

    public ResourceStreamerImpl(Request request,

    Response response,

    Context context,

    ResourceCache resourceCache,

    Map<String, String> configuration,

    ResponseCompressionAnalyzer analyzer,

    @Symbol(SymbolConstants.MIN_GZIP_SIZE)
    int compressionCutoff,

    @Symbol(SymbolConstants.PRODUCTION_MODE)
    boolean productionMode)
    {
        this.request = request;
        this.response = response;
        this.context = context;
        this.resourceCache = resourceCache;
        this.configuration = configuration;
        this.analyzer = analyzer;
        this.compressionCutoff = compressionCutoff;
        this.productionMode = productionMode;
    }

    public void streamResource(Resource resource) throws IOException
    {
        if (!resource.exists())
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ServicesMessages.assetDoesNotExist(resource));
            return;
        }

        long ifModifiedSince = 0;

        try
        {
            ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE_HEADER);
        }
        catch (IllegalArgumentException ex)
        {
            // Simulate the header being missing if it is poorly formatted.

            ifModifiedSince = -1;
        }

        if (ifModifiedSince > 0)
        {
            long modified = resourceCache.getTimeModified(resource);

            if (ifModifiedSince >= modified)
            {
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED, "");
                return;
            }
        }

        // Prevent the upstream code from compressing when we don't want to.

        request.setAttribute(InternalConstants.SUPPRESS_COMPRESSION, true);

        StreamableResource streamble = resourceCache.getStreamableResource(resource);

        long lastModified = streamble.getLastModified();

        response.setDateHeader("Last-Modified", lastModified);

        if (productionMode)
            response.setDateHeader("Expires", lastModified + InternalConstants.TEN_YEARS);

        String contentType = identifyContentType(resource, streamble);

        boolean compress = analyzer.isGZipSupported() && streamble.getSize(false) >= compressionCutoff
                && analyzer.isCompressable(contentType);

        int contentLength = streamble.getSize(compress);

        if (contentLength >= 0)
            response.setContentLength(contentLength);

        if (compress)
            response.setHeader(InternalConstants.CONTENT_ENCODING_HEADER, InternalConstants.GZIP_CONTENT_ENCODING);

        InputStream is = null;

        try
        {
            is = streamble.getStream(compress);

            OutputStream os = response.getOutputStream(contentType);

            TapestryInternalUtils.copy(is, os);

            is.close();
            is = null;

            os.close();
        }
        finally
        {
            InternalUtils.close(is);
        }
    }

    public String getContentType(Resource resource) throws IOException
    {
        return identifyContentType(resource, resourceCache.getStreamableResource(resource));
    }

    private String identifyContentType(Resource resource, StreamableResource streamble) throws IOException
    {
        String contentType = streamble.getContentType();

        if ("content/unknown".equals(contentType))
            contentType = null;

        if (contentType != null)
            return contentType;

        contentType = context.getMimeType(resource.getPath());

        if (contentType != null)
            return contentType;

        String file = resource.getFile();
        int dotx = file.lastIndexOf('.');

        if (dotx > 0)
        {
            String extension = file.substring(dotx + 1);

            contentType = configuration.get(extension);
        }

        return contentType != null ? contentType : "application/octet-stream";
    }
}
