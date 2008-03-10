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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.events.UpdateListener;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newThreadSafeList;

import java.util.List;

public class UpdateListenerHubImpl implements UpdateListenerHub
{
    private final List<UpdateListener> _listeners = newThreadSafeList();

    public void addUpdateListener(UpdateListener listener)
    {
        _listeners.add(listener);

    }

    /**
     * Notifies all {@link UpdateListener}s.
     */
    public void fireUpdateEvent()
    {
        for (UpdateListener listener : _listeners)
        {
            listener.checkForUpdates();
        }
    }
}
