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

/**
 *
 */
package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;

/**
 * Provides per-thread implementations of services, along with end-of-request thread cleanup.
 */
public class PerThreadServiceCreator extends ThreadLocal implements ThreadCleanupListener, ObjectCreator
{
    private final ThreadCleanupHub _threadCleanupHub;

    private final ObjectCreator _delegate;

    public PerThreadServiceCreator(ThreadCleanupHub threadCleanupHub, ObjectCreator delegate)
    {
        _threadCleanupHub = threadCleanupHub;
        _delegate = delegate;
    }

    @Override
    protected Object initialValue()
    {
        // First time the value is accessed per thread, set up a callback to clear out the
        // value (at the end of the request) and use the creator to create a new instance.

        _threadCleanupHub.addThreadCleanupListener(this);

        return _delegate.createObject();
    }

    public Object createObject()
    {
        // Get (or create) the service.
        return get();
    }

    public void threadDidCleanup()
    {
        remove();
    }

}