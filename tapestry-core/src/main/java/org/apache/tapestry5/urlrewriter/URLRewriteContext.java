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

package org.apache.tapestry5.urlrewriter;

import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.ComponentEventRequestParameters;

/**
 * Context passed to the process method of URLRewriterRule implementations, providing additional
 * information that the rules might need to function.
 */
public interface URLRewriteContext {

    /**
     *
     * @return true if the "process" method of URLRewriterRule is being called for an incoming request.
     */
    boolean isIncoming();

    /**
     * If the request being processed is processed in response to pagelink creation, the PageRenderRequestParameters
     * associated with that creation will be available via this method.  Otherwise, this method returns null.
     * @return the PageRenderRequestParameters associated with the link creation for this request, or null
     */
    PageRenderRequestParameters getPageParameters();

    /**
     * If the request being processed is processed in response to component event link creation, the
     * ComponentEVentRequestParameters associated with that creation will be available via this method.
     * Otherwise, this method returns null.
     * @return the ComponentEventRequestParameters associated with the link creation for this request, or null
     */
    ComponentEventRequestParameters getComponentEventParameters();

}
