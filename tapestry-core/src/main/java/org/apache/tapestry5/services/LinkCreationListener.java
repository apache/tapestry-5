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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Link;

/**
 * Listener interface for objects that need to be notified about newly created links.
 *
 * @see org.apache.tapestry5.services.LinkCreationHub
 */
public interface LinkCreationListener
{
    /**
     * Invoked when a page link (a link that renders a page) is created. The listener may decide to encode additional
     * query parameters into the link (via {@link Link#addParameter(String, String)}).
     *
     * @param link the newly created link
     */
    void createdPageRenderLink(Link link);

    /**
     * Invoked when an action link (a link that asks a component to perform an action) is created. The listener may
     * decide to encode additional query parameters into the link (via {@link Link#addParameter(String, String)}).
     *
     * @param link the newly created link
     */
    void createdComponentEventLink(Link link);
}
