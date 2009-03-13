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

package org.apache.tapestry5.internal.model;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;

class ModelMessages
{
    private final static Messages MESSAGES = MessagesImpl.forClass(ModelMessages.class);

    private ModelMessages()
    {
    }

    static String duplicateParameter(String parameterName, String componentName)
    {
        return MESSAGES.format("duplicate-parameter", parameterName, componentName);
    }

    static String duplicateParameterValue(String parameterName, String componentId, String componentClassName)
    {
        return MESSAGES.format("duplicate-parameter-value", parameterName, componentId, componentClassName);
    }

    static String duplicateComponentId(String id, String componentClassName)
    {
        return MESSAGES.format("duplicate-component-id", id, componentClassName);
    }

    static String missingPersistentField(String fieldName)
    {
        return MESSAGES.format("missing-persistent-field", fieldName);
    }

    static String duplicateMixin(String simpleName, String componentId)
    {
        return MESSAGES.format("duplicate-mixin", simpleName, componentId);
    }
}
