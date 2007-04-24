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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.MessagesImpl;

final class BindingsMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(BindingsMessages.class);

    private BindingsMessages()
    {
    }

    static String bindingIsReadOnly(Binding binding)
    {
        return MESSAGES.format("binding-is-read-only", binding);
    }

    static String bindingIsWriteOnly(Binding binding)
    {
        return MESSAGES.format("binding-is-write-only", binding);
    }

    static String noSuchProperty(Class targetClass, String propertyName, String propertyExpression)
    {
        return MESSAGES.format(
                "no-such-property",
                targetClass.getName(),
                propertyName,
                propertyExpression);
    }

    static String writeOnlyProperty(String propertyName, Class clazz, String propertyExpression)
    {
        return MESSAGES.format(
                "write-only-property",
                propertyName,
                clazz.getName(),
                propertyExpression);
    }

    static String validateBindingForFieldsOnly(ComponentResources component)
    {
        return MESSAGES.format("validate-binding-for-fields-only", component.getCompleteId());
    }

    static String methodNotFound(String methodName, Class inClass, String propertyExpression)
    {
        return MESSAGES.format(
                "method-not-found",
                methodName,
                inClass.getName(),
                propertyExpression);
    }

    static String methodIsVoid(String methodName, Class inClass, String propertyExpression)
    {
        return MESSAGES.format("method-is-void", methodName, inClass.getName(), propertyExpression);
    }
}
