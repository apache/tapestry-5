// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services.assets;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.AssetDispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

/**
 * Handler for asset requests, which expose some kind of {@link Asset} to
 * the user agent (i.e., the client web browser). Starting in Tapestry 5.2,
 * asset paths are more structured, consisting of four parts:
 * <ul>
 * <li><code>/assets/</code> -- the root path for all assets
 * <li>
 * <em>application version</em> -- the application version, as defined by the
 * {@link SymbolConstants#APPLICATION_VERSION} symbol
 * <li><em>handler id</em> -- a handler for this part of the asset path (defined by contributions to the
 * <code>AssetDispatcher</code> service)
 * <li><em>extra path</em> -- additional path beyond the handler id, used to identify the specific resource
 * </ul>
 * <p>
 * So, an example path might be <code>/assets/1.0.1/corelib/components/select.png</code>. The handler id would be
 * <code>corelib</code>, and the extra path would be <code>components/select.png</code>.
 * 
 * @since 5.2.0
 * @see AssetDispatcher
 */
public interface AssetRequestHandler
{
    /**
     * Given a request targeted (via the handler id) to the specific handler, process the request.
     * The handler is responsible for processing the request, sending back either a bytestream
     * (via {@link Response#getOutputStream(String)}) or an error response
     * (via {@link Response#sendError(int, String)}). It is the handler's responsibility to allow
     * for client-side caching (possibly sending an {@link HttpServletResponse#SC_NOT_MODIFIED} response).
     * <p>
     * The handler should return true if it provided a response. If the handler returns false, this indicates that the
     * extra path did not identify a known asset (virtual or otherwise) and the AssetDispatcher service should send a
     * {@link HttpServletResponse#SC_NOT_FOUND} response.
     * 
     * @param request
     *            incoming asset request
     * @param response
     *            used to send a response to client
     * @param extraPath
     *            additional path to identify the specific asset
     * @return true if request handler, false if asset not found
     */
    boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException;
}
