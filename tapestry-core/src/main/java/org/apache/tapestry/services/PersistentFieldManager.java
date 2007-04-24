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

package org.apache.tapestry.services;

import org.apache.tapestry.ComponentResources;

/**
 * Handle persistent property changes. Primarily, delegates to a number of
 * {@link PersistentFieldStrategy} instances.
 */
public interface PersistentFieldManager
{
    /**
     * Posts a change of a persistent property.
     * 
     * @param pageName
     *            the name of the page containing the component
     * @param resources
     *            the resources for the component or mixin (used to determine the persistence
     *            strategy)
     * @param fieldName
     *            the name of the field whose persistent value has changed
     * @param newValue
     *            the new value for the field, possibly null
     */
    void postChange(String pageName, ComponentResources resources, String fieldName, Object newValue);

    /**
     * Locates all persistently stored changes to all properties within the page (for the current
     * session and request) and gathers them together into a bundle.
     * 
     * @param pageName
     *            the name of the page to gather changes for
     * @return a bundle identifying all such changes
     */
    PersistentFieldBundle gatherChanges(String pageName);
}
