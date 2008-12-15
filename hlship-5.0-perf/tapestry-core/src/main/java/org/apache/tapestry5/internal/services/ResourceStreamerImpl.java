// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.services.Response;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class ResourceStreamerImpl implements ResourceStreamer
{
    private static final long TEN_YEARS = new TimeInterval("10y").milliseconds();

    private static final int BUFFER_SIZE = 5000;

    private final Response response;

    private final Map<String, String> configuration;

    public ResourceStreamerImpl(final Response response, Map<String, String> configuration)
    {
        this.response = response;
        this.configuration = configuration;
    }

    public void streamResource(Resource resource) throws IOException
    {
        URL url = resource.toURL();

        URLConnection connection = url.openConnection();

        int contentLength = connection.getContentLength();

        if (contentLength >= 0) response.setContentLength(contentLength);

        // Could get this from the ResourceCache, but can't imagine
        // it's very expensive.

        long lastModified = connection.getLastModified();

        response.setDateHeader("Last-Modified", lastModified);
        response.setDateHeader("Expires", lastModified + TEN_YEARS);

        String contentType = connection.getContentType();

        if ("content/unknown".equals(contentType)) contentType = null;

        if (contentType == null)
        {
            String file = resource.getFile();
            int dotx = file.lastIndexOf('.');

            if (dotx > 0)
            {
                String extension = file.substring(dotx + 1);

                contentType = configuration.get(extension);
            }

            if (contentType == null) contentType = "application/octet-stream";
        }

        InputStream is = null;

        try
        {
            connection.connect();

            is = new BufferedInputStream(connection.getInputStream());

            OutputStream os = response.getOutputStream(contentType);

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true)
            {
                int length = is.read(buffer);

                if (length < 0) break;

                os.write(buffer, 0, length);
            }

            is.close();
            is = null;

            os.flush();
        }
        finally
        {
            InternalUtils.close(is);
        }

    }
}
