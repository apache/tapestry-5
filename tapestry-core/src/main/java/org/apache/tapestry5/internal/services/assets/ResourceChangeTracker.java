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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.UpdateListener;
import org.apache.tapestry5.services.assets.ResourceDependencies;

/**
 * Tracks resources (at least, resources that can change because they are on the file system) and
 * acts as an {@link UpdateListener} to check for changes and notify its listeners.
 *
 * @since 5.3
 */
public interface ResourceChangeTracker extends InvalidationEventHub, ResourceDependencies
{
    /**
     * Start tracking the resource (or return the last modified time of an already tracked resource). Only file system
     * resources are tracked. Resources are tracked until <em>any</em> resource changes, at which points
     * {@linkplain InvalidationListener listeners} are notified and the internal state
     * is cleared.
     *
     * @param resource to track
     * @return last modified time, to nearest second
     * @see URLChangeTracker
     */
    long trackResource(Resource resource);
}
