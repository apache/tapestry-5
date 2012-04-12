// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import java.util.Map;

/**
 * Tracks field read and write instrumentations for a particular class. A field instrumentation is the replacement
 * of direct access to an instance field with invocation of a (synthetic) instance method on the same class. In most cases,
 * the same field will have two instrumentations; one for read, one for write. In some cases, a field will have a single
 * write instrumentation.
 */
class FieldInstrumentations
{
    final String superClassInternalName;

    /**
     * Map field name to a read method.
     */
    final Map<String, FieldInstrumentation> read = PlasticInternalUtils.newMap();

    /**
     * Maps field name to a write method.
     */
    final Map<String, FieldInstrumentation> write = PlasticInternalUtils.newMap();

    FieldInstrumentations(String superClassInternalName)
    {
        this.superClassInternalName = superClassInternalName;
    }

    FieldInstrumentation get(String fieldName, boolean forRead)
    {
        Map<String, FieldInstrumentation> map = forRead ? read : write;

        return map.get(fieldName);
    }
}
