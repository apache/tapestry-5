// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.event;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeList;

import java.util.List;

import org.apache.tapestry.internal.events.InvalidationListener;

/**
 * Base implementation class for classes (especially services) that need to manage a list of
 * {@link org.apache.tapestry.internal.events.InvalidationListener}s.
 */
public class InvalidationEventHubImpl implements InvalidationEventHub
{
    private final List<InvalidationListener> _listeners = newThreadSafeList();

    /** Notifies all {@link InvalidationListener listener}s. */
    protected final void fireInvalidationEvent()
    {
        for (InvalidationListener listener : _listeners)
        {
            listener.objectWasInvalidated();
        }
    }

    public final void addInvalidationListener(InvalidationListener listener)
    {
        _listeners.add(listener);
    }
}
