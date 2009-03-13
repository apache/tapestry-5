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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.apache.tapestry5.services.PersistentFieldChange;

import java.util.Collection;
import java.util.Map;

public class PersistentFieldBundleImpl implements PersistentFieldBundle
{
    /**
     * Keyed on componentId + fieldName.
     */
    private final Map<String, Object> values = CollectionFactory.newMap();

    public PersistentFieldBundleImpl(Collection<PersistentFieldChange> changes)
    {
        for (PersistentFieldChange change : changes)
        {
            String key = buildKey(change.getComponentId(), change.getFieldName());

            values.put(key, change.getValue());
        }
    }

    private String buildKey(String componentId, String fieldName)
    {
        StringBuilder builder = new StringBuilder();
        if (componentId != null) builder.append(componentId);
        builder.append(':');
        builder.append(fieldName);

        return builder.toString();
    }

    public boolean containsValue(String componentId, String fieldName)
    {
        String key = buildKey(componentId, fieldName);

        return values.containsKey(key);
    }

    public Object getValue(String componentId, String fieldName)
    {
        String key = buildKey(componentId, fieldName);

        return values.get(key);
    }

}
