// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel.internal.services;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.tapestry5.beanmodel.PropertyConduit;
import org.apache.tapestry5.beanmodel.PropertyConduit2;
import org.apache.tapestry5.commons.services.TypeCoercer;

public class CoercingPropertyConduitWrapper implements PropertyConduit2
{
    private final PropertyConduit conduit;

    private final TypeCoercer coercer;

    public CoercingPropertyConduitWrapper(final PropertyConduit conduit, final TypeCoercer coercer)
    {
        this.conduit = conduit;
        this.coercer = coercer;
    }

    public Object get(Object instance)
    {
        return conduit.get(instance);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return conduit.getAnnotation(annotationClass);
    }

    public Class getPropertyType()
    {
        return conduit.getPropertyType();
    }
    
    public Type getPropertyGenericType()
    {
        if (conduit instanceof PropertyConduit2) {
            return ((PropertyConduit2) conduit).getPropertyGenericType();
        }
        return conduit.getPropertyType();
    }

    @SuppressWarnings("unchecked")
    public void set(Object instance, Object value)
    {
        Object coerced = coercer.coerce(value, getPropertyType());

        conduit.set(instance, coerced);
    }

}
