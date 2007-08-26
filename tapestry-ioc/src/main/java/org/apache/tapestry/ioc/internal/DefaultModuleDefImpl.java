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
import static org.apache.tapestry.ioc.internal.IOCMessages.buildMethodConflict;
import static org.apache.tapestry.ioc.internal.IOCMessages.buildMethodWrongReturnType;
import static org.apache.tapestry.ioc.internal.IOCMessages.decoratorMethodWrongReturnType;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.ServiceBinder;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.annotations.EagerLoad;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.annotations.Match;
import org.apache.tapestry.ioc.annotations.Order;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.slf4j.Logger;

/**
 * Starting from the Class for a module builder, identifies all the services (service builder
 * methods), decorators (service decorator methods) and (not yet implemented) contributions (service
 * contributor methods).
 */
public class DefaultModuleDefImpl implements ModuleDef, ServiceDefAccumulator
{
    /** The prefix used to identify service builder methods. */
    private static final String BUILD_METHOD_NAME_PREFIX = "build";

    /** The prefix used to identify service decorator methods. */
    private static final String DECORATE_METHOD_NAME_PREFIX = "decorate";

    /** The prefix used to identify service contribution methods. */
    private static final String CONTRIBUTE_METHOD_NAME_PREFIX = "contribute";

    private final Class _builderClass;

    private final Logger _logger;

    private final ClassFactory _classFactory;

    /** Keyed on service id. */
    private final Map<String, ServiceDef> _serviceDefs = newCaseInsensitiveMap();

    /** Keyed on decorator id. */
    private final Map<String, DecoratorDef> _decoratorDefs = newCaseInsensitiveMap();

    private final Set<ContributionDef> _contributionDefs = newSet();

    private final static Map<Class, ConfigurationType> PARAMETER_TYPE_TO_CONFIGURATION_TYPE = newMap();

    static
    {
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(Configuration.class, UNORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(OrderedConfiguration.class, ORDERED);
        PARAMETER_TYPE_TO_CONFIGURATION_TYPE.put(MappedConfiguration.class, MAPPED);
    }

    /**
     * @param builderClass
     *            the class that is responsible for building services, etc.
     * @param logger
     * @param classFactory
     *            TODO
     */
    public DefaultModuleDefImpl(Class builderClass, Logger logger, ClassFactory classFactory)
    {
        _builderClass = builderClass;
        _logger = logger;
        _classFactory = classFactory;

        grind();
        bind();
    }

    /** Identifies the module builder class and a list of service ids within the module. */
    @Override
    public String toString()
    {
        return String.format("ModuleDef[%s %s]", _builderClass.getName(), InternalUtils
                .joinSorted(_serviceDefs.keySet()));
    }

    public Class getBuilderClass()
    {
        return _builderClass;
    }

    public Set<String> getServiceIds()
    {
        return _serviceDefs.keySet();
    }

    public ServiceDef getServiceDef(String serviceId)
    {
        return _serviceDefs.get(serviceId);
    }

    private void grind()
    {
        Method[] methods = _builderClass.getMethods();

        Comparator<Method> c = new Comparator<Method>()
        {
            // By name, ascending, then by parameter count, descending.

            public int compare(Method o1, Method o2)
            {
                int result = o1.getName().compareTo(o2.getName());

                if (result == 0)
                    result = o2.getParameterTypes().length - o1.getParameterTypes().length;

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
        if (!returnType.equals(void.class))
            _logger.warn(IOCMessages.contributionWrongReturnType(method));

        ConfigurationType type = null;

        for (Class parameterType : method.getParameterTypes())
        {
            ConfigurationType thisParameter = PARAMETER_TYPE_TO_CONFIGURATION_TYPE
                    .get(parameterType);

            if (thisParameter != null)
            {
                if (type != null)
                {
                    _logger.warn(IOCMessages.tooManyContributionParameters(method));
                    return;
                }

                type = thisParameter;
            }
        }

        if (type == null)
        {
            _logger.warn(IOCMessages.noContributionParameter(method));
            return;
        }

        ContributionDef def = new ContributionDefImpl(serviceId, method, _classFactory);

        _contributionDefs.add(def);
    }

    private void addDecoratorDef(Method method)
    {
        // TODO: methods just named "decorate"

        String decoratorId = stripMethodPrefix(method, DECORATE_METHOD_NAME_PREFIX);

        // TODO: Check for duplicates

        Class returnType = method.getReturnType();

        if (returnType.isPrimitive() || returnType.isArray())
        {
            _logger.warn(decoratorMethodWrongReturnType(method));
            return;
        }

        if (!methodContainsObjectParameter(method))
        {
            _logger.warn(IOCMessages.decoratorMethodNeedsDelegateParameter(method));
            return;
        }

        // TODO: Check that at least one parameter is type java.lang.Object,
        // since that's how the delegate is passed in.

        Order orderAnnotation = method.getAnnotation(Order.class);
        Match match = method.getAnnotation(Match.class);

        String[] constraints = orderAnnotation != null ? orderAnnotation.value() : null;

        // TODO: Validate constraints here?

        String[] patterns = match == null ? new String[]
        { decoratorId } : match.value();

        DecoratorDef def = new DecoratorDefImpl(decoratorId, method, patterns, constraints,
                _classFactory);

        _decoratorDefs.put(decoratorId, def);
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

    /** Invoked for public methods that have the proper prefix. */
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
            _logger.warn(buildMethodWrongReturnType(method));
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
                return InternalUtils.asString(method, _classFactory);
            }
        };

        ServiceDefImpl serviceDef = new ServiceDefImpl(returnType, serviceId, scope, eagerLoad,
                source);

        addServiceDef(serviceDef);
    }

    public void addServiceDef(ServiceDef serviceDef)
    {
        String serviceId = serviceDef.getServiceId();

        ServiceDef existing = _serviceDefs.get(serviceId);

        if (existing != null)
        {
            _logger.warn(buildMethodConflict(serviceDef.toString(), existing.toString()));
            return;
        }

        _serviceDefs.put(serviceId, serviceDef);
    }

    private String extractServiceScope(Method method)
    {
        Scope scope = method.getAnnotation(Scope.class);

        return scope != null ? scope.value() : IOCConstants.DEFAULT_SCOPE;
    }

    public Set<DecoratorDef> getDecoratorDefs()
    {
        return newSet(_decoratorDefs.values());
    }

    public Set<ContributionDef> getContributionDefs()
    {
        return _contributionDefs;
    }

    public String getLoggerName()
    {
        return _builderClass.getName();
    }

    /** See if the build class defined a bind method and invoke it. */
    private void bind()
    {
        Throwable failure = null;
        Method bindMethod = null;

        try
        {
            bindMethod = _builderClass.getMethod("bind", ServiceBinder.class);

            if (!Modifier.isStatic(bindMethod.getModifiers()))
            {
                _logger.error(IOCMessages.bindMethodMustBeStatic(InternalUtils.asString(
                        bindMethod,
                        _classFactory)));

                return;
            }

            ServiceBinderImpl binder = new ServiceBinderImpl(this, _classFactory);

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

        String methodId = InternalUtils.asString(bindMethod, _classFactory);

        throw new RuntimeException(IOCMessages.errorInBindMethod(methodId, failure), failure);
    }
}
