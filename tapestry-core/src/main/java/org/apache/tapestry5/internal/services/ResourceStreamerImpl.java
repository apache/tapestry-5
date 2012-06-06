// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 The Apache Software Foundation
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
import org.apache.tapestry5.internal.IOOperation;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class ResourceStreamerImpl implements ResourceStreamer
{
    static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    private final Request request;

    private final Response response;

    private final StreamableResourceSource streamableResourceSource;

    private final ResponseCompressionAnalyzer analyzer;

    private final boolean productionMode;

    private final OperationTracker tracker;

    private final ResourceChangeTracker resourceChangeTracker;

    public ResourceStreamerImpl(Request request,

                                Response response,

                                StreamableResourceSource streamableResourceSource,

                                ResponseCompressionAnalyzer analyzer,

                                OperationTracker tracker,

                                @Symbol(SymbolConstants.PRODUCTION_MODE)
                                boolean productionMode, ResourceChangeTracker resourceChangeTracker)
    {
        this.request = request;
        this.response = response;
        this.streamableResourceSource = streamableResourceSource;

        this.analyzer = analyzer;
        this.tracker = tracker;
        this.productionMode = productionMode;
        this.resourceChangeTracker = resourceChangeTracker;
    }

    public void streamResource(final Resource resource) throws IOException
    {
        if (!resource.exists())
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ServicesMessages.assetDoesNotExist(resource));
            return;
        }

        TapestryInternalUtils.performIO(tracker, String.format("Streaming %s", resource), new IOOperation()
        {
            public void perform() throws IOException
            {
                StreamableResourceProcessing processing = analyzer.isGZipSupported() ? StreamableResourceProcessing.COMPRESSION_ENABLED
                        : StreamableResourceProcessing.COMPRESSION_DISABLED;

                StreamableResource streamable = streamableResourceSource.getStreamableResource(resource, processing, resourceChangeTracker);

                streamResource(streamable);
            }
        });
    }

    public void streamResource(StreamableResource streamable) throws IOException
    {
        long lastModified = streamable.getLastModified();

        long ifModifiedSince = 0;

        try
        {
            ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE_HEADER);
        } catch (IllegalArgumentException ex)
        {
            // Simulate the header being missing if it is poorly formatted.

            ifModifiedSince = -1;
        }

        if (ifModifiedSince > 0)
        {
            if (ifModifiedSince >= lastModified)
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        // Prevent the upstream code from compressing when we don't want to.

        response.disableCompression();

        response.setDateHeader("Last-Modified", lastModified);

        if (productionMode)
        {
            response.setDateHeader("Expires", lastModified + InternalConstants.TEN_YEARS);
        }

        response.setContentLength(streamable.getSize());

        if (streamable.getCompression() == CompressionStatus.COMPRESSED)
        {
            response.setHeader(InternalConstants.CONTENT_ENCODING_HEADER, InternalConstants.GZIP_CONTENT_ENCODING);
        }

        OutputStream os = response.getOutputStream(streamable.getContentType());

        streamable.streamTo(os);

        os.close();
    }
}
