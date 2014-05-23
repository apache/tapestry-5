// Copyright 2011 The Apache Software Foundation
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class AnnotationBuilder extends AbstractAnnotationBuilder
{
    private static final class AnnotationValueHandler implements InvocationHandler
    {
        private final Class annotationType;

        private final Map<String, Object> attributes;

        public AnnotationValueHandler(final Class annotationType, Map<String, Object> attributes)
        {
            this.annotationType = annotationType;
            this.attributes = attributes;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            // args is null for no-arguments methods
            if (args == null)
            {
                String attributeName = method.getName();

                if (attributes.containsKey(attributeName)) { return attributes.get(attributeName); }
            }

            // TODO: Handling of equals() and hashCode() and toString(), plus other methods
            // inherited from Object

            throw new RuntimeException(String.format("Annotation proxy for class %s does not handle method %s.",
                    annotationType.getName(), method));
        }
    }

    private final Class annotationType;

    final Map<String, Object> attributes = PlasticInternalUtils.newMap();

    public AnnotationBuilder(Class annotationType, PlasticClassPool pool)
    {
        super(pool);

        this.annotationType = annotationType;

        attributes.put("annotationType", annotationType);

        // Annotation attributes are represented as methods, and for each method there may be a
        // default value. Preload the default values, which may be overwritten by explicit
        // values.

        for (Method m : annotationType.getMethods())
        {
            Object defaultValue = m.getDefaultValue();

            if (defaultValue != null)
            {
                attributes.put(m.getName(), defaultValue);
            }
        }

        if (!attributes.containsKey("toString"))
        {
            attributes.put("toString", "@" + annotationType.getName());
        }

    }

    @Override
    protected void store(String name, Object value)
    {
        attributes.put(name, value);
    }

    @Override
    protected Class elementTypeForArrayAttribute(String name)
    {
        try
        {
            return annotationType.getMethod(name).getReturnType().getComponentType();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(String.format(
                    "Unable to determine element type for attribute '%s' of annotation %s: %s", name,
                    annotationType.getName(), PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    public Object createAnnotation()
    {
        // Use a static inner class to keep the AnnotationBuilder from being retained

        InvocationHandler handler = new AnnotationValueHandler(annotationType, attributes);

        try
        {
            return Proxy.newProxyInstance(pool.loader, new Class[]
            { annotationType }, handler);
        }
        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException(String.format("Unable to create instance of annotation type %s: %s",
                    annotationType.getName(), PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

}
