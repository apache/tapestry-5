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

import static org.apache.tapestry.ioc.IOCUtilities.toQualifiedId;
import static org.apache.tapestry.ioc.internal.ConfigurationType.MAPPED;
import static org.apache.tapestry.ioc.internal.ConfigurationType.ORDERED;
import static org.apache.tapestry.ioc.internal.ConfigurationType.UNORDERED;
import static org.apache.tapestry.ioc.internal.IOCMessages.buildMethodConflict;
import static org.apache.tapestry.ioc.internal.IOCMessages.buildMethodWrongReturnType;
import static org.apache.tapestry.ioc.internal.IOCMessages.decoratorMethodWrongReturnType;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.annotations.Contribute;
import org.apache.tapestry.ioc.annotations.EagerLoad;
import org.apache.tapestry.ioc.annotations.Id;
import org.apache.tapestry.ioc.annotations.Lifecycle;
import org.apache.tapestry.ioc.annotations.Match;
import org.apache.tapestry.ioc.annotations.Order;
import org.apache.tapestry.ioc.annotations.Private;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;

/**
 * Starting from the Class for a module builder, identifies all the services (service builder
 * methods), decorators (service decorator methods) and (not yet implemented) contributions (service
 * contributor methods).
 * 
 * 
 */
public class DefaultModuleDefImpl implements ModuleDef
{
    /** The prefix used to identify service builder methods. */
    private static final String BUILD_METHOD_NAME_PREFIX = "build";

    /** The prefix used to identify service decorator methods. */
    private static final String DECORATE_METHOD_NAME_PREFIX = "decorate";

    /** The prefix used to identify service contribution methods. */
    private static final String CONTRIBUTE_METHOD_NAME_PREFIX = "contribute";

    private final Class _builderClass;

    private final Log _log;

    /** Keyed on fully qualified service id. */
    private final Map<String, ServiceDef> _serviceDefs = newCaseInsensitiveMap();

    /** Keyed on fully qualified decorator id. */
    private final Map<String, DecoratorDef> _decoratorDefs = newCaseInsensitiveMap();

    private final Set<ContributionDef> _contributionDefs = newSet();

    private final String _moduleId;

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
     * @param log
     */
    public DefaultModuleDefImpl(Class builderClass, Log log)
    {
        _builderClass = builderClass;
        _log = log;

        _moduleId = extractModuleId();

        grind();
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

    public String getModuleId()
    {
        return _moduleId;
    }

    private String extractModuleId()
    {
        Id id = getAnnotation(_builderClass, Id.class);

        if (id != null)
            return id.value();

        String className = _builderClass.getName();

        // Don't try to do this with classes in the default package. Then again, you should
        // never put classes in the default package!

        int lastdot = className.lastIndexOf('.');

        return className.substring(0, lastdot);
    }

    // This appears useless, but it's really about some kind of ambiguity in Class, which seems
    // to think getAnnotation() returns Object, not <? extends Annotation>. This may be a bug
    // in Eclipse's Java compiler.

    private <T extends Annotation> T getAnnotation(AnnotatedElement element, Class<T> annotationType)
    {
        return element.getAnnotation(annotationType);
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
            _log.warn(IOCMessages.contributionWrongReturnType(method));

        Contribute contribute = method.getAnnotation(Contribute.class);
        if (contribute != null)
            serviceId = contribute.value();

        ConfigurationType type = null;

        for (Class parameterType : method.getParameterTypes())
        {
            ConfigurationType thisParameter = PARAMETER_TYPE_TO_CONFIGURATION_TYPE
                    .get(parameterType);

            if (thisParameter != null)
            {
                if (type != null)
                {
                    _log.warn(IOCMessages.tooManyContributionParameters(method));
                    return;
                }

                type = thisParameter;
            }
        }

        if (type == null)
        {
            _log.warn(IOCMessages.noContributionParameter(method));
            return;
        }

        // Any other parameters will be validated and worked out at runtime, when we invoke the
        // service contribution method.

        String qualifiedId = IOCUtilities.toQualifiedId(_moduleId, serviceId);

        ContributionDef def = new ContributionDefImpl(qualifiedId, method);

        _contributionDefs.add(def);
    }

    private void addDecoratorDef(Method method)
    {
        // TODO: methods just named "decorate"

        String simpleDecoratorId = stripMethodPrefix(method, DECORATE_METHOD_NAME_PREFIX);
        String id = _moduleId + "." + simpleDecoratorId;

        // TODO: Check for duplicates

        Class returnType = method.getReturnType();

        if (returnType.isPrimitive() || returnType.isArray())
        {
            _log.warn(decoratorMethodWrongReturnType(method), null);
            return;
        }

        if (!methodContainsObjectParameter(method))
        {
            _log.warn(IOCMessages.decoratorMethodNeedsDelegateParameter(method), null);
            return;
        }

        // TODO: Check that at least one parameter is type java.lang.Object,
        // since that's how the delegate is passed in.

        Order orderAnnotation = method.getAnnotation(Order.class);
        Match match = method.getAnnotation(Match.class);

        String[] constraints = orderAnnotation != null ? orderAnnotation.value() : null;

        // TODO: Validate constraints here?

        String[] patterns = match == null ? new String[]
        { simpleDecoratorId } : match.value();

        // Qualify any unqualified match patterns with the decorator's module id.

        for (int i = 0; i < patterns.length; i++)
            patterns[i] = toQualifiedId(_moduleId, patterns[i]);

        DecoratorDef def = new DecoratorDefImpl(id, method, patterns, constraints);

        _decoratorDefs.put(id, def);
    }

    private boolean methodContainsObjectParameter(Method method)
    {
        for (Class parameterType : method.getParameterTypes())
        {
            // TODO: But what if the type Object parameter has an injection?
            // We should skip it and look for a different parameter.

            if (parameterType.equals(Object.class))
                return true;
        }

        return false;
    }

    private String stripMethodPrefix(Method method, String prefix)
    {
        return method.getName().substring(prefix.length());
    }

    /** Invoked for public methods that have the proper prefix. */
    private void addServiceDef(Method method)
    {
        // TODO: Methods named just "build"
        String serviceId = _moduleId + "." + stripMethodPrefix(method, BUILD_METHOD_NAME_PREFIX);

        ServiceDef existing = _serviceDefs.get(serviceId);
        if (existing != null)
        {
            _log.warn(buildMethodConflict(method, existing.toString()), null);
            return;
        }

        // Any number of parameters is fine, we'll adapt. Eventually we have to check
        // that we can satisfy the parameters requested. Thrown exceptions of the method
        // will be caught and wrapped, so we don't need to check those. But we do need a proper
        // return type.

        Class returnType = method.getReturnType();

        if (!returnType.isInterface())
        {
            _log.warn(buildMethodWrongReturnType(method), null);
            return;
        }

        String lifecycle = extractLifecycle(method);
        boolean isPrivate = method.isAnnotationPresent(Private.class);
        boolean eagerLoad = method.isAnnotationPresent(EagerLoad.class);

        _serviceDefs.put(serviceId, new ServiceDefImpl(serviceId, lifecycle, method, isPrivate,
                eagerLoad));
    }

    private String extractLifecycle(Method method)
    {
        Lifecycle lifecycle = method.getAnnotation(Lifecycle.class);

        return lifecycle != null ? lifecycle.value() : IOCConstants.DEFAULT_LIFECYCLE;
    }

    public Set<DecoratorDef> getDecoratorDefs()
    {
        return newSet(_decoratorDefs.values());
    }

    public Set<ContributionDef> getContributionDefs()
    {
        return _contributionDefs;
    }
}
