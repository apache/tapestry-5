// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.services.ClassFabUtils;

class UtilMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(UtilMessages.class);

    private UtilMessages()
    {
    }

    static String dependencyCycle(DependencyNode dependency, DependencyNode node)
    {
        return MESSAGES.format("dependency-cycle", dependency.getId(), node.getId());
    }

    static String duplicateOrderer(String id)
    {
        return MESSAGES.format("duplicate-orderer", id);
    }

    static String constraintFormat(String constraint, String id)
    {
        return MESSAGES.format("constraint-format", constraint, id);
    }

    static String oneShotLock(StackTraceElement element)
    {
        return MESSAGES.format("one-shot-lock", element);
    }

    static String parameterWasNull(String parameterName)
    {
        return MESSAGES.format("parameter-was-null", parameterName);
    }

    static String parameterWasBlank(String parameterName)
    {
        return MESSAGES.format("parameter-was-blank", parameterName);
    }

    static String badCast(String parameterName, Object parameterValue, Class type)
    {
        return MESSAGES.format("bad-cast", parameterName, parameterValue, type.getName());
    }

    static String badMarkerAnnotation(Class annotationClass)
    {
        return MESSAGES.format("bad-marker-annotation", annotationClass.getName());
    }

    static String injectResourceFailure(String fieldName, Class fieldType)
    {
        return MESSAGES.format("inject-resource-failure", fieldName, ClassFabUtils.toJavaClassName(fieldType));
    }
}
