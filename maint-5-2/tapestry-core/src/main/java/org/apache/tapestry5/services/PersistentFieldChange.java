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
 * Represents a previously stored change to a persistent field, within the context of a particular page of the
 * application.
 */
public interface PersistentFieldChange
{
    /**
     * Returns the nested id of the component, or the empty string for the page's root component.
     */
    String getComponentId();

    /**
     * Returns the name of the field for which a change was recorded.
     */
    String getFieldName();

    /**
     * Returns the new value for the field (which may be null).
     */
    Object getValue();
}
