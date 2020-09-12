// Copyright 2006, 2007, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.annotations.PreventServiceDecoration;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;

@PreventServiceDecoration
public class UpdateListenerHubImpl implements UpdateListenerHub
{
    private final List<WeakReference<UpdateListener>> listeners = CollectionFactory.newThreadSafeList();

    @Override
    public void addUpdateListener(UpdateListener listener)
    {
        assert listener != null;
        listeners.add(new WeakReference<UpdateListener>(listener));
    }

    /**
     * Notifies all {@link UpdateListener}s.
     */
    @Override
    public void fireCheckForUpdates()
    {
        List<WeakReference<UpdateListener>> deadReferences = CollectionFactory.newList();

        Iterator<WeakReference<UpdateListener>> i = listeners.iterator();

        while (i.hasNext())
        {
            WeakReference<UpdateListener> reference = i.next();

            UpdateListener listener = reference.get();

            if (listener == null)
                deadReferences.add(reference);
            else
                listener.checkForUpdates();
        }

        if (!deadReferences.isEmpty())
            listeners.removeAll(deadReferences);
    }
}
