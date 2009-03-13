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

package org.apache.tapestry5.services;

/**
 * Encapsulates persisted property information for an entire page.
 */
public interface PersistentFieldBundle
{
    /**
     * Checks to see if a persistent value has been stored for the indicated component and field. TODO: This method can
     * probably be removed; it doesn't look like its used (instead, we if check getValue() returns null).
     *
     * @param componentId the nested id of the component (within the page), may be null or blank for the root component
     *                    of the page
     * @param fieldName   the name of the field whose value was persisted
     * @return true if a change has been stored
     */
    boolean containsValue(String componentId, String fieldName);

    /**
     * @param componentId the nested if of the component (within the page), may be null or blank for the root component
     *                    of the page
     * @param fieldName   the name of the field whose value was persisted
     * @return the persisted value, possibly null
     */
    Object getValue(String componentId, String fieldName);
}
