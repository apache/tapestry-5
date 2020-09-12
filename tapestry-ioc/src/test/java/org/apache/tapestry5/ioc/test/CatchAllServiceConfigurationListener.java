// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.services.ServiceConfigurationListener;

/**
 * Just for testing {@link ServiceConfigurationListener}.
 */
@SuppressWarnings("rawtypes")
public class CatchAllServiceConfigurationListener implements ServiceConfigurationListener
{
    
    final private Map<String, Map> mappedConfigurations = CollectionFactory.newCaseInsensitiveMap();
    final private Map<String, List> orderedConfigurations = CollectionFactory.newCaseInsensitiveMap();
    final private Map<String, Collection> unorderedConfigurations = CollectionFactory.newCaseInsensitiveMap();

    @Override
    public void onOrderedConfiguration(ServiceDef serviceDef, List configuration)
    {
        orderedConfigurations.put(serviceDef.getServiceId(), configuration);
    }

    @Override
    public void onUnorderedConfiguration(ServiceDef serviceDef, Collection configuration)
    {
        unorderedConfigurations.put(serviceDef.getServiceId(), configuration);
    }

    @Override
    public void onMappedConfiguration(ServiceDef serviceDef, Map configuration)
    {
        mappedConfigurations.put(serviceDef.getServiceId(), configuration);
    }

    public Map<String, Map> getMappedConfigurations()
    {
        return mappedConfigurations;
    }

    public Map<String, List> getOrderedConfigurations()
    {
        return orderedConfigurations;
    }

    public Map<String, Collection> getUnorderedConfigurations()
    {
        return unorderedConfigurations;
    }

}
