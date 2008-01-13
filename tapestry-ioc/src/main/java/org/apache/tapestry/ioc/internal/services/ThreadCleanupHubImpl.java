// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;
import org.slf4j.Logger;

import java.util.List;

public class ThreadCleanupHubImpl implements ThreadCleanupHub
{
    private static class ListHolder extends ThreadLocal<List<ThreadCleanupListener>>
    {
        @Override
        protected List<ThreadCleanupListener> initialValue()
        {
            return newList();
        }
    }

    private final Logger _logger;

    private final ListHolder _holder = new ListHolder();

    public ThreadCleanupHubImpl(Logger logger)
    {
        _logger = logger;
    }

    public synchronized void addThreadCleanupListener(ThreadCleanupListener listener)
    {
        _holder.get().add(listener);
    }

    /**
     * Instructs the hub to notify all its listeners (for the current thread). It also discards its list of listeners.
     */
    public synchronized void cleanup()
    {
        List<ThreadCleanupListener> listeners = _holder.get();

        // Discard the listeners. In a perfect world, we would set a per-thread flag that prevented
        // more listeners from being added, until a new thread begins. But we don't have a concept
        // of thread start, just thread complete.

        _holder.remove();

        for (ThreadCleanupListener listener : listeners)
        {
            try
            {
                listener.threadDidCleanup();
            }
            catch (Exception ex)
            {
                _logger.warn(ServiceMessages.threadCleanupError(listener, ex), ex);
            }
        }

    }

}
