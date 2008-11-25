// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.services.DataTypeAnalyzer;
import org.apache.tapestry5.services.InvalidationListener;

import java.util.Map;

/**
 * The default data type analyzer, which is based entirely on the type of the property (and not on annotations or naming
 * conventions). This is based on a configuration of property type class to string provided as an IoC service
 * configuration.
 */
public class DefaultDataTypeAnalyzer implements DataTypeAnalyzer, InvalidationListener
{
    private final StrategyRegistry<String> registry;

    public DefaultDataTypeAnalyzer(Map<Class, String> configuration)
    {
        registry = StrategyRegistry.newInstance(String.class, configuration);
    }

    /**
     * Clears the registry on an invalidation event (this is because the registry caches results, and the keys are
     * classes that may be component classes from the invalidated component class loader).
     */
    public void objectWasInvalidated()
    {
        registry.clearCache();
    }

    public String identifyDataType(PropertyAdapter adapter)
    {
        Class propertyType = adapter.getType();

        String dataType = registry.get(propertyType);

        // To avoid "no strategy" exceptions, we expect a contribution of Object.class to the empty
        // string. We convert that back to a null.

        if (dataType.equals(""))
            return null;

        return dataType;
    }
}
