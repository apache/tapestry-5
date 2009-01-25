// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.def.ServiceDef2;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import static org.apache.tapestry5.ioc.internal.util.InternalUtils.asString;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import static org.apache.tapestry5.ioc.services.ClassFabUtils.toJavaClassName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class IOCMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(IOCMessages.class);

    static String buildMethodConflict(String serviceId, String conflict, String existing)
    {
        return MESSAGES.format("build-method-conflict", serviceId, conflict, existing);
    }

    static String buildMethodWrongReturnType(Method method)
    {
        return MESSAGES.format("build-method-wrong-return-type", asString(method), method
                .getReturnType().getCanonicalName());
    }

    static String decoratorMethodWrongReturnType(Method method)
    {
        return MESSAGES.format("decorator-method-wrong-return-type", asString(method), method
                .getReturnType().getCanonicalName());
    }

    public static String builderLocked()
    {
        return MESSAGES.get("builder-locked");
    }

    static String serviceWrongInterface(String serviceId, Class actualInterface, Class requestedInterface)
    {
        return MESSAGES.format("service-wrong-interface", serviceId, actualInterface.getName(),
                               requestedInterface.getName());
    }

    static String instantiateBuilderError(Class builderClass, Throwable cause)
    {
        return MESSAGES.format("instantiate-builder-error", builderClass.getName(), cause);
    }

    static String builderMethodError(String methodId, String serviceId, Throwable cause)
    {
        return MESSAGES.format("builder-method-error", methodId, serviceId, cause);
    }

    static String constructorError(String creatorDescription, String serviceId, Throwable cause)
    {
        return MESSAGES.format("constructor-error", creatorDescription, serviceId, cause);
    }

    static String builderMethodReturnedNull(String methodId, String serviceId)
    {
        return MESSAGES.format("builder-method-returned-null", methodId, serviceId);
    }

    static String noServiceMatchesType(Class serviceInterface)
    {
        return MESSAGES.format("no-service-matches-type", serviceInterface.getName());
    }

    static String manyServiceMatches(Class serviceInterface, List<String> ids)
    {
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < ids.size(); i++)
        {
            if (i > 0) buffer.append(", ");

            buffer.append(ids.get(i));
        }

        return MESSAGES.format("many-service-matches", serviceInterface.getName(), ids.size(), buffer.toString());
    }

    static String unknownScope(String name)
    {
        return MESSAGES.format("unknown-scope", name);
    }

    static String decoratorReturnedWrongType(Method method, String serviceId, Object returned, Class serviceInterface)
    {
        return MESSAGES.format("decorator-returned-wrong-type", asString(method), serviceId, returned,
                               serviceInterface.getName());
    }

    static String creatingService(String serviceId)
    {
        return MESSAGES.format("creating-service", serviceId);
    }

    static String invokingMethod(String methodId)
    {
        return MESSAGES.format("invoking-method", methodId);
    }

    static String invokingConstructor(String creatorDescription)
    {
        return MESSAGES.format("invoking-constructor", creatorDescription);
    }

    static String invokingMethod(ContributionDef def)
    {
        // The toString() of a contribution def is the name of the method.
        return MESSAGES.format("invoking-method", def);
    }

    static String recursiveServiceBuild(ServiceDef def)
    {
        return MESSAGES.format("recursive-service-build", def.getServiceId(), def.toString());
    }

    static String contributionWrongReturnType(Method method)
    {
        return MESSAGES.format("contribution-wrong-return-type", asString(method),
                               toJavaClassName(method.getReturnType()));
    }

    static String tooManyContributionParameters(Method method)
    {
        return MESSAGES.format("too-many-contribution-parameters", asString(method));
    }

    static String noContributionParameter(Method method)
    {
        return MESSAGES.format("no-contribution-parameter", asString(method));
    }

    static String contributionMethodError(Method method, Throwable cause)
    {
        return MESSAGES.format("contribution-method-error", asString(method), cause);
    }

    static String contributionWasNull(String serviceId)
    {
        return MESSAGES.format("contribution-was-null", serviceId);
    }

    static String contributionKeyWasNull(String serviceId)
    {
        return MESSAGES.format("contribution-key-was-null", serviceId);
    }

    static String contributionWrongValueType(String serviceId, Class actualClass,
                                             Class expectedClass)
    {
        return MESSAGES.format("contribution-wrong-value-type", serviceId, actualClass
                .getName(), expectedClass.getName());
    }

    static String contributionWrongKeyType(String serviceId, Class actualClass,
                                           Class expectedClass)
    {
        return MESSAGES.format("contribution-wrong-key-type", serviceId, actualClass.getName(),
                               expectedClass.getName());
    }

    static String tooManyConfigurationParameters(String methodId)
    {
        return MESSAGES.format("too-many-configuration-parameters", methodId);
    }

    static String genericTypeNotSupported(Type type)
    {
        return MESSAGES.format("generic-type-not-supported", type);
    }

    static String contributionDuplicateKey(String serviceId, ContributionDef existingDef)
    {
        return MESSAGES.format("contribution-duplicate-key", serviceId, existingDef);
    }

    static String errorBuildingService(String serviceId, ServiceDef serviceDef, Throwable cause)
    {
        return MESSAGES.format("error-building-service", serviceId, serviceDef, cause);
    }

    static String noPublicConstructors(Class moduleClass)
    {
        return MESSAGES.format("no-public-constructors", moduleClass.getName());
    }

    static String tooManyPublicConstructors(Class moduleClass, Constructor constructor)
    {
        return MESSAGES.format("too-many-public-constructors", moduleClass.getName(), constructor);
    }

    static String recursiveModuleConstructor(Class builderClass, Constructor constructor)
    {
        return MESSAGES.format("recursive-module-constructor", builderClass.getName(), constructor);
    }

    static String constructedConfiguration(Collection result)
    {
        return MESSAGES.format("constructed-configuration", result);
    }

    static String constructedConfiguration(Map result)
    {
        return MESSAGES.format("constructed-configuration", result);
    }

    static String serviceConstructionFailed(ServiceDef serviceDef, Throwable cause)
    {
        return MESSAGES.format("service-construction-failed", serviceDef.getServiceId(), cause);
    }

    static String noSuchService(String serviceId, Collection<String> serviceIds)
    {
        return MESSAGES.format("no-such-service", serviceId, InternalUtils.joinSorted(serviceIds));
    }

    static String serviceIdConflict(String serviceId, ServiceDef existing, ServiceDef conflicting)
    {
        return MESSAGES.format("service-id-conflict", serviceId, existing, conflicting);
    }

    static String noConstructor(Class implementationClass, String serviceId)
    {
        return MESSAGES.format("no-constructor", implementationClass.getName(), serviceId);
    }

    static String bindMethodMustBeStatic(String methodId)
    {
        return MESSAGES.format("bind-method-must-be-static", methodId);
    }

    static String errorInBindMethod(String methodId, Throwable cause)
    {
        return MESSAGES.format("error-in-bind-method", methodId, cause);
    }

    static String noAutobuildConstructor(Class clazz)
    {
        return MESSAGES.format("no-autobuild-constructor", clazz.getName());
    }

    static String autobuildConstructorError(String constructorDescription, Throwable cause)
    {
        return MESSAGES.format("autobuild-constructor-error", constructorDescription, cause);
    }

    private static String toJavaClassNames(List<Class> classes)
    {
        List<String> names = CollectionFactory.newList();

        for (Class<?> clazz : classes)
        {
            names.add(ClassFabUtils.toJavaClassName(clazz));
        }

        return InternalUtils.joinSorted(names);
    }

    static String noServicesMatchMarker(Class objectType, List<Class> markers)
    {
        return MESSAGES.format("no-services-match-marker",
                               ClassFabUtils.toJavaClassName(objectType),
                               toJavaClassNames(markers));
    }

    static String manyServicesMatchMarker(Class objectType, List<Class> markers,
                                          Collection<ServiceDef2> matchingServices)
    {
        return MESSAGES.format("many-services-match-marker",
                               ClassFabUtils.toJavaClassName(objectType),
                               toJavaClassNames(markers),
                               InternalUtils.joinSorted(matchingServices));
    }

    static String overlappingServiceProxyProviders()
    {
        return MESSAGES.get("overlapping-service-proxy-providers");
    }

    static String unexpectedServiceProxyProvider()
    {
        return MESSAGES.get("unexpected-service-proxy-provider");
    }

    static String noProxyProvider(String serviceId)
    {
        return MESSAGES.format("no-proxy-provider", serviceId);
    }

    static String noConventionServiceImplementationFound(Class clazz)
    {
        return MESSAGES.format("no-convention-service-implementation-found", clazz.getName(), clazz.getName());
    }
}
