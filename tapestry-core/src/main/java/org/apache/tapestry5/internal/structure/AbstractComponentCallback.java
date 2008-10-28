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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.runtime.Event;

/**
 * Base class for most implementations of {@link org.apache.tapestry5.internal.structure.ComponentCallback}, used when
 * there is an underlying {@link org.apache.tapestry5.runtime.Event}.
 *
 * @see LifecycleNotificationComponentCallback
 */
public abstract class AbstractComponentCallback implements ComponentCallback
{
    private final Event event;

    public AbstractComponentCallback(Event event)
    {
        this.event = event;
    }

    public boolean isEventAborted()
    {
        return event.isAborted();
    }
}
