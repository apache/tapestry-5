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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class PerthreadManagerImpl implements PerthreadManager
{
    private static final String LISTENERS_KEY = "PerthreadManager.listenerList";

    private static class MapHolder extends ThreadLocal<Map>
    {
        @Override
        protected Map initialValue()
        {
            return CollectionFactory.newMap();
        }
    }

    private final Logger logger;

    private final MapHolder holder = new MapHolder();

    public PerthreadManagerImpl(Logger logger)
    {
        this.logger = logger;
    }


    private synchronized Map getPerthreadMap()
    {
        return holder.get();
    }


    private List<ThreadCleanupListener> getListeners()
    {
        List<ThreadCleanupListener> result = (List<ThreadCleanupListener>) get(LISTENERS_KEY);

        if (result == null)
        {
            result = CollectionFactory.newList();
            put(LISTENERS_KEY, result);
        }

        return result;
    }


    public void addThreadCleanupListener(ThreadCleanupListener listener)
    {
        getListeners().add(listener);
    }

    /**
     * Instructs the hub to notify all its listeners (for the current thread). It also discards its list of listeners.
     */
    public void cleanup()
    {
        List<ThreadCleanupListener> listeners = getListeners();

        put(LISTENERS_KEY, null);

        for (ThreadCleanupListener listener : listeners)
        {
            try
            {
                listener.threadDidCleanup();
            }
            catch (Exception ex)
            {
                logger.warn(ServiceMessages.threadCleanupError(listener, ex), ex);
            }
        }

        // Listeners should not re-add themselves or store any per-thread state here,
        // it will be lost.

        synchronized (this)
        {
            holder.remove();
        }
    }

    public void put(Object key, Object value)
    {
        getPerthreadMap().put(key, value);
    }

    public Object get(Object key)
    {
        return getPerthreadMap().get(key);
    }
}
