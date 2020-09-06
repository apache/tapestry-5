// Copyright 2006, 2007, 2008, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.event;

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.InvalidationListener;
import org.apache.tapestry5.commons.util.CollectionFactory;

import java.util.List;
import java.util.Map;

/**
 * Base implementation class for classes (especially services) that need to manage a list of
 * {@link org.apache.tapestry5.commons.services.InvalidationListener}s.
 */
public class InvalidationEventHubImpl implements InvalidationEventHub
{
    private final List<Runnable> callbacks;

    protected InvalidationEventHubImpl(boolean productionMode)
    {
        if (productionMode)
        {
            callbacks = null;
        } else
        {
            callbacks = CollectionFactory.newThreadSafeList();
        }
    }

    /**
     * Notifies all listeners/callbacks.
     */
    protected final void fireInvalidationEvent()
    {
        if (callbacks == null)
        {
            return;
        }

        for (Runnable callback : callbacks)
        {
            callback.run();
        }
    }

    public final void addInvalidationCallback(Runnable callback)
    {
        assert callback != null;

        // In production mode, callbacks may be null, in which case, just
        // ignore the callback.
        if (callbacks != null)
        {
            callbacks.add(callback);
        }
    }

    public final void clearOnInvalidation(final Map<?, ?> map)
    {
        assert map != null;

        addInvalidationCallback(new Runnable()
        {
            public void run()
            {
                map.clear();
            }
        });
    }

    public final void addInvalidationListener(final InvalidationListener listener)
    {
        assert listener != null;

        addInvalidationCallback(new Runnable()
        {
            public void run()
            {
                listener.objectWasInvalidated();
            }
        });
    }

}
