// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import static org.apache.tapestry5.ioc.internal.ConfigurationType.*;
import static org.apache.tapestry5.ioc.internal.IOCMessages.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.*;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Starting from the Class for a module builder, identifies all the services (service builder methods), decorators
 * (service decorator methods) and (not yet implemented) contributions (service contributor methods).
 */
public class DefaultModuleDefImpl implements ModuleDef, ServiceDefAccumulator
{
    /**
     * The prefix used to identify service builder methods.
     */
    private static final String BUILD_METHOD_NAME_PREFIX = "build";

    /**
     * The prefix used to identify service decorator methods.
     */
    private static final String DECORATE_METHOD_NAME_PREFIX = "decorate";

    /**
     * The prefix used to identify service contribution methods.
     */
    private static final String CONTRIBUTE_METHOD_NAME_PREFIX = "contribute";

    private final static Map<Class, ConfigurationType> PARAMETER_TYPE_TO_CONFIGURATION_TYPE = newMap();

    private final Class builderClass;

    private final Logger logger;

    private final ClassFactory classFactory;

    /**
     * Keyed on service id.
     */
    private final Map<String, ServiceDef> serviceDefs = newCaseInsensitiveMap();

    /**
     * Keyed on decorator id.
     */
    private final Map<String, DecoratorDef> decoratorDefs = newCaseInsensitiveMap();

    private final Set<ContributionDef> contributionDefs = newSet();

    private final Set<Class> defaultMarkers = newSet();

    static
    {
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(Configuration.class, UNORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(OrderedConfiguration.class, ORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(MappedConfiguration.class, MAPPED);
    }

    /**
     * @param builderClass the class that is responsible for building services, etc.
     * @param logger       based on the class name of the module
     * @param classFactory factory used to create new classes at runtime or locate method line numbers for error
     *                     reporting
     */
    public DefaultModuleDefImpl(Class<?> builderClass, Logger logger, ClassFactory classFactory)
    {
        this.builderClass = builderClass;
        this.logger = logger;
        this.classFactory = classFactory;

        Marker annotation = builderClass.getAnnotation(Marker.class);

        if (annotation != null)
        {
            InternalUtils.validateMarkerAnnotations(annotation.value());
            defaultMarkers.addAll(Arrays.asList(annotation.value()));
        }

        grind();
        bind();
    }

    /**
     * Identifies the module builder class and a list of service ids within the module.
     */
    @Override
    public String toString()
    {
        return String.format("ModuleDef[%s %s]", builderClass.getName(), InternalUtils
                .joinSorted(serviceDefs.keySet()));
    }

    public Class getBuilderClass()
    {
        return builderClass;
    }

    public Set<String> getServiceIds()
    {
        return serviceDefs.keySet();
    }

    public ServiceDef getServiceDef(String serviceId)
    {
        return serviceDefs.get(serviceId);
    }

    private void grind()
    {
        Method[] methods = builderClass.getMethods();

        Comparator<Method> c = new Comparator<Method>()
        {
            // By name, ascending, then by parameter count, descending.

            public int compare(Method o1, Method o2)
            {
                int result = o1.getName().compareTo(o2.getName());

                if (result == 0) result = o2.getParameterTypes().length - o1.getParameterTypes().length;

                return result;
            }
        };

        Arrays.sort(methods, c);

        for (Method m : methods)
        {
            String name = m.getName();

            if (name.startsWith(BUILD_METHOD_NAME_PREFIX))
            {
                addServiceDef(m);
                continue;
            }

            if (name.startsWith(DECORATE_METHOD_NAME_PREFIX))
            {
                addDecoratorDef(m);
                continue;
            }

            if (name.startsWith(CONTRIBUTE_METHOD_NAME_PREFIX))
            {
                addContributionDef(m);
                continue;
            }
        }
    }

    private void addContributionDef(Method method)
    {
        String serviceId = stripMethodPrefix(method, CONTRIBUTE_METHOD_NAME_PREFIX);

        Class returnType = method.getReturnType();
        if (!returnType.equals(void.class)) logger.warn(IOCMessages.contributionWrongReturnType(method));

        ConfigurationType type = null;

        for (Class parameterType : method.getParameterTypes())
        {
            ConfigurationType thisParameter = PARAMETER_TYPE_TO_CONFIGURATION_TYPE
                    .get(parameterType);

            if (thisParameter != null)
            {
                if (type != null)
                {
                    logger.warn(IOCMessages.tooManyContributionParameters(method));
                    return;
                }

                type = thisParameter;
            }
        }

        if (type == null)
        {
            logger.warn(IOCMessages.noContributionParameter(method));
            return;
        }

        ContributionDef def = new ContributionDefImpl(serviceId, method, classFactory);

        contributionDefs.add(def);
    }

    private void addDecoratorDef(Method method)
    {
        // TODO: methods just named "decorate"

        String decoratorId = stripMethodPrefix(method, DECORATE_METHOD_NAME_PREFIX);

        // TODO: Check for duplicates

        Class returnType = method.getReturnType();

        if (returnType.isPrimitive() || returnType.isArray())
        {
            logger.warn(decoratorMethodWrongReturnType(method));
            return;
        }

        Order orderAnnotation = method.getAnnotation(Order.class);
        Match match = method.getAnnotation(Match.class);

        String[] constraints = orderAnnotation != null ? orderAnnotation.value() : null;

        // TODO: Validate constraints here?

        String[] patterns = match == null ? new String[] {decoratorId} : match.value();

        DecoratorDef def = new DecoratorDefImpl(decoratorId, method, patterns, constraints, classFactory);

        decoratorDefs.put(decoratorId, def);
    }

    private boolean methodContainsObjectParameter(Method method)
    {
        for (Class parameterType : method.getParameterTypes())
        {
            // TODO: But what if the type Object parameter has an injection?
            // We should skip it and look for a different parameter.

            if (parameterType.equals(Object.class)) return true;
        }

        return false;
    }

    private String stripMethodPrefix(Method method, String prefix)
    {
        return method.getName().substring(prefix.length());
    }

    /**
     * Invoked for public methods that have the proper prefix.
     */
    private void addServiceDef(final Method method)
    {
        String serviceId = stripMethodPrefix(method, BUILD_METHOD_NAME_PREFIX);

        // If the method name was just "build()", then work from the return type.

        if (serviceId.equals("")) serviceId = method.getReturnType().getSimpleName();

        // Any number of parameters is fine, we'll adapt. Eventually we have to check
        // that we can satisfy the parameters requested. Thrown exceptions of the method
        // will be caught and wrapped, so we don't need to check those. But we do need a proper
        // return type.

        Class returnType = method.getReturnType();

        if (returnType.isPrimitive() || returnType.isArray())
        {
            logger.warn(buildMethodWrongReturnType(method));
            return;
        }

        String scope = extractServiceScope(method);
        boolean eagerLoad = method.isAnnotationPresent(EagerLoad.class);

        ObjectCreatorSource source = new ObjectCreatorSource()
        {
            public ObjectCreator constructCreator(ServiceBuilderResources resources)
            {
                return new ServiceBuilderMethodInvoker(resources, getDescription(), method);
            }

            public String getDescription()
            {
                return InternalUtils.asString(method, classFactory);
            }
        };

        Set<Class> markers = newSet(defaultMarkers);
        markers.addAll(extractMarkers(method));

        ServiceDefImpl serviceDef = new ServiceDefImpl(returnType, serviceId, markers, scope, eagerLoad, source);

        addServiceDef(serviceDef);
    }

    private Collection<Class> extractMarkers(Method method)
    {
        Marker annotation = method.getAnnotation(Marker.class);

        if (annotation == null) return Collections.emptyList();

        return CollectionFactory.newList(annotation.value());
    }

    public void addServiceDef(ServiceDef serviceDef)
    {
        String serviceId = serviceDef.getServiceId();

        ServiceDef existing = serviceDefs.get(serviceId);

        if (existing != null)
            throw new RuntimeException(buildMethodConflict(serviceId, serviceDef.toString(), existing.toString()));

        serviceDefs.put(serviceId, serviceDef);
    }

    private String extractServiceScope(Method method)
    {
        Scope scope = method.getAnnotation(Scope.class);

        return scope != null ? scope.value() : ScopeConstants.DEFAULT;
    }

    public Set<DecoratorDef> getDecoratorDefs()
    {
        return newSet(decoratorDefs.values());
    }

    public Set<ContributionDef> getContributionDefs()
    {
        return contributionDefs;
    }

    public String getLoggerName()
    {
        return builderClass.getName();
    }

    /**
     * See if the build class defined a bind method and invoke it.
     */
    private void bind()
    {
        Throwable failure;
        Method bindMethod = null;

        try
        {
            bindMethod = builderClass.getMethod("bind", ServiceBinder.class);

            if (!Modifier.isStatic(bindMethod.getModifiers()))
            {
                logger.error(IOCMessages.bindMethodMustBeStatic(InternalUtils.asString(bindMethod, classFactory)));

                return;
            }

            ServiceBinderImpl binder = new ServiceBinderImpl(this, bindMethod, classFactory, defaultMarkers);

            bindMethod.invoke(null, binder);

            binder.finish();

            return;
        }
        catch (NoSuchMethodException ex)
        {
            // No problem! Many modules will not have such a method.

            return;
        }
        catch (IllegalArgumentException ex)
        {
            failure = ex;
        }
        catch (IllegalAccessException ex)
        {
            failure = ex;
        }
        catch (InvocationTargetException ex)
        {
            failure = ex.getTargetException();
        }

        String methodId = InternalUtils.asString(bindMethod, classFactory);

        throw new RuntimeException(IOCMessages.errorInBindMethod(methodId, failure), failure);
    }
}
