// Copyright 2008 The Apache Software Foundation
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

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Base implementation of {@link org.apache.tapestry5.OptimizedSessionPersistedObject}.  Subclasses should invoke {@link
 * #markDirty()} when internal state of the object changes.
 *
 * @since 5.1.1.0
 */
public abstract class BaseOptimizedSessionPersistedObject implements OptimizedSessionPersistedObject, HttpSessionBindingListener
{
    private transient boolean dirty;

    public final boolean isSessionPersistedObjectDirty()
    {
        return dirty;
    }

    /**
     * Invoked by the servlet container when the value is stored (or re-stored) as an attribute of the session. This
     * clears the dirty flag. Subclasses may override this method, but should invoke this implementation.
     */
    public void valueBound(HttpSessionBindingEvent event)
    {
        dirty = false;
    }

    /**
     * Does nothing.
     */
    public void valueUnbound(HttpSessionBindingEvent event)
    {
    }

    /**
     * Invoked by the subclass whenever the internal state of the object changes. Typically, this is invoked from
     * mutator methods.
     */
    protected final void markDirty()
    {
        dirty = true;
    }
}
