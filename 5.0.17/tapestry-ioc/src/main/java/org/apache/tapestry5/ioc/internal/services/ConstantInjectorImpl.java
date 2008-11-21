//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.util.BodyBuilder;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class ConstantInjectorImpl implements ConstantInjector
{
    private final ClassFab classFab;

    private final IdAllocator allocator = new IdAllocator("_");

    private class InjectedValue
    {
        final Class type;
        final Object value;
        final String fieldName;

        private InjectedValue(Class type, Object value, String fieldName)
        {
            this.type = type;
            this.value = value;
            this.fieldName = fieldName;
        }
    }

    private final List<InjectedValue> injections = CollectionFactory.newList();

    private final Map<Object, String> instanceToFieldName = CollectionFactory.newMap();

    public ConstantInjectorImpl(ClassFab classFab)
    {
        this.classFab = classFab;
    }

    public <T> String inject(Class<T> type, T instance)
    {
        String fieldName = instanceToFieldName.get(instance);

        if (fieldName == null)
        {
            fieldName = allocator.allocateId(type.getSimpleName().toLowerCase());

            injections.add(new InjectedValue(type, instance, fieldName));

            classFab.addField(fieldName, Modifier.PRIVATE | Modifier.FINAL, type);

            instanceToFieldName.put(instance, fieldName);
        }

        return fieldName;
    }

    void implementConstructor()
    {
        int count = injections.size();

        Class[] types = new Class[count];

        BodyBuilder builder = new BodyBuilder().begin();

        for (int i = 0; i < count; i++)
        {
            InjectedValue injected = injections.get(i);

            types[i] = injected.type;

            builder.addln("%s = $%d;", injected.fieldName, i + 1);
        }

        builder.end();

        classFab.addConstructor(types, null, builder.toString());
    }

    public Object[] getParameters()
    {
        int count = injections.size();

        Object[] values = new Object[count];

        for (int i = 0; i < count; i++)
        {
            InjectedValue injected = injections.get(i);

            values[i] = injected.value;
        }

        return values;
    }
}
