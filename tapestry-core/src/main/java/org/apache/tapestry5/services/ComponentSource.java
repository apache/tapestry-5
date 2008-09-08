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

import org.apache.tapestry5.runtime.Component;

/**
 * Used by classes that need to retrieve a component by its complete id, or a page by its logical page name or root
 * component class. The complete id is the logical name of the containing page, a colon, and the nested component id. It
 * may also just be the page name (for the root component of a page).
 */
public interface ComponentSource
{
    /**
     * Gets a component by its {@linkplain org.apache.tapestry5.ComponentResourcesCommon#getCompleteId() complete id}.
     * If the component id is for a mixin, then the mixin attached to the component will be returned. A mixin's complete
     * id is its container's complete id, suffixed with "$" and the mixin's id (its simple class name).
     *
     * @param completeId complete component id (case insensitive)
     * @return the component
     * @throws IllegalArgumentException if the component can not be found
     * @see org.apache.tapestry5.ComponentResourcesCommon#getCompleteId()
     */
    Component getComponent(String completeId);

    /**
     * Returns the page identified by its logical page name. A logical page name is the short form of a page name often
     * emebedded into URLs.
     *
     * @param pageName the logical page name
     * @return the corresponding page's root component
     * @throws IllegalArgumentException if the page can not be found
     */
    Component getPage(String pageName);

    /**
     * A convienience for obtaining a page instance via a class instance.  This is provided so as to be refactoring
     * safe.  The pageClass is simply converted to a class name and this is used to locate a page instance.
     *
     * @param pageClass used to locate the page instance
     * @return the page instance
     */
    Component getPage(Class pageClass);
}
