// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import java.io.ObjectOutputStream;


/**
 * Allows binary object data to be encoded into a string.
 *
 * @see ClientDataEncoder#createSink()
 * @since 5.1.0.1
 */
public interface ClientDataSink
{
    /**
     * Provides the output stream to which data can be written.
     *
     * @return the stream
     */
    ObjectOutputStream getObjectOutputStream();

    /**
     * Encoded the data written to the stream as a string that can be provided to the client. Implicitly closes the
     * stream (if it has not already been closed).
     *
     * @return the encoded data as a string
     * @see org.apache.tapestry5.services.ClientDataEncoder#decodeClientData(String)
     */
    String getClientData();

    /**
     * Returns the client data encoded (for inclusion in a URL) via {@link org.apache.tapestry5.services.URLEncoder}.
     *
     * @since 5.1.0.4
     */
    String getEncodedClientData();
}
