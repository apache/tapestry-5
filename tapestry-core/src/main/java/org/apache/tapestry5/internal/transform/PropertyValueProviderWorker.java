// Copyright 2023 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.plastic.PlasticUtils.FieldInfo;
import org.apache.tapestry5.plastic.PropertyValueProvider;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Worker used to gather {@linkplain FieldInfo} instances and implement
 * {@linkplain PropertyValueProvider} for any class that has them.
 * 
 */
public final class PropertyValueProviderWorker implements ComponentClassTransformWorker2
{
    private final PerThreadValue<Map<PlasticClass, Set<FieldInfo>>> perThreadMap;
    
    public PropertyValueProviderWorker(PerthreadManager perThreadManager)
    {
        perThreadMap = perThreadManager.createValue();
    }
    
    public void add(PlasticClass plasticClass, Set<FieldInfo> fieldInfos)
    {
        final Map<PlasticClass, Set<FieldInfo>> map = perThreadMap.computeIfAbsent(HashMap::new);
        if (!map.containsKey(plasticClass))
        {
            map.put(plasticClass, new HashSet<>(5));
        }
        map.get(plasticClass).addAll(fieldInfos);
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        if (perThreadMap.exists())
        {
            final Set<FieldInfo> fieldInfos = perThreadMap.get().get(plasticClass);
            if (fieldInfos != null && !fieldInfos.isEmpty())
            {
                PlasticUtils.implementPropertyValueProvider(plasticClass, fieldInfos);
            }
        }
    }

}
