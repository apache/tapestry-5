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

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.ioc.services.ClassFabUtils;

public final class InternalMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(InternalMessages.class);

    public static String componentActionNotSerializable(String componentId, Throwable cause)
    {
        return MESSAGES.format("component-action-not-serializable", componentId, cause);
    }

    public static String encloseErrorsInForm()
    {
        return MESSAGES.get("enclose-errors-in-form");
    }

    public static String failureInstantiatingObject(Class objectType, String componentId, Throwable cause)
    {
        return MESSAGES.format("failure-instantitating-object", ClassFabUtils
                .toJavaClassName(objectType), componentId, cause);
    }

    public static String conflictingEncodingType(String existing, String conflicting)
    {
        return MESSAGES.format("conflicting-encoding-type", existing, conflicting);
    }

    public static String toClientShouldReturnString()
    {
        return MESSAGES.format("to-client-should-return-string");
    }

    public static String formFieldOutsideForm(String fieldName)
    {
        return MESSAGES.format("form-field-outside-form", fieldName);
    }
}
