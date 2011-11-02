// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

/**
 * An object that can be locked, at which point most of its functionality is disabled. This conforms to general
 * builder approach used throughout Plastic where objects have an active construction phase, but are then locked
 * (to encourage user code to discard them after they are no longer of any use).
 */
public class Lockable
{
    private boolean locked;

    /**
     * Checks to see if the object has been locked.
     *
     * @throws IllegalStateException if {@link #lock()} has been invoked.
     */
    protected void check()
    {
        if (locked)
            throw new IllegalStateException(toString() + " has been locked and can no longer be used.");
    }

    /**
     * Invokes {@link #check()}, then sets the locked flag. Subsequent calls to {@link #check()} will fail.
     */
    protected void lock()
    {
        check();

        locked = true;
    }
}
