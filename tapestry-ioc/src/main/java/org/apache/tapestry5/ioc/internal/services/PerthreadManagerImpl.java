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

package org.apache.tapestry5.ioc.internal.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;
import org.slf4j.Logger;

@SuppressWarnings("all")
public class PerthreadManagerImpl implements PerthreadManager
{
    private final PerThreadValue<List<Runnable>> callbacksValue;

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

    private final AtomicInteger uuidGenerator = new AtomicInteger();

    private volatile boolean shutdown = false;

    public PerthreadManagerImpl(Logger logger)
    {
        this.logger = logger;

        callbacksValue = createValue();
    }

    public void registerForShutdown(RegistryShutdownHub hub)
    {
        hub.addRegistryShutdownListener(new Runnable()
        {
            @Override
            public void run()
            {
                cleanup();
                shutdown = true;
            }
        });
    }

    private Map getPerthreadMap()
    {
        // This is a degenerate case; it may not even exist; but if during registry shutdown somehow code executes
        // that attempts to create new values or add new listeners, those go into a new map instance that is
        // not referenced (and so immediately GCed).
        if (shutdown)
        {
            return CollectionFactory.newMap();
        }

        return holder.get();
    }

    private List<Runnable> getCallbacks()
    {
        List<Runnable> result = callbacksValue.get();

        if (result == null)
        {
            result = CollectionFactory.newList();
            callbacksValue.set(result);
        }

        return result;
    }

    @Override
    public void addThreadCleanupListener(final ThreadCleanupListener listener)
    {
        assert listener != null;

        addThreadCleanupCallback(new Runnable()
        {
            @Override
            public void run()
            {
                listener.threadDidCleanup();
            }
        });
    }

    @Override
    public void addThreadCleanupCallback(Runnable callback)
    {
        assert callback != null;

        getCallbacks().add(callback);
    }

    /**
     * Instructs the hub to notify all its listeners (for the current thread).
     * It also discards its list of listeners.
     */
    @Override
    public void cleanup()
    {
        List<Runnable> callbacks = getCallbacks();

        callbacksValue.set(null);

        for (Runnable callback : callbacks)
        {
            try
            {
                callback.run();
            } catch (Exception ex)
            {
                logger.warn("Error invoking callback {}: {}", callback, ex, ex);
            }
        }

        // Listeners should not re-add themselves or store any per-thread state
        // here, it will be lost.

        // Discard the per-thread map of values, including the key that stores
        // the listeners. This means that if a listener attempts to register
        // new listeners, the new listeners will not be triggered and will be
        // released to the GC.

        holder.remove();
    }

    private static Object NULL_VALUE = new Object();

    <T> ObjectCreator<T> createValue(final Object key, final ObjectCreator<T> delegate)
    {
        return new DefaultObjectCreator<T>(key, delegate);
    }

    public <T> ObjectCreator<T> createValue(ObjectCreator<T> delegate)
    {
        return createValue(uuidGenerator.getAndIncrement(), delegate);
    }

    <T> PerThreadValue<T> createValue(final Object key)
    {
        return new DefaultPerThreadValue(key);
    }

    @Override
    public <T> PerThreadValue<T> createValue()
    {
        return createValue(uuidGenerator.getAndIncrement());
    }

    @Override
    public void run(Runnable runnable)
    {
        assert runnable != null;

        try
        {
            runnable.run();
        } finally
        {
            cleanup();
        }
    }

    @Override
    public <T> T invoke(Invokable<T> invokable)
    {
        try
        {
            return invokable.invoke();
        } finally
        {
            cleanup();
        }
    }

    private final class DefaultPerThreadValue<T> implements PerThreadValue<T>
    {
        private final Object key;

        DefaultPerThreadValue(final Object key)
        {
            this.key = key;

        }
        @Override
        public T get()
        {
            return get(null);
        }

        @Override
        public T get(T defaultValue)
        {
            Map map = getPerthreadMap();

            Object storedValue = map.get(key);

            if (storedValue == null)
            {
                return defaultValue;
            }

            if (storedValue == NULL_VALUE)
            {
                return null;
            }

            return (T) storedValue;
        }

        @Override
        public T set(T newValue)
        {
            getPerthreadMap().put(key, newValue == null ? NULL_VALUE : newValue);

            return newValue;
        }

        @Override
        public boolean exists()
        {
            return getPerthreadMap().containsKey(key);
        }
    }

    private final class DefaultObjectCreator<T> implements ObjectCreator<T>
    {

        private final Object key;
        private final ObjectCreator<T> delegate;

        DefaultObjectCreator(final Object key, final ObjectCreator<T> delegate)
        {
            this.key = key;
            this.delegate = delegate;
        }

        public T createObject()
        {
            Map map = getPerthreadMap();
            T storedValue = (T) map.get(key);

            if (storedValue != null)
            {
                return (storedValue == NULL_VALUE) ? null : storedValue;
            }

            T newValue = delegate.createObject();

            map.put(key, newValue == null ? NULL_VALUE : newValue);

            return newValue;
        }
    }
}
