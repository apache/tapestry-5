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

import static org.apache.tapestry.ioc.internal.ConfigurationType.MAPPED;
import static org.apache.tapestry.ioc.internal.ConfigurationType.ORDERED;
import static org.apache.tapestry.ioc.internal.ConfigurationType.UNORDERED;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFactory;

/**
 * Basic implementation of {@link org.apache.tapestry.ioc.ObjectCreator} that handles invoking a
 * method on the module builder, and figures out the correct parameters to pass into the annotated
 * method.
 */
public class ServiceBuilderMethodInvoker implements ObjectCreator
{
    private final String _serviceId;

    private final Map<Class, Object> _parameterDefaults = newMap();

    private final ServiceBuilderResources _resources;

    private final Method _builderMethod;

    private final Log _log;

    private final ClassFactory _classFactory;

    private final static Map<Class, ConfigurationType> PARAMETER_TYPE_TO_CONFIGURATION_TYPE = newMap();

    static
    {
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(Collection.class, UNORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(List.class, ORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(Map.class, MAPPED);
    }

    public ServiceBuilderMethodInvoker(Method method, ServiceBuilderResources resources,
            ClassFactory classFactory)
    {
        _serviceId = resources.getServiceId();
        _builderMethod = method;
        _resources = resources;
        _log = resources.getServiceLog();
        _classFactory = classFactory;

        _parameterDefaults.put(String.class, _serviceId);
        _parameterDefaults.put(ServiceResources.class, resources);
        _parameterDefaults.put(Log.class, _log);
        _parameterDefaults.put(Class.class, resources.getServiceInterface());
    }

    private String methodId()
    {
        return InternalUtils.asString(_builderMethod, _classFactory);
    }

    /**
     * Returns a map (based on _parameterDefaults) that includes (possibly) an additional mapping
     * containing the collected configuration data. This involves scanning the builder method's
     * parameters
     */
    private Map<Class, Object> getParameterDefaultsWithConfigurations()
    {
        Map<Class, Object> result = newMap(_parameterDefaults);
        ConfigurationType type = null;

        Class[] parameterTypes = _builderMethod.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++)
        {
            Class parameterType = parameterTypes[i];

            ConfigurationType thisType = PARAMETER_TYPE_TO_CONFIGURATION_TYPE.get(parameterType);

            if (thisType == null) continue;

            if (type != null)
            {
                _log.warn(IOCMessages.tooManyConfigurationParameters(methodId()));
                break;
            }

            // Remember that we've seen a configuration parameter, in case there
            // is another.

            type = thisType;

            Type genericType = _builderMethod.getGenericParameterTypes()[i];

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
    private void addOrderedConfigurationParameter(Map<Class, Object> parameterDefaults,
            Type genericType)
    {
        Class valueType = findParameterizedTypeFromGenericType(genericType);
        List configuration = _resources.getOrderedConfiguration(valueType);

        parameterDefaults.put(List.class, configuration);
    }

    @SuppressWarnings("unchecked")
    private void addUnorderedConfigurationParameter(Map<Class, Object> parameterDefaults,
            Type genericType)
    {
        Class valueType = findParameterizedTypeFromGenericType(genericType);
        Collection configuration = _resources.getUnorderedConfiguration(valueType);

        parameterDefaults.put(Collection.class, configuration);
    }

    @SuppressWarnings("unchecked")
    private void addMappedConfigurationParameter(Map<Class, Object> parameterDefaults,
            Type genericType)
    {
        Class keyType = findParameterizedTypeFromGenericType(genericType, 0);
        Class valueType = findParameterizedTypeFromGenericType(genericType, 1);

        if (keyType == null || valueType == null)
            throw new IllegalArgumentException(IOCMessages.genericTypeNotSupported(genericType));

        Map configuration = _resources.getMappedConfiguration(keyType, valueType);

        parameterDefaults.put(Map.class, configuration);
    }

    /**
     * Extracts from a generic type the underlying parameterized type. I.e., for List<Runnable>,
     * will return Runnable. This is limited to simple parameterized types, not the more complex
     * cases involving wildcards and upper/lower boundaries.
     * 
     * @param type
     *            the genetic type of the parameter, i.e., List<Runnable>
     * @return the parameterize type (i.e. Runnable.class if type represents List<Runnable>).
     */

    // package private for testing
    static Class findParameterizedTypeFromGenericType(Type type)
    {
        Class result = findParameterizedTypeFromGenericType(type, 0);

        if (result == null)
            throw new IllegalArgumentException(IOCMessages.genericTypeNotSupported(type));

        return result;
    }

    /**
     * "Sniffs" a generic type to find the underlying parameterized type. If the Type is a class,
     * then Object.class is returned. Otherwise, the type must be a ParameterizedType. We check to
     * make sure it has the correct number of a actual types (1 for a Collection or List, 2 for a
     * Map). The actual types must be classes (wildcards just aren't supported)
     * 
     * @param type
     *            a Class or ParameterizedType to inspect
     * @param typeIndex
     *            the index within the ParameterizedType to extract
     * @return the actual type, or Object.class if the input type is not generic, or null if any
     *         other pre-condition is not met
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

    /**
     * Invoked from the proxy to create the actual service implementation.
     */
    public Object createObject()
    {
        // Defer getting (and possibly instantitating) the module builder until the last possible
        // moment. If the method is static, there's no need to even get the builder.

        Object moduleBuilder = InternalUtils.isStatic(_builderMethod) ? null : _resources
                .getModuleBuilder();

        Object result = null;
        Throwable failure = null;

        try
        {
            Object[] parameters = InternalUtils.calculateParametersForMethod(
                    _builderMethod,
                    _resources,
                    getParameterDefaultsWithConfigurations());

            if (_log.isDebugEnabled()) _log.debug(IOCMessages.invokingMethod(methodId()));

            result = _builderMethod.invoke(moduleBuilder, parameters);
        }
        catch (InvocationTargetException ite)
        {
            failure = ite.getTargetException();
        }
        catch (Exception ex)
        {
            failure = ex;
        }

        if (failure != null)
            throw new RuntimeException(IOCMessages.builderMethodError(
                    methodId(),
                    _serviceId,
                    failure), failure);

        if (result == null)
            throw new RuntimeException(IOCMessages
                    .builderMethodReturnedNull(methodId(), _serviceId));

        return result;
    }
}
