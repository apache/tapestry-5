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

package org.apache.tapestry.services;

import java.io.IOException;

/**
 * Handles a invocation related to rendering out a pages complete content.
 *
 * @see PageRenderRequestFilter
 */
public interface PageRenderRequestHandler
{
    /**
     * Invoked to activate and render a page. In certain cases, based on values returned when activating the page, a
     * {@link org.apache.tapestry.services.ComponentEventResultProcessor} may be used to send an alternate response
     * (typically, a redirect).
     *
     * @param logicalPageName the logical name of the page to activate and render
     * @param context         context data, supplied by the page at render time, extracted from the render URL
     */
    void handle(String logicalPageName, String[] context) throws IOException;
}
