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

import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.slf4j.Logger;

/**
 * Implementation of {@link org.apache.tapestry.ioc.ServiceBuilderResources}. We just have one
 * implementation that fills the purposes of methods that need a
 * {@link org.apache.tapestry.ioc.ServiceResources} (which includes service decorator methods) as
 * well as methods that need a {@link org.apache.tapestry.ioc.ServiceBuilderResources} (which is
 * just service builder methods). Since it is most commonly used for the former, we'll just leave
 * the name as ServiceResourcesImpl.
 */
public class ServiceResourcesImpl extends ObjectLocatorImpl implements ServiceBuilderResources
{
    private final ServiceDef _serviceDef;

    private final Logger _logger;

    private final ClassFactory _classFactory;

    public ServiceResourcesImpl(InternalRegistry registry, Module module, ServiceDef serviceDef,
            ClassFactory classFactory, Logger logger)
    {
        super(registry, module);

        _serviceDef = serviceDef;
        _classFactory = classFactory;
        _logger = logger;
    }

    public String getServiceId()
    {
        return _serviceDef.getServiceId();
    }

    public Class getServiceInterface()
    {
        return _serviceDef.getServiceInterface();
    }

    public Logger getLogger()
    {
        return _logger;
    }

    public <T> Collection<T> getUnorderedConfiguration(Class<T> valueType)
    {
        Collection<T> result = getRegistry().getUnorderedConfiguration(_serviceDef, valueType);

        logConfiguration(result);

        return result;
    }

    private void logConfiguration(Collection configuration)
    {
        if (_logger.isDebugEnabled())
            _logger.debug(IOCMessages.constructedConfiguration(configuration));
    }

    public <T> List<T> getOrderedConfiguration(Class<T> valueType)
    {
        List<T> result = getRegistry().getOrderedConfiguration(_serviceDef, valueType);

        logConfiguration(result);

        return result;
    }

    public <K, V> Map<K, V> getMappedConfiguration(Class<K> keyType, Class<V> valueType)
    {
        Map<K, V> result = getRegistry().getMappedConfiguration(_serviceDef, keyType, valueType);

        if (_logger.isDebugEnabled()) _logger.debug(IOCMessages.constructedConfiguration(result));

        return result;
    }

    public Object getModuleBuilder()
    {
        return getModule().getModuleBuilder();
    }

    public <T> T autobuild(Class<T> clazz)
    {
        notNull(clazz, "clazz");

        Constructor constructor = InternalUtils.findAutobuildConstructor(clazz);

        if (constructor == null)
            throw new RuntimeException(IOCMessages.noAutobuildConstructor(clazz));

        String description = _classFactory.getConstructorLocation(constructor).toString();

        ObjectCreator creator = new ConstructorServiceCreator(this, description, constructor);

        return clazz.cast(creator.createObject());
    }
}
