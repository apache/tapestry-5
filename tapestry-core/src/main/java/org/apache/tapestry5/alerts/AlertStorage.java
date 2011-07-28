// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.alerts;

import org.apache.tapestry5.BaseOptimizedSessionPersistedObject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * A stateless session object used to store Alerts between requests.
 *
 * @since 5.3
 */
public class AlertStorage extends BaseOptimizedSessionPersistedObject implements Serializable
{
    private final List<Alert> alerts = CollectionFactory.newList();

    public synchronized void add(Alert alert)
    {
        assert alert != null;
        assert alert.duration.persistent;

        alerts.add(alert);

        markDirty();
    }

    /**
     * Dismisses all Alerts.
     */
    public synchronized void dismissAll()
    {
        if (!alerts.isEmpty())
        {
            alerts.clear();
            markDirty();
        }
    }

    /**
     * Dismisses non-persistent Alerts; this is useful after rendering the {@link org.apache.tapestry5.corelib.components.Alerts}
     * component.
     */
    public synchronized void dismissNonPersistent()
    {
        boolean dirty = false;

        Iterator<Alert> i = alerts.iterator();

        while (i.hasNext())
        {
            if (!i.next().duration.persistent)
            {
                dirty = true;
                i.remove();
            }
        }

        if (dirty)
        {
            markDirty();
        }
    }


    /**
     * Dismisses a single Alert, if present.
     */
    public synchronized void dismiss(long alertId)
    {
        Iterator<Alert> i = alerts.iterator();

        while (i.hasNext())
        {
            if (i.next().id == alertId)
            {
                i.remove();
                markDirty();
                return;
            }
        }
    }


    /**
     * Returns all stored alerts.
     *
     * @return list of alerts (possibly empty)
     */
    public List<Alert> getAlerts()
    {
        return alerts;
    }
}
