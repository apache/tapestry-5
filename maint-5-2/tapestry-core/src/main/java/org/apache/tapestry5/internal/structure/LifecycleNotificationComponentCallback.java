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

/**
 * Implementation of {@link org.apache.tapestry5.internal.structure.ComponentCallback} used for lifecycle notifications
 * that do not have an event, and can therefore never be aborted.
 *
 * @see org.apache.tapestry5.internal.structure.AbstractComponentCallback
 */
public abstract class LifecycleNotificationComponentCallback implements ComponentCallback
{
    /**
     * Always returns false, as there is no event.
     */
    public boolean isEventAborted()
    {
        return false;
    }
}
