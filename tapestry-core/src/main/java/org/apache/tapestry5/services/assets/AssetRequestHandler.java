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

package org.apache.tapestry5.services.assets;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.services.AssetDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handler for asset requests, which expose some kind of {@link Asset} to
 * the user agent (i.e., the client web browser). When contributed to the {@link AssetDispatcher} service,
 * the contributed key is a handler id (such as "meta/core").
 *
 * An example request path might be <code>/assets/meta/core/dd8d73ac51dbab28caaec4865d302bf2/deselect.png</code>.
 * The handler id would be
 * <code>meta/core</code>, the {@linkplain AssetChecksumGenerator checksum of the resource content} is the
 * hex string, and the extra path would be <code>select.png</code>.
 *
 * @see AssetDispatcher
 * @see org.apache.tapestry5.services.AssetRequestDispatcher
 * @see AssetPathConstructor
 * @since 5.2.0
 */
public interface AssetRequestHandler
{
    /**
     * Given a request targeted (via the handler id) to the specific handler, process the request.
     * The handler is responsible for processing the request, sending back either a bytestream
     * (via {@link Response#getOutputStream(String)}) or an error response
     * (via {@link Response#sendError(int, String)}). It is the handler's responsibility to allow
     * for client-side caching (possibly sending an {@link HttpServletResponse#SC_NOT_MODIFIED} response).
     *
     * The handler should return true if it provided a response. If the handler returns false, this indicates that the
     * extra path did not identify a known asset (virtual or otherwise) and the AssetDispatcher service should send a
     * {@link HttpServletResponse#SC_NOT_FOUND} response.
     *
     * Starting in Tapestry 5.4, the handler is informed by the {@link org.apache.tapestry5.services.AssetRequestDispatcher}
     * whether or not the content should be compressed (this is determined based on information in the URL).
     *
     * @param request
     *         incoming asset request
     * @param response
     *         used to send a response to client
     * @param extraPath
     *         additional path to identify the specific asset
     * @return true if request was handled (and response sent), false if asset not found
     */
    boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException;
}
