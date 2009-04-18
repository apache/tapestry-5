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

package org.apache.tapestry5.corelib;

/**
 * Identifies how a {@link org.apache.tapestry5.corelib.LoopFormState} component stores state into the {@link
 * org.apache.tapestry5.services.FormSupport} object.
 *
 * @since 5.1.0.4
 */
public enum LoopFormState
{
    /**
     * No state of any kind is stored, as if the Loop was not contained within a {@link
     * org.apache.tapestry5.corelib.components.Form}.
     */
    NONE,

    /**
     * Only enough state to iterate over the source values is stored. When the Form is submitted, the Loop will
     * re-acquire its source and iterate over it. This is equivalent to "volatile" mode in Tapestry 5.0. This can be
     * subject to race conditions when the values within the source change between render and submit.
     */
    ITERATION,

    /**
     * Stores a sequence of values (obtained via a {@link org.apache.tapestry5.ValueEncoder}) into the Form state. The
     * source parameter is <em>not</em> re-acquired when the Form is submitted.
     */
    VALUES
}
