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

package org.apache.tapestry5.ioc.internal.services;

import javassist.CtClass;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.services.ThreadCleanupListener;

final class ServiceMessages
{
    private final static Messages MESSAGES = MessagesImpl.forClass(ServiceMessages.class);

    private ServiceMessages()
    {
    }

    static String unableToAddMethod(MethodSignature signature, CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-add-method", signature, ctClass.getName(), cause);
    }

    static String unableToAddConstructor(CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-add-constructor", ctClass.getName(), cause);
    }

    static String unableToAddField(String fieldName, CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-add-field", fieldName, ctClass.getName(), cause);
    }

    static String unableToCreateClass(String className, Class superClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-create-class", className, superClass.getName(), cause);
    }

    static String unableToLookupClass(String className, Throwable cause)
    {
        return MESSAGES.format("unable-to-lookup-class", className, cause);
    }

    static String unableToWriteClass(CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-write-class", ctClass.getName(), cause);
    }

    static String duplicateMethodInClass(MethodSignature ms, ClassFabImpl fab)
    {
        return MESSAGES.format("duplicate-method-in-class", ms, fab.getName());
    }

    static String loggingInterceptor(String serviceId, Class serviceInterface)
    {
        return MESSAGES.format("logging-interceptor", serviceId, serviceInterface.getName());
    }

    static String threadCleanupError(ThreadCleanupListener listener, Throwable cause)
    {
        return MESSAGES.format("thread-cleanup-error", listener, cause);
    }

    static String noSuchProperty(Class clazz, String propertyName)
    {
        return MESSAGES.format("no-such-property", clazz.getName(), propertyName);
    }

    static String readNotSupported(Object instance, String propertyName)
    {
        return MESSAGES.format("read-not-supported", instance.getClass().getName(), propertyName);
    }

    static String writeNotSupported(Object instance, String propertyName)
    {
        return MESSAGES.format("write-not-supported", instance.getClass().getName(), propertyName);
    }

    static String readFailure(String propertyName, Object instance, Throwable cause)
    {
        return MESSAGES.format("read-failure", propertyName, instance, cause);
    }

    static String writeFailure(String propertyName, Object instance, Throwable cause)
    {
        return MESSAGES.format("write-failure", propertyName, instance, cause);
    }

    static String propertyTypeMismatch(String propertyName, Class sourceClass, Class propertyType,
                                       Class expectedType)
    {
        return MESSAGES.format(
                "property-type-mismatch",
                propertyName,
                sourceClass.getName(),
                propertyType.getName(),
                expectedType.getName());
    }

    static String extraFilterMethod(MethodSignature sig, Class filterInterface,
                                    Class serviceInterface)
    {
        return MESSAGES.format(
                "extra-filter-method",
                sig,
                filterInterface.getName(),
                serviceInterface.getName());
    }

    static String unmatchedServiceMethod(MethodSignature sig, Class filterInterface)
    {
        return MESSAGES.format("unmatched-service-method", sig, filterInterface.getName());
    }

    static String unknownObjectProvider(String prefix, String reference)
    {
        return MESSAGES.format("unknown-object-provider", prefix, reference);
    }

    static String shutdownListenerError(Object listener, Throwable cause)
    {
        return MESSAGES.format("shutdown-listener-error", listener, cause);
    }

    static String noCoercionFound(Class sourceType, Class targetType, String coercions)
    {
        return MESSAGES.format(
                "no-coercion-found",
                sourceType.getName(),
                targetType.getName(),
                coercions);
    }

    static String recursiveSymbol(String symbolName, String path)
    {
        return MESSAGES.format("recursive-symbol", symbolName, path);
    }

    static String symbolUndefined(String symbolName)
    {
        return MESSAGES.format("symbol-undefined", symbolName);
    }

    static String symbolUndefinedInPath(String symbolName, String path)
    {
        return MESSAGES.format("symbol-undefined-in-path", symbolName, path);
    }

    static String missingSymbolCloseBrace(String input)
    {
        return MESSAGES.format("missing-symbol-close-brace", input);
    }

    static String missingSymbolCloseBraceInPath(String input, String path)
    {
        return MESSAGES.format("missing-symbol-close-brace-in-path", input, path);
    }

    static String failedCoercion(Object input, Class targetType, Coercion coercion, Throwable cause)
    {
        return MESSAGES.format("failed-coercion", String.valueOf(input), ClassFabUtils
                .toJavaClassName(targetType), coercion, cause);
    }

    static String registryShutdown(String serviceId)
    {
        return MESSAGES.format("registry-shutdown", serviceId);
    }

    static String serviceBuildFailure(String serviceId, Throwable cause)
    {
        return MESSAGES.format("service-build-failure", serviceId, cause);
    }

    static String startupFailure(Throwable cause)
    {
        return MESSAGES.format("startup-failure", cause);
    }
}
