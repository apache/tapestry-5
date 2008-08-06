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

package org.apache.tapestry5;

import org.apache.tapestry5.services.Response;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An alternate response from a component event handler method used to directly provide a stream of data to be sent to
 * the client, rather than indicating what page to send a render redirect request to.
 */
public interface StreamResponse
{
    /**
     * Returns the content type to be reported to the client.
     */
    String getContentType();

    /**
     * Returns the stream of bytes to be sent to the client. The stream will be closed when the end of the stream is
     * reached. The provided stream will be wrapped in a {@link BufferedInputStream} for efficiency.
     */
    InputStream getStream() throws IOException;


    /**
     * Prepares the response before it is sent to the client. This is the place to set any response headers (e.g.
     * content-disposition).
     *
     * @param response Response that will be sent.
     */
    void prepareResponse(Response response);

}
