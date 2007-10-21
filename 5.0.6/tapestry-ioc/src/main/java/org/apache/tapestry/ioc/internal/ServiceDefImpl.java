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

package org.apache.tapestry.ioc.internal;


import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.def.ServiceDef;

public class ServiceDefImpl implements ServiceDef
{
    private final Class _serviceInterface;

    private final String _serviceId;

    private final String _scope;

    private final boolean _eagerLoad;

    private final ObjectCreatorSource _source;

    private Class _marker;

    ServiceDefImpl(Class serviceInterface, String serviceId, Class marker,
            String scope, boolean eagerLoad, ObjectCreatorSource source)
    {
        _serviceInterface = serviceInterface;
        _serviceId = serviceId;
        _marker = marker;
        _scope = scope;
        _eagerLoad = eagerLoad;
        _source = source;
    }

    @Override
    public String toString()
    {
        return _source.getDescription();
    }

    public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
    {
        return _source.constructCreator(resources);
    }

    public String getServiceId()
    {
        return _serviceId;
    }

    public Class getServiceInterface()
    {
        return _serviceInterface;
    }

    public String getServiceScope()
    {
        return _scope;
    }

    public boolean isEagerLoad()
    {
        return _eagerLoad;
    }

    public Class getMarker()
    {
        return _marker;
    }

}
