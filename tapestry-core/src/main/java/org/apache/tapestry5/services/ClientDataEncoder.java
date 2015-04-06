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

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A service used when a component or service needs to encode some amount of data on the client as a string. The string
 * may be a query parameter, hidden form field, or a portion of a URL.  The default implementation converts the object
 * output stream into a Base64 string.
 *
 * Starting in release 5.3.6, the encoded data incorporates an HMAC (hash based message authentication code) signature,
 * as a prefix. HMAC requires a secret key, configured using the
 * {@link org.apache.tapestry5.SymbolConstants#HMAC_PASSPHRASE} symbol.
 *
 * @since 5.1.0.1
 */
public interface ClientDataEncoder
{
    /**
     * Creates a sink for client data.  The sink provides an output stream and ultimately, a string representation of
     * the data sent to the stream.
     *
     * @return a new sink
     */
    ClientDataSink createSink();

    /**
     * Decodes data previously obtained from {@link ClientDataSink#getClientData()}.
     *
     * @param clientData
     *         encoded client data
     * @return stream of decoded data
     * @throws IOException
     *         if the client data has been corrupted (verified via the HMAC)
     */
    ObjectInputStream decodeClientData(String clientData) throws IOException;

    /**
     * Decodes client data obtained via {@link ClientDataSink#getEncodedClientData()}.
     *
     * @param clientData
     *         URLEncoded client data
     * @return stream of objects
     * @throws IOException
     *         if the client data has been corrupted (verified via the HMAC)
     * @since 5.1.0.4
     */
    ObjectInputStream decodeEncodedClientData(String clientData) throws IOException;
}
