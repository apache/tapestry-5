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

import java.util.Collection;

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
     * Finds all persistent changes previously stored for the named page (for the current active
     * session or client).
     */
    Collection<PersistentFieldChange> gatherFieldChanges(String pageName);
}
