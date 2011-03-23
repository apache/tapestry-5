// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ServiceActivity;
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry5.ioc.services.Status;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ServiceActivityTrackerImpl implements ServiceActivityScoreboard,
        ServiceActivityTracker
{
    public static class MutableServiceActivity implements ServiceActivity
    {
        private final ServiceDef serviceDef;

        private Status status;

        public MutableServiceActivity(ServiceDef serviceDef, Status status)
        {
            this.serviceDef = serviceDef;
            this.status = status;
        }

        public String getServiceId()
        {
            return serviceDef.getServiceId();
        }

        public Class getServiceInterface()
        {
            return serviceDef.getServiceInterface();
        }

        public String getScope()
        {
            return serviceDef.getServiceScope();
        }

        // Mutable properties must be synchronized

        public synchronized Status getStatus()
        {
            return status;
        }

        synchronized void setStatus(Status status)
        {
            this.status = status;
        }
    }

    /**
     * Tree map keeps everything in order by key (serviceId).
     */
    private final Map<String, MutableServiceActivity> serviceIdToServiceStatus = new TreeMap<String, MutableServiceActivity>();

    public synchronized List<ServiceActivity> getServiceActivity()
    {
        // Need to wrap the values in a new list because
        // a) we don't want people arbitrarily changing the internal state of
        // _serviceIdtoServiceStatus
        // b) values() is Collection and we want to return List

        // Note: ugly code here to keep Sun compiler happy.

        List<ServiceActivity> result = CollectionFactory.newList();

        result.addAll(serviceIdToServiceStatus.values());

        return result;
    }

    void startup()
    {
        // Does nothing, first pass does not use a worker thread
    }

    void shutdown()
    {
        // Does nothing, first pass does not use a worker thread
    }

    public synchronized void define(ServiceDef serviceDef, Status initialStatus)
    {
        serviceIdToServiceStatus.put(serviceDef.getServiceId(), new MutableServiceActivity(
                serviceDef, initialStatus));
    }

    public synchronized void setStatus(String serviceId, Status status)
    {
        serviceIdToServiceStatus.get(serviceId).setStatus(status);
    }

}
