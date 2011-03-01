// Copyright 2011 The Apache Software Foundation
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

import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.services.UpdateListener;
import org.apache.tapestry5.services.UpdateListenerHub;

public class ResourceChangeTrackerImpl extends InvalidationEventHubImpl implements ResourceChangeTracker,
        UpdateListener
{
    private final URLChangeTracker tracker;

    public ResourceChangeTrackerImpl(ClasspathURLConverter classpathURLConverter)
    {
        // Use granularity of seconds (not milliseconds) since that works properly
        // with response headers for identifying last modified. Don't track
        // folder changes, just changes to actual files.
        tracker = new URLChangeTracker(classpathURLConverter, true, false);
    }

    @PostInjection
    public void registerWithUpdateListenerHub(UpdateListenerHub hub)
    {
        hub.addUpdateListener(this);
    }

    public long trackResource(Resource resource)
    {
        return tracker.add(resource.toURL());
    }

    public void checkForUpdates()
    {
        if (tracker.containsChanges())
        {
            fireInvalidationEvent();
            tracker.clear();
        }
    }

}
