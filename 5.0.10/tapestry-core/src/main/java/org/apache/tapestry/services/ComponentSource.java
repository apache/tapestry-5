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

import org.apache.tapestry.ComponentResourcesCommon;
import org.apache.tapestry.runtime.Component;

/**
 * Used by classes that need to retrieve a component by its complete id, or a page by its logical
 * page name or root component class. The complete id is the logical name of the containing page, a colon,
 * and the nested component id. It may also just be the page name (for the root component of a
 * page).
 */
public interface ComponentSource
{
    /**
     * Gets a component by its id.
     *
     * @param componentId complete component id
     * @return the component
     * @throws IllegalArgumentException if the component can not be found
     * @see ComponentResourcesCommon#getCompleteId()
     */
    Component getComponent(String componentId);

    /**
     * Returns the page identified by its logical page name. A logical page name is the short form
     * of a page name often emebedded into URLs.
     *
     * @param pageName the logical page name
     * @return the corresponding page's root component
     * @throws IllegalArgumentException if the page can not be found
     */
    Component getPage(String pageName);
}
