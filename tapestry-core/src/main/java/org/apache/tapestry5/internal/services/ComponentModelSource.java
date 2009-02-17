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

import org.apache.tapestry5.model.ComponentModel;

/**
 * Access to component models (as provided via {@link org.apache.tapestry5.internal.services.ComponentInstantiatorSource}).
 * <p/>
 * This is a good candidate to move into the public services package.
 *
 * @since 5.1.0.0
 */
public interface ComponentModelSource
{
    /**
     * Returns the model for a particular component class name.
     *
     * @param componentClassName name of component class
     * @return model for component
     * @throws IllegalArgumentException if component class name does not match a known component
     */
    ComponentModel getModel(String componentClassName);

    /**
     * Returns the model for a page.  The page name is resolved to a component class name.
     *
     * @param pageName name of page
     * @return the model for the page
     * @throws IllegalArgumentException if the page name is not a known page name
     */
    ComponentModel getPageModel(String pageName);
}
