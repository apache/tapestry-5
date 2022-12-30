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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.InvalidationListener;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.slf4j.Logger;

/**
 * Base implementation class for classes (especially services) that need to manage a list of
 * {@link org.apache.tapestry5.commons.services.InvalidationListener}s.
 */
public class InvalidationEventHubImpl implements InvalidationEventHub
{
    private final List<Function<List<String>, List<String>>> callbacks;
    
    private final Logger logger;
    
    protected InvalidationEventHubImpl(boolean productionMode, Logger logger)
    {
        if (productionMode)
        {
            callbacks = null;
        } else
        {
            callbacks = CollectionFactory.newThreadSafeList();
        }
        this.logger = logger;
    }

    /**
     * Notifies all listeners/callbacks.
     */
    protected final void fireInvalidationEvent()
    {
        fireInvalidationEvent(Collections.emptyList());
    }
    
    /**
     * Notifies all listeners/callbacks.
     */
    public final void fireInvalidationEvent(List<String> resources)
    {
        if (callbacks == null)
        {
            return;
        }
        
        final Set<String> alreadyProcessed = new HashSet<>();
        
        int level = 1;
        do 
        {
            final Set<String> extraResources = new HashSet<>();
            Set<String> actuallyNewResources;
            if (!resources.isEmpty())
            {
                logger.info("Invalidating {} resource(s) at level {}: {}", resources.size(), level, String.join(", ", resources));
            }
            else
            {
                logger.info("Invalidating all resources");
            }
            for (Function<List<String>, List<String>> callback : callbacks)
            {
                final List<String> newResources = callback.apply(resources);
                if (newResources == null) {
                    throw new TapestryException("InvalidationEventHub callback functions cannot return null", null);
                }
                actuallyNewResources = newResources.stream()
                        .filter(r -> !alreadyProcessed.contains(r))
                        .collect(Collectors.toSet());
                extraResources.addAll(actuallyNewResources);
                alreadyProcessed.addAll(newResources);
            }
            level++;
            resources = new ArrayList<>(extraResources);
        }
        while (!resources.isEmpty());
    }

    public final void addInvalidationCallback(final Runnable callback)
    {
        assert callback != null;

        // In production mode, callbacks may be null, in which case, just
        // ignore the callback.
        if (callbacks != null)
        {
            callbacks.add((r) -> {
                callback.run();
                return Collections.emptyList();
            });
        }
    }

    public /*final*/ void clearOnInvalidation(final Map<?, ?> map)
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

    @Override
    public void addInvalidationCallback(Function<List<String>, List<String>> callback) 
    {
        if (callbacks != null)
        {
            callbacks.add(callback);
        }
    }

}
