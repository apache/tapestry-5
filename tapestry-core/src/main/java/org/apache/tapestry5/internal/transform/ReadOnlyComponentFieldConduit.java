// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.InstanceContext;

/**
 * An implementation of {@link FieldConduit} for a read-only component field. Subclasses
 * provide an implementation of the {@link #get(Object, org.apache.tapestry5.plastic.InstanceContext)} method.
 *
 * @since 5.2.0
 */
public abstract class ReadOnlyComponentFieldConduit implements FieldConduit<Object>
{
    private final String qualifiedFieldName;

    public ReadOnlyComponentFieldConduit(String qualifiedFieldName)
    {
        this.qualifiedFieldName = qualifiedFieldName;
    }

    public ReadOnlyComponentFieldConduit(String className, String fieldName)
    {
        this(String.format("%s.%s", className, fieldName));
    }

    public ReadOnlyComponentFieldConduit(ComponentResources resources, String fieldName)
    {
        this(resources.getComponentModel().getComponentClassName(), fieldName);
    }

    public void set(Object instance, InstanceContext context, Object newValue)
    {
        throw new RuntimeException(String.format("Field %s is read only.", qualifiedFieldName));
    }
}
