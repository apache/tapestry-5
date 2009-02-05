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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.PageRenderRequestParameters;

/**
 * Used by {@link org.apache.tapestry5.internal.services.LinkSource} service to create {@link org.apache.tapestry5.Link}
 * instances.
 *
 * @since 5.1.0.0
 */
public interface LinkFactory
{

    /**
     * Creates a component event link (possibly for a Form).
     *
     * @param parameters defines the data need to create the link
     * @param forForm    if true, the link is for a form (which handles query parameters differently than normal)
     * @return the component event link
     * @see org.apache.tapestry5.services.ComponentEventRequestHandler
     */
    Link createComponentEventLink(ComponentEventRequestParameters parameters, boolean forForm);

    /**
     * Creates a page render event link.
     *
     * @param parameters defines the page and page activation context
     * @return the link
     * @see org.apache.tapestry5.services.PageRenderRequestHandler
     */
    Link createPageRenderLink(PageRenderRequestParameters parameters);
}
