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

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.ServiceResources;
import static org.apache.tapestry5.ioc.internal.ConfigurationType.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
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

    private final Map<Class, Object> parameterDefaults = CollectionFactory.newMap();

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

        parameterDefaults.put(String.class, serviceId);
        parameterDefaults.put(ObjectLocator.class, resources);
        parameterDefaults.put(ServiceResources.class, resources);
        parameterDefaults.put(Logger.class, logger);
        parameterDefaults.put(Class.class, resources.getServiceInterface());
    }

    /**
     * Returns a map (based on _parameterDefaults) that includes (possibly) an additional mapping containing the
     * collected configuration data. This involves scanning the parameters and generic types.
     */
    protected final Map<Class, Object> getParameterDefaultsWithConfiguration(Class[] parameterTypes,
                                                                             Type[] genericParameterTypes)
    {
        Map<Class, Object> result = CollectionFactory.newMap(parameterDefaults);
        ConfigurationType type = null;

        for (int i = 0; i < parameterTypes.length; i++)
        {
            Class parameterType = parameterTypes[i];

            ConfigurationType thisType = PARAMETER_TYPE_TO_CONFIGURATION_TYPE.get(parameterType);

            if (thisType == null) continue;

            if (type != null)
            {
                logger.warn(IOCMessages.tooManyConfigurationParameters(creatorDescription));
                break;
            }

            // Remember that we've seen a configuration parameter, in case there
            // is another.

            type = thisType;

            Type genericType = genericParameterTypes[i];

            switch (type)
            {

                case UNORDERED:

                    addUnorderedConfigurationParameter(result, genericType);

                    break;

                case ORDERED:

                    addOrderedConfigurationParameter(result, genericType);

                    break;

                case MAPPED:

                    addMappedConfigurationParameter(result, genericType);

                    break;
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void addOrderedConfigurationParameter(Map<Class, Object> parameterDefaults, Type genericType)
    {
        Class valueType = findParameterizedTypeFromGenericType(genericType);
        List configuration = resources.getOrderedConfiguration(valueType);

        parameterDefaults.put(List.class, configuration);
    }

    @SuppressWarnings("unchecked")
    private void addUnorderedConfigurationParameter(Map<Class, Object> parameterDefaults, Type genericType)
    {
        Class valueType = findParameterizedTypeFromGenericType(genericType);
        Collection configuration = resources.getUnorderedConfiguration(valueType);

        parameterDefaults.put(Collection.class, configuration);
    }

    @SuppressWarnings("unchecked")
    private void addMappedConfigurationParameter(Map<Class, Object> parameterDefaults, Type genericType)
    {
        Class keyType = findParameterizedTypeFromGenericType(genericType, 0);
        Class valueType = findParameterizedTypeFromGenericType(genericType, 1);

        if (keyType == null || valueType == null)
            throw new IllegalArgumentException(IOCMessages.genericTypeNotSupported(genericType));

        Map configuration = resources.getMappedConfiguration(keyType, valueType);

        parameterDefaults.put(Map.class, configuration);
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
