// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.beaneditor;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;

import java.util.Collection;

class BeanEditorMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(BeanEditorMessages.class);

    static String duplicatePropertyName(Class beanType, String propertyName)
    {
        return MESSAGES.format("duplicate-property-name", beanType.getName(), propertyName);
    }

    static String unknownProperty(Class beanType, String propertyName,
                                  Collection<String> propertyNames)
    {
        return MESSAGES.format("unknown-property", beanType.getName(), propertyName, InternalUtils
                .joinSorted(propertyNames));
    }

    static String unknownPropertyId(Class beanType, String propertyId, Collection<String> propertyIds)
    {
        return MESSAGES.format("unknown-property-id", beanType.getName(), propertyId,
                               InternalUtils.joinSorted(propertyIds));
    }
}
