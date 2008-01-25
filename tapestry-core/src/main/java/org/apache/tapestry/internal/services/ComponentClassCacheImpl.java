// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.ClassFactory;

import java.util.Map;

public class ComponentClassCacheImpl implements ComponentClassCache, InvalidationListener
{
    private final Map<String, Class> _cache = CollectionFactory.newConcurrentMap();

    private final ClassFactory _classFactory;

    public ComponentClassCacheImpl(ClassFactory classFactory)
    {
        _classFactory = classFactory;
    }

    public void objectWasInvalidated()
    {
        _cache.clear();
    }


    public Class forName(final String className)
    {
        Class result = _cache.get(className);

        if (result == null)
        {
            // This step is necessary to handle primitives and, especially, primitive arrays.

            String jvmName = ClassFabUtils.toJVMBinaryName(className);

            ClassLoader componentLoader = _classFactory.getClassLoader();

            try
            {
                result = Class.forName(jvmName, true, componentLoader);
            }
            catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }

            _cache.put(className, result);
        }

        return result;
    }
}
