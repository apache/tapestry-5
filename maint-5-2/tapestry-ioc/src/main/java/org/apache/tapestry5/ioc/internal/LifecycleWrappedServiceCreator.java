// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.ServiceResources;

/**
 * Wrapper around a lifecycle, a set of resources for a service, and an underlying {@link ObjectCreator} for a service
 * that allows the service lifecycle to alter the way that the service is created (this is needed for the more advanced,
 * non-singleton types of service lifecycles).
 */
public class LifecycleWrappedServiceCreator implements ObjectCreator
{
     private final ServiceLifecycle lifecycle;

    private final ServiceResources resources;

    private final ObjectCreator creator;

    public LifecycleWrappedServiceCreator(ServiceLifecycle lifecycle, ServiceResources resources, ObjectCreator creator)
    {
        this.lifecycle = lifecycle;
        this.resources = resources;
        this.creator = creator;
    }

    /**
     * Passes the resources and the service creator through the {@link org.apache.tapestry5.ioc.ServiceLifecycle}.
     */
    public Object createObject()
    {
        return lifecycle.createService(resources, creator);
    }

}
