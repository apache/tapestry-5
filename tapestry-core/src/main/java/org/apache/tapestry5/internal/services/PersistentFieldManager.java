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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.apache.tapestry5.services.PersistentFieldStrategy;

/**
 * Handle persistent property changes. Primarily, delegates to a number of {@link org.apache.tapestry5.services.PersistentFieldStrategy}
 * instances.
 */
@UsesMappedConfiguration(PersistentFieldStrategy.class)
public interface PersistentFieldManager
{
    /**
     * Posts a change of a persistent property.
     *
     * @param pageName  the logical name of the page containing the component
     * @param resources the resources for the component or mixin (used to determine the persistence strategy)
     * @param fieldName the name of the field whose persistent value has changed
     * @param newValue  the new value for the field, possibly null
     */
    void postChange(String pageName, ComponentResources resources, String fieldName, Object newValue);

    /**
     * Locates all persistently stored changes to all properties within the page (for the current session and request)
     * and gathers them together into a bundle.
     *
     * @param pageName the logical name of the page to gather changes for
     * @return a bundle identifying all such changes
     */
    PersistentFieldBundle gatherChanges(String pageName);

    /**
     * Discards all changes for the indicated page. This will not affect pages that have already been attached to this
     * request, but will affect subsequent page attachments in this and later requests.
     *
     * @param pageName logical name of page whose persistent field data is to be discarded
     */
    void discardChanges(String pageName);
}
