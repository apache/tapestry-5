// Copyright 2006 The Apache Software Foundation
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter interface for {@link org.apache.tapestry5.services.HttpServletRequestHandler}.
 */
public interface HttpServletRequestFilter
{
    /**
     * Filter interface for the HttpServletRequestHandler pipeline. A filter should delegate to the handler. It may
     * perform operations before or after invoking the handler, and may modify the request and response passed in to the
     * handler.
     *
     * @return true if the request has been handled, false otherwise
     */
    boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler)
            throws IOException;
}
