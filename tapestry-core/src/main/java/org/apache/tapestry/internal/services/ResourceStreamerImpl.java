// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.Response;

public class ResourceStreamerImpl implements ResourceStreamer
{
    private final Response _response;

    private final Map<String, String> _configuration;

    private final int _bufferSize = 1000;

    // One year, in milliseconds

    final static long EXPIRE_DELTA = 31536000000l;

    public ResourceStreamerImpl(final Response response, Map<String, String> configuration)
    {
        _response = response;
        _configuration = configuration;
    }

    public void streamResource(Resource resource) throws IOException
    {
        URL url = resource.toURL();

        URLConnection connection = url.openConnection();

        int contentLength = connection.getContentLength();

        if (contentLength >= 0) _response.setContentLength(contentLength);

        // Could get this from the ResourceCache, but can't imagine
        // it's very expensive.

        long lastModified = connection.getLastModified();

        _response.setDateHeader("Last-Modified", lastModified);
        _response.setDateHeader("Expires", lastModified + EXPIRE_DELTA);

        String contentType = connection.getContentType();

        if ("content/unknown".equals(contentType)) contentType = null;

        if (contentType == null)
        {
            String file = resource.getFile();
            int dotx = file.lastIndexOf('.');

            if (dotx > 0)
            {
                String extension = file.substring(dotx + 1);

                contentType = _configuration.get(extension);
            }

            if (contentType == null) contentType = "application/octet-stream";
        }

        InputStream is = null;

        try
        {
            connection.connect();

            is = new BufferedInputStream(connection.getInputStream());

            OutputStream os = _response.getOutputStream(contentType);

            byte[] buffer = new byte[_bufferSize];

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
            TapestryInternalUtils.close(is);
        }

    }
}
