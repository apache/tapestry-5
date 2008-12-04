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

package org.apache.tapestry5.internal.services;

import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.services.PersistentFieldChange;

public class PersistentFieldChangeImpl implements PersistentFieldChange
{
    private final String componentId;

    private final String fieldName;

    private final Object value;

    public PersistentFieldChangeImpl(final String componentId, final String fieldName,
                                     final Object value)
    {
        notNull(componentId, "componentId");
        notBlank(fieldName, "fieldName");

        this.componentId = componentId;
        this.fieldName = fieldName;
        this.value = value;
    }

    public String getComponentId()
    {
        return componentId;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public Object getValue()
    {
        return value;
    }
}
