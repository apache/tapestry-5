// Copyright 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.commons.internal.util.LockSupport;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.OptimizedSessionPersistedObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * A stateless session object used to store Alerts between requests.
 *
 * @since 5.3
 */
public class AlertStorage extends LockSupport implements Serializable, OptimizedSessionPersistedObject
{
    private boolean dirty;

    private final List<Alert> alerts = CollectionFactory.newList();

    public boolean checkAndResetDirtyMarker()
    {
        try
        {
            takeWriteLock();

            return dirty;
        } finally
        {
            dirty = false;

            releaseWriteLock();
        }
    }


    public void add(Alert alert)
    {
        assert alert != null;

        try
        {
            takeWriteLock();

            alerts.add(alert);

            dirty = true;
        } finally
        {
            releaseWriteLock();
        }
    }

    /**
     * Dismisses all Alerts.
     */
    public void dismissAll()
    {
        try
        {
            takeWriteLock();

            if (!alerts.isEmpty())
            {
                alerts.clear();
                dirty = true;
            }
        } finally
        {
            releaseWriteLock();
        }
    }

    /**
     * Dismisses non-persistent Alerts; this is useful after rendering the {@link org.apache.tapestry5.corelib.components.Alerts}
     * component.
     */
    public void dismissNonPersistent()
    {
        try
        {
            takeWriteLock();

            Iterator<Alert> i = alerts.iterator();

            while (i.hasNext())
            {
                if (!i.next().duration.persistent)
                {
                    dirty = true;
                    i.remove();
                }
            }
        } finally
        {
            releaseWriteLock();
        }
    }


    /**
     * Dismisses a single Alert, if present.
     */
    public void dismiss(long alertId)
    {
        try
        {
            takeWriteLock();

            Iterator<Alert> i = alerts.iterator();

            while (i.hasNext())
            {
                if (i.next().id == alertId)
                {
                    i.remove();
                    dirty = true;
                    return;
                }
            }
        } finally
        {
            releaseWriteLock();
        }
    }


    /**
     * Returns all stored alerts.
     *
     * @return list of alerts (possibly empty)
     */
    public List<Alert> getAlerts()
    {
        try
        {
            acquireReadLock();

            return alerts;
        } finally
        {
            releaseReadLock();
        }
    }
}
