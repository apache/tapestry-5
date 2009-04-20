//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

/**
 * Used to determine which field on a page should receive focus, based on its status.
 *
 * @see org.apache.tapestry5.RenderSupport#autofocus(FieldFocusPriority, String)
 */
public enum FieldFocusPriority
{
    /**
     * An optional field, the lowest priority.
     */
    OPTIONAL,

    /**
     * A field whose input is required, which takes higher priority than optional.
     */
    REQUIRED,

    /**
     * A field that contains a validation error, the highest priority.
     */
    IN_ERROR,

    /**
     * Used to allow field focus to be manually overridden; this would be selected in user code and is higher priorty
     * than {@link #IN_ERROR}.
     *
     * @since 5.1.0.4
     */
    OVERRIDE;
}
