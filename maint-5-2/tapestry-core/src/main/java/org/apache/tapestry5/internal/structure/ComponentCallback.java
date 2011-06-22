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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.runtime.Component;

/**
 * Callback interface, used when invoking lifecycle methods on components.
 */
public interface ComponentCallback
{
    /**
     * Callback method, passed a component to operate upon.
     */
    void run(Component component);

    /**
     * Returns true if the underlying event has been aborted and no further event method invocations should occur.
     *
     * @return true if the event is aborted, false if event processing should continue
     * @see org.apache.tapestry5.runtime.Event#isAborted()
     */
    boolean isEventAborted();
}
