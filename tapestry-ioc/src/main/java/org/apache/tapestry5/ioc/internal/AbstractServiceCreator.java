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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.*;
import static org.apache.tapestry5.ioc.internal.ConfigurationType.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.DelegatingInjectionResources;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.slf4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of {@link ObjectCreator} geared towards the creation of the core service implementation,
 * either by invoking a service builder method on a module, or by invoking a constructor.
 */
public abstract class AbstractServiceCreator implements ObjectCreator
{
    protected final String serviceId;

    private final Map<Class, Object> injectionResources = CollectionFactory.newMap();

    protected final ServiceBuilderResources resources;

    protected final Logger logger;

    private final static Map<Class, ConfigurationType> PARAMETER_TYPE_TO_CONFIGURATION_TYPE = CollectionFactory.newMap();

    protected final String creatorDescription;

    static
    {
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(Collection.class, UNORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(List.class, ORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(Map.class, MAPPED);
    }

    public AbstractServiceCreator(ServiceBuilderResources resources, String creatorDescription)
    {
        serviceId = resources.getServiceId();
        this.resources = resources;
        this.creatorDescription = creatorDescription;
        logger = resources.getLogger();

        injectionResources.put(String.class, serviceId);
        injectionResources.put(ObjectLocator.class, resources);
        injectionResources.put(ServiceResources.class, resources);
        injectionResources.put(Logger.class, logger);
        injectionResources.put(Class.class, resources.getServiceInterface());
        injectionResources.put(OperationTracker.class, resources.getTracker());
    }

    /**
     * Returns a map (based on parameterDefaults) that includes (possibly) an additional mapping containing the
     * collected configuration data. This involves scanning the parameters and generic types.
     */
    protected final InjectionResources createInjectionResources()
    {
        InjectionResources core = new MapInjectionResources(injectionResources);

        InjectionResources configurations = new InjectionResources()
        {
            private boolean seenOne;

            public <T> T findResource(Class<T> resourceType, Type genericType)
            {
                ConfigurationType thisType = PARAMETER_TYPE_TO_CONFIGURATION_TYPE.get(resourceType);

                if (thisType == null) return null;

                if (seenOne)
                    throw new RuntimeException(IOCMessages.tooManyConfigurationParameters(creatorDescription));


                seenOne = true;

                switch (thisType)
                {
                    case UNORDERED:

                        return resourceType.cast(getUnorderedConfiguration(genericType));

                    case ORDERED:

                        return resourceType.cast(getOrderedConfiguration(genericType));

                    case MAPPED:

                        return resourceType.cast(getMappedConfiguration(genericType));
                }

                return null;
            }
        };


        return new DelegatingInjectionResources(core, configurations);
    }

    private List getOrderedConfiguration(Type genericType)
    {
        Class valueType = findParameterizedTypeFromGenericType(genericType);
        return resources.getOrderedConfiguration(valueType);
    }


    @SuppressWarnings("unchecked")
    private Collection getUnorderedConfiguration(Type genericType)
    {
        Class valueType = findParameterizedTypeFromGenericType(genericType);

        return resources.getUnorderedConfiguration(valueType);
    }

    @SuppressWarnings("unchecked")
    private Map getMappedConfiguration(Type genericType)
    {
        Class keyType = findParameterizedTypeFromGenericType(genericType, 0);
        Class valueType = findParameterizedTypeFromGenericType(genericType, 1);

        if (keyType == null || valueType == null)
            throw new IllegalArgumentException(IOCMessages.genericTypeNotSupported(genericType));

        return resources.getMappedConfiguration(keyType, valueType);
    }

    /**
     * Extracts from a generic type the underlying parameterized type. I.e., for List<Runnable>, will return Runnable.
     * This is limited to simple parameterized types, not the more complex cases involving wildcards and upper/lower
     * boundaries.
     *
     * @param type the genetic type of the parameter, i.e., List<Runnable>
     * @return the parameterize type (i.e. Runnable.class if type represents List<Runnable>).
     */

    // package private for testing
    static Class findParameterizedTypeFromGenericType(Type type)
    {
        Class result = findParameterizedTypeFromGenericType(type, 0);

        if (result == null) throw new IllegalArgumentException(IOCMessages.genericTypeNotSupported(type));

        return result;
    }

    /**
     * "Sniffs" a generic type to find the underlying parameterized type. If the Type is a class, then Object.class is
     * returned. Otherwise, the type must be a ParameterizedType. We check to make sure it has the correct number of a
     * actual types (1 for a Collection or List, 2 for a Map). The actual types must be classes (wildcards just aren't
     * supported)
     *
     * @param type      a Class or ParameterizedType to inspect
     * @param typeIndex the index within the ParameterizedType to extract
     * @return the actual type, or Object.class if the input type is not generic, or null if any other pre-condition is
     *         not met
     */
    private static Class findParameterizedTypeFromGenericType(Type type, int typeIndex)
    {
        // For a raw Class type, it means the parameter is not parameterized (i.e. Collection, not
        // Collection<Foo>), so we can return Object.class to allow no restriction.

        if (type instanceof Class) return Object.class;

        if (!(type instanceof ParameterizedType)) return null;

        ParameterizedType pt = (ParameterizedType) type;

        Type[] types = pt.getActualTypeArguments();

        Type actualType = types[typeIndex];

        return actualType instanceof Class ? (Class) actualType : null;
    }
}
