// Copyright 2008, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.InvalidationListener;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.ComponentLayer;
import org.apache.tapestry5.ioc.annotations.PostInjection;

import java.util.Map;

public class ComponentClassCacheImpl implements ComponentClassCache
{
    private final Map<String, Class> cache = CollectionFactory.newConcurrentMap();

    private final PlasticProxyFactory plasticFactory;

    private final TypeCoercer typeCoercer;

    public ComponentClassCacheImpl(@ComponentLayer
    PlasticProxyFactory plasticFactory, TypeCoercer typeCoercer)
    {
        this.plasticFactory = plasticFactory;
        this.typeCoercer = typeCoercer;
    }

    @PostInjection
    public void setupInvalidation(@ComponentClasses InvalidationEventHub hub) {
        hub.clearOnInvalidation(cache);
    }

    @SuppressWarnings("unchecked")
    public Object defaultValueForType(String className)
    {
        Class clazz = forName(className);

        if (!clazz.isPrimitive())
            return null;

        // Remembering that 0 coerces to boolean false, this covers all the primitive
        // types (boolean, int, short, etc.)
        return typeCoercer.coerce(0, clazz);
    }

    public Class forName(String className)
    {
        Class result = cache.get(className);

        if (result == null)
        {
            result = lookupClassForType(className);

            cache.put(className, result);
        }

        return result;
    }

    private Class lookupClassForType(String className)
    {
        ClassLoader componentLoader = plasticFactory.getClassLoader();
        try
        {
            return PlasticInternalUtils.toClass(componentLoader, className);
        }
        catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
