// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services.javascript;

/**
 * Sets the priority for JavaScript initialization scripting. InitializationPriority allows coarse-grained control
 * over the order in which initialization occurs on the client. The default is normally {@link #NORMAL}.
 * 
 * @since 5.2.0
 */
public enum InitializationPriority
{
    /**
     * Provided JavaScript will be executed immediately (it is not deferred until the page loads). In an Ajax
     * update, IMMEDIATE code executed after the DOM is updated and before EARLY.
     */
    IMMEDIATE,

    /** Execution is deferred until the page loads. All early execution occurs before {@link #NORMAL}. */
    EARLY,

    /** Execution is deferred until the page loads. This is the typical priority. */
    NORMAL,

    /** Execution is deferred until the page loads. Execution occurs after {@link #NORMAL}. */
    LATE
}
