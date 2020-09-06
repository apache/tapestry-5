// Copyright 2006, 2007, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.commons.internal.services;

import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.internal.util.MessagesImpl;
import org.apache.tapestry5.commons.services.Coercion;
import org.apache.tapestry5.plastic.PlasticUtils;

public class ServiceMessages
{
    private final static Messages MESSAGES = MessagesImpl.forClass(ServiceMessages.class);

    private ServiceMessages()
    {
    }

    public static String noSuchProperty(Class clazz, String propertyName)
    {
        return MESSAGES.format("no-such-property", clazz.getName(), propertyName);
    }


    public static String readFailure(String propertyName, Object instance, Throwable cause)
    {
        return MESSAGES.format("read-failure", propertyName, instance, cause);
    }

    public static String propertyTypeMismatch(String propertyName, Class sourceClass, Class propertyType,
                                              Class expectedType)
    {
        return MESSAGES.format("property-type-mismatch", propertyName, sourceClass.getName(), propertyType.getName(),
                expectedType.getName());
    }

    public static String shutdownListenerError(Object listener, Throwable cause)
    {
        return MESSAGES.format("shutdown-listener-error", listener, cause);
    }

    public static String failedCoercion(Object input, Class targetType, Coercion coercion, Throwable cause)
    {
        return MESSAGES.format("failed-coercion", String.valueOf(input), PlasticUtils.toTypeName(targetType),
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
}
