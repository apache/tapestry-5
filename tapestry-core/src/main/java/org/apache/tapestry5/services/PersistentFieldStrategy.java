// Copyright 2006, 2008 The Apache Software Foundation
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

import java.util.Collection;

/**
 * Defines how changes to fields (within components, within pages) may have their values persisted between requests.
 * Different implementations store the field values {@linkplain org.apache.tapestry5.internal.services.SessionPersistentFieldStrategy
 * in the session}, {@linkplain org.apache.tapestry5.internal.services.ClientPersistentFieldStrategy on the client}, or
 * elsewhere.
 */
public interface PersistentFieldStrategy
{
    /**
     * Posts a change of a persistent property.
     *
     * @param pageName    the name of the page containing the component
     * @param componentId the nested id path of the component (or null for the page's root component)
     * @param fieldName   the name of the field whose persistent value has changed
     * @param newValue    the new value for the field, possibly null
     */
    void postChange(String pageName, String componentId, String fieldName, Object newValue);

    /**
     * Finds all persistent changes previously stored for the named page (for the current active session or client).
     */
    Collection<PersistentFieldChange> gatherFieldChanges(String pageName);

    /**
     * Discards any saved changes for the name page. There is no expectation that data already gathered from the
     * strategy and persumably dumped into component instance fields will be affected, but future field access (within
     * this request or a later one) will show no data for the indicated page.
     *
     * @param pageName logical name of page whose field persistent data should be discarded
     */
    void discardChanges(String pageName);
}
