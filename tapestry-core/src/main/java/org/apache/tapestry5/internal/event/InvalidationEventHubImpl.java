// Copyright 2006, 2007, 2008, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.InvalidationListener;

import java.util.List;

/**
 * Base implementation class for classes (especially services) that need to manage a list of
 * {@link org.apache.tapestry5.services.InvalidationListener}s.
 */
public class InvalidationEventHubImpl implements InvalidationEventHub
{
    private final List<InvalidationListener> listeners;

    protected InvalidationEventHubImpl(boolean productionMode)
    {
        if (productionMode)
        {
            listeners = null;
        } else
        {
            listeners = CollectionFactory.newThreadSafeList();
        }
    }

    /**
     * Notifies all {@link InvalidationListener listener}s.
     */
    protected final void fireInvalidationEvent()
    {
        if (listeners == null)
        {
            return;
        }

        for (InvalidationListener listener : listeners)
        {
            listener.objectWasInvalidated();
        }
    }

    public final void addInvalidationListener(InvalidationListener listener)
    {
        if (listeners != null)
        {
            listeners.add(listener);
        }
    }
}
