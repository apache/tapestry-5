// Copyright 2006, 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
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

public class ServiceMessages
{
    private final static Messages MESSAGES = MessagesImpl.forClass(ServiceMessages.class);

    private ServiceMessages()
    {
    }

    public static String unableToAddMethod(MethodSignature signature, CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-add-method", signature, ctClass.getName(), cause);
    }

    public static String unableToAddConstructor(CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-add-constructor", ctClass.getName(), cause);
    }

    public static String unableToAddField(String fieldName, CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-add-field", fieldName, ctClass.getName(), cause);
    }

    public static String unableToCreateClass(String className, Class superClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-create-class", className, superClass.getName(), cause);
    }

    public static String unableToLookupClass(String className, Throwable cause)
    {
        return MESSAGES.format("unable-to-lookup-class", className, cause);
    }

    public static String unableToWriteClass(CtClass ctClass, Throwable cause)
    {
        return MESSAGES.format("unable-to-write-class", ctClass.getName(), cause);
    }

    public static String duplicateMethodInClass(MethodSignature ms, ClassFabImpl fab)
    {
        return MESSAGES.format("duplicate-method-in-class", ms, fab.getName());
    }

    public static String loggingInterceptor(String serviceId, Class serviceInterface)
    {
        return MESSAGES.format("logging-interceptor", serviceId, serviceInterface.getName());
    }

    public static String threadCleanupError(ThreadCleanupListener listener, Throwable cause)
    {
        return MESSAGES.format("thread-cleanup-error", listener, cause);
    }

    public static String noSuchProperty(Class clazz, String propertyName)
    {
        return MESSAGES.format("no-such-property", clazz.getName(), propertyName);
    }

    public static String readNotSupported(Object instance, String propertyName)
    {
        return MESSAGES.format("read-not-supported", instance.getClass().getName(), propertyName);
    }

    public static String writeNotSupported(Object instance, String propertyName)
    {
        return MESSAGES.format("write-not-supported", instance.getClass().getName(), propertyName);
    }

    public static String readFailure(String propertyName, Object instance, Throwable cause)
    {
        return MESSAGES.format("read-failure", propertyName, instance, cause);
    }

    public static String writeFailure(String propertyName, Object instance, Throwable cause)
    {
        return MESSAGES.format("write-failure", propertyName, instance, cause);
    }

    public static String propertyTypeMismatch(String propertyName, Class sourceClass, Class propertyType,
            Class expectedType)
    {
        return MESSAGES.format("property-type-mismatch", propertyName, sourceClass.getName(), propertyType.getName(),
                expectedType.getName());
    }

    public static String extraFilterMethod(MethodSignature sig, Class filterInterface, Class serviceInterface)
    {
        return MESSAGES.format("extra-filter-method", sig, filterInterface.getName(), serviceInterface.getName());
    }

    public static String unmatchedServiceMethod(MethodSignature sig, Class filterInterface)
    {
        return MESSAGES.format("unmatched-service-method", sig, filterInterface.getName());
    }

    public static String unknownObjectProvider(String prefix, String reference)
    {
        return MESSAGES.format("unknown-object-provider", prefix, reference);
    }

    public static String shutdownListenerError(Object listener, Throwable cause)
    {
        return MESSAGES.format("shutdown-listener-error", listener, cause);
    }

    public static String noCoercionFound(Class sourceType, Class targetType, String coercions)
    {
        return MESSAGES.format("no-coercion-found", sourceType.getName(), targetType.getName(), coercions);
    }

    public static String recursiveSymbol(String symbolName, String path)
    {
        return MESSAGES.format("recursive-symbol", symbolName, path);
    }

    public static String symbolUndefined(String symbolName)
    {
        return MESSAGES.format("symbol-undefined", symbolName);
    }

    public static String symbolUndefinedInPath(String symbolName, String path)
    {
        return MESSAGES.format("symbol-undefined-in-path", symbolName, path);
    }

    public static String missingSymbolCloseBrace(String input)
    {
        return MESSAGES.format("missing-symbol-close-brace", input);
    }

    public static String missingSymbolCloseBraceInPath(String input, String path)
    {
        return MESSAGES.format("missing-symbol-close-brace-in-path", input, path);
    }

    public static String failedCoercion(Object input, Class targetType, Coercion coercion, Throwable cause)
    {
        return MESSAGES.format("failed-coercion", String.valueOf(input), ClassFabUtils.toJavaClassName(targetType),
                coercion, cause);
    }

    public static String registryShutdown(String serviceId)
    {
        return MESSAGES.format("registry-shutdown", serviceId);
    }

    public static String serviceBuildFailure(String serviceId, Throwable cause)
    {
        return MESSAGES.format("service-build-failure", serviceId, cause);
    }

    public static String startupFailure(Throwable cause)
    {
        return MESSAGES.format("startup-failure", cause);
    }
}
