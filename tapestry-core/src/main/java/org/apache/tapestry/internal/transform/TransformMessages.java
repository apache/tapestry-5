// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.transform;

import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.MessagesImpl;
import org.apache.tapestry.runtime.Component;

class TransformMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(TransformMessages.class);

    static String fieldInjectionError(String className, String fieldName, Throwable cause)
    {
        return MESSAGES.format("field-injection-error", className, fieldName, cause);
    }

    static String componentNotAssignableToField(Component component, String fieldName, String fieldType)
    {
        return MESSAGES.format("component-not-assignable-to-field", component
                .getComponentResources().getCompleteId(), fieldName, fieldType);
    }

}
