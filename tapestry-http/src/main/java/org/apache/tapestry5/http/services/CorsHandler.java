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

package org.apache.tapestry5.http.services;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.CorsHandlerResult;

/**
 * Interface implemented by classes handling CORS (Cross-Origin Resource Sharing) in requests.
 * Implementations should be contributed to {@link CorsHttpServletRequestFilter} and 
 * can use the useful methods from {@link CorsHandlerHelper}.
 * @see CorsHandlerHelper
 * @see CorsHandlerResult
 * @see CorsHttpServletRequestFilter
 * @since 5.8.2
 */
public interface CorsHandler
{
    /**
     * Handles the CORS processing of a request, possibly doing nothing. This method
     * cannot return <code>null</code>.
     * @return a {@linkplain CorsHandlerResult} object. 
     */
    CorsHandlerResult handle(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
