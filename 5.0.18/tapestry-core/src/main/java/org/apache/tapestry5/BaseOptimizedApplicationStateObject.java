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

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Base class for creating optimized application state objects.  Works as a {@link
 * javax.servlet.http.HttpSessionBindingListener} to determine when the object is no longer dirty.
 */
public abstract class BaseOptimizedApplicationStateObject implements OptimizedApplicationStateObject, HttpSessionBindingListener
{
    private transient boolean dirty;

    public final boolean isApplicationStateObjectDirty()
    {
        return dirty;
    }

    /**
     * Invoked by the servlet container when the value is stored (or re-stored) as an attribute of the session. This
     * clears the dirty flag.
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
     * Invoked by the subclass whenever the internal state of the ASO changes. Typically, this is invoked from mutator
     * methods.
     */
    protected final void markDirty()
    {
        dirty = true;
    }
}
