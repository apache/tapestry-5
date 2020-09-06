// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;

public class ResourceChangeTrackerImpl extends InvalidationEventHubImpl implements ResourceChangeTracker,
        UpdateListener
{
    private final URLChangeTracker tracker;

    /**
     * Used in production mode as the last modified time of any resource exposed to the client. Remember that
     * all exposed assets include a URL with a version number, and each new deployment of the application should change
     * that version number.
     */
    private final long fixedLastModifiedTime = Math.round(System.currentTimeMillis() / 1000d) * 1000L;

    public ResourceChangeTrackerImpl(ClasspathURLConverter classpathURLConverter,
                                     @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                     boolean productionMode)
    {
        super(productionMode);

        // Use granularity of seconds (not milliseconds) since that works properly
        // with response headers for identifying last modified. Don't track
        // folder changes, just changes to actual files.
        tracker = productionMode ? null : new URLChangeTracker(classpathURLConverter, true, false);
    }

    @PostInjection
    public void registerWithUpdateListenerHub(UpdateListenerHub hub)
    {
        hub.addUpdateListener(this);
    }

    public long trackResource(Resource resource)
    {
        if (tracker == null)
        {
            return fixedLastModifiedTime;
        }

        return tracker.add(resource.toURL());
    }

    public void addDependency(Resource dependency)
    {
        trackResource(dependency);
    }

    public void forceInvalidationEvent()
    {
        fireInvalidationEvent();

        if (tracker != null)
        {
            tracker.clear();
        }
    }

    public void checkForUpdates()
    {
        if (tracker.containsChanges())
        {
            forceInvalidationEvent();
        }
    }

}
