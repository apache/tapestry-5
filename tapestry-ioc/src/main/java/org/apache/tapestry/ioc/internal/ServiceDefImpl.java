// Copyright 2006 The Apache Software Foundation
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

import java.lang.reflect.Method;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * 
 */
public class ServiceDefImpl implements ServiceDef
{
    private final Method _builderMethod;

    private final String _serviceId;

    private final String _lifecycle;

    private final boolean _private;

    private final boolean _eagerLoad;

    ServiceDefImpl(String serviceId, String lifecycle, Method builderMethod, boolean isprivate,
            boolean eagerLoad)
    {
        _serviceId = serviceId;
        _lifecycle = lifecycle;
        _builderMethod = builderMethod;
        _private = isprivate;
        _eagerLoad = eagerLoad;
    }

    @Override
    public String toString()
    {
        return InternalUtils.asString(_builderMethod);
    }

    Method getBuilderMethod()
    {
        return _builderMethod;
    }

    public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
    {
        return new ServiceBuilderMethodInvoker(_builderMethod, resources);
    }

    public String getServiceId()
    {
        return _serviceId;
    }

    public Class getServiceInterface()
    {
        return _builderMethod.getReturnType();
    }

    public String getServiceLifeycle()
    {
        return _lifecycle;
    }

    public boolean isPrivate()
    {
        return _private;
    }

    public boolean isEagerLoad()
    {
        return _eagerLoad;
    }

}
