// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.InternalUtils.asString;
import static org.apache.tapestry.ioc.services.ClassFabUtils.getJavaClassName;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.MessagesImpl;

public final class IOCMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(IOCMessages.class);

    private IOCMessages()
    {
    }

    static String buildMethodConflict(Method conflict, String existing)
    {
        return MESSAGES.format("build-method-conflict", asString(conflict), existing);
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

    static String noSuchModule(String moduleId)
    {
        return MESSAGES.format("no-such-module", moduleId);
    }

    static String missingService(String serviceId)
    {
        return MESSAGES.format("missing-service", serviceId);
    }

    public static String builderLocked()
    {
        return MESSAGES.get("builder-locked");
    }

    public static String moduleIdConflict(String id)
    {
        return MESSAGES.format("module-id-conflict", id);
    }

    static String serviceWrongInterface(String serviceId, Class actualInterface,
            Class requestedInterface)
    {
        return MESSAGES.format(
                "service-wrong-interface",
                serviceId,
                actualInterface.getName(),
                requestedInterface.getName());
    }

    static String instantiateBuilderError(Class builderClass, String moduleId, Throwable cause)
    {
        return MESSAGES
                .format("instantiate-builder-error", builderClass.getName(), moduleId, cause);
    }

    static String builderMethodError(Method method, String serviceId, Throwable cause)
    {
        return MESSAGES.format("builder-method-error", asString(method), serviceId, cause);
    }

    static String decoratorMethodError(Method method, String serviceId, Throwable cause)
    {
        return MESSAGES.format("decorator-method-error", asString(method), serviceId, cause);
    }

    static String builderMethodReturnedNull(Method method, String serviceId)
    {
        return MESSAGES.format("builder-method-returned-null", asString(method), serviceId);
    }

    static String serviceIsPrivate(String serviceId)
    {
        return MESSAGES.format("service-is-private", serviceId);
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
            if (i > 0)
                buffer.append(", ");

            buffer.append(ids.get(i));
        }

        return MESSAGES.format(
                "many-service-matches",
                serviceInterface.getName(),
                ids.size(),
                buffer.toString());
    }

    static String unknownLifecycle(String name)
    {
        return MESSAGES.format("unknown-lifecycle", name);
    }

    static String decoratorMethodNeedsDelegateParameter(Method method)
    {
        return MESSAGES.format("decorator-method-needs-delegate-parameter", asString(method));
    }

    static String decoratorReturnedWrongType(Method method, String serviceId, Object returned,
            Class serviceInterface)
    {
        return MESSAGES.format(
                "decorator-returned-wrong-type",
                asString(method),
                serviceId,
                returned,
                serviceInterface.getName());
    }

    static String creatingService(String serviceId)
    {
        return MESSAGES.format("creating-service", serviceId);
    }

    static String invokingMethod(Method method)
    {
        return MESSAGES.format("invoking-method", asString(method));
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
        return MESSAGES.format(
                "contribution-wrong-return-type",
                asString(method),
                getJavaClassName(method.getReturnType()));
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

    static String contributionWasNull(String serviceId, ContributionDef def)
    {
        return MESSAGES.format("contribution-was-null", serviceId, def);
    }

    static String contributionKeyWasNull(String serviceId, ContributionDef def)
    {
        return MESSAGES.format("contribution-key-was-null", serviceId, def);
    }

    static String contributionWrongValueType(String serviceId, ContributionDef def,
            Class actualClass, Class expectedClass)
    {
        return MESSAGES.format("contribution-wrong-value-type", serviceId, def, actualClass
                .getName(), expectedClass.getName());
    }

    static String contributionWrongKeyType(String serviceId, ContributionDef def,
            Class actualClass, Class expectedClass)
    {
        return MESSAGES.format(
                "contribution-wrong-key-type",
                serviceId,
                def,
                actualClass.getName(),
                expectedClass.getName());
    }

    static String tooManyConfigurationParameters(Method method)
    {
        return MESSAGES.format("too-many-configuration-parameters", asString(method));
    }

    static String genericTypeNotSupported(Type type)
    {
        return MESSAGES.format("generic-type-not-supported", type);
    }

    static String contributionDuplicateKey(String serviceId, ContributionDef contributionDef,
            ContributionDef existingDef)
    {
        return MESSAGES.format(
                "contribution-duplicate-key",
                serviceId,
                contributionDef,
                existingDef);
    }

    static String errorBuildingService(String serviceId, ServiceDef serviceDef, Throwable cause)
    {
        return MESSAGES.format("error-building-service", serviceId, serviceDef, cause);
    }

    static String noPublicConstructors(String moduleId, Class moduleBuilderClass)
    {
        return MESSAGES.format("no-public-constructors", moduleId, moduleBuilderClass.getName());
    }

    static String tooManyPublicConstructors(String moduleId, Class moduleBuilderClass,
            Constructor constructor)
    {
        return MESSAGES.format("too-many-public-constructors", moduleId, moduleBuilderClass
                .getName(), constructor);
    }

    static String recursiveModuleConstructor(String moduleId, Class builderClass,
            Constructor constructor)
    {
        return MESSAGES.format(
                "recursive-module-constructor",
                moduleId,
                builderClass.getName(),
                constructor);
    }

    static String registryShutdown(String serviceId)
    {
        return MESSAGES.format("registry-shutdown", serviceId);
    }

    static String constructedConfiguration(Collection result)
    {
        return MESSAGES.format("constructed-configuration", result);
    }

    static String constructedConfiguration(Map result)
    {
        return MESSAGES.format("constructed-configuration", result);
    }
}
