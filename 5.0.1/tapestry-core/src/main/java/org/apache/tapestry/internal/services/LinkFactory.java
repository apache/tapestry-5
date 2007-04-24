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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Link;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;

/**
 * A source for {@link Link} objects.
 * 
 * @see LinkFactoryListener
 */
public interface LinkFactory
{
    /**
     * Creates a stateful action link. Action links are built for components. Action links are
     * encoded by the current request (that is, bound to the current request's session, if any).
     * 
     * @param component
     *            the component for which an action link is to be generated
     * @param action
     *            a name associated with the action
     * @param forForm
     *            true if the link is for a form, false otherwise
     * @param context
     *            Additional path data, each value will be converted to a string and appended to the
     *            URI
     * @return a link
     */
    Link createActionLink(ComponentPageElement component, String action, boolean forForm,
            Object... context);

    /**
     * Creates a link to a page. TODO: What about context? Additional query parameters?
     * 
     * @param page
     * @return
     */
    Link createPageLink(Page page);

    /** Creates a link to a page, given its logical page name. */
    Link createPageLink(String page);

    void addListener(LinkFactoryListener listener);
}
