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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Response;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamResponseResultProcessor implements ComponentEventResultProcessor<StreamResponse>
{
    private static final int BUFFER_SIZE = 5000;

    private final Response response;

    public StreamResponseResultProcessor(Response response)
    {
        this.response = response;
    }

    public void processResultValue(StreamResponse streamResponse)
            throws IOException
    {
        OutputStream os = null;
        InputStream is = null;

        streamResponse.prepareResponse(response);

        try
        {
            is = new BufferedInputStream(streamResponse.getStream());

            os = response.getOutputStream(streamResponse.getContentType());

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true)
            {
                int length = is.read(buffer, 0, buffer.length);
                if (length < 0) break;

                os.write(buffer, 0, length);
            }

            // TAPESTRY-2415: WebLogic needs this flush() call.            
            os.flush();

            os.close();
            os = null;

            is.close();
            is = null;
        }
        finally
        {
            InternalUtils.close(is);
            InternalUtils.close(os);
        }

    }

}
