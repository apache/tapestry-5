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

import org.apache.tapestry5.internal.plastic.asm.AnnotationVisitor;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.Type;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings(
{ "rawtypes", "unchecked" })
public abstract class AbstractAnnotationBuilder extends AnnotationVisitor
{
    protected final PlasticClassPool pool;

    public AbstractAnnotationBuilder(PlasticClassPool pool)
    {
        super(Opcodes.ASM4);

        this.pool = pool;
    }

    protected abstract void store(String name, Object value);

    protected Class elementTypeForArrayAttribute(String name)
    {
        throw new IllegalStateException("elementTypeForArrayAttribute() may not be invoked here.");
    }

    @Override
    public void visit(String name, Object value)
    {
        if (value instanceof Type)
        {
            Type type = (Type) value;

            Class valueType = pool.loadClass(type.getClassName());
            store(name, valueType);
            return;
        }

        store(name, value);
    }

    @Override
    public void visitEnum(String name, String desc, String value)
    {

        try
        {
            String enumClassName = PlasticInternalUtils.objectDescriptorToClassName(desc);

            Class enumClass = pool.loader.loadClass(enumClassName);

            Object enumValue = Enum.valueOf(enumClass, value);

            store(name, enumValue);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(String.format("Unable to convert enum annotation attribute %s %s: %s",
                    value, desc, PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, String desc)
    {
        final AbstractAnnotationBuilder outerBuilder = this;

        final Class nestedAnnotationType = pool.loadClass(PlasticInternalUtils.objectDescriptorToClassName(desc));

        // Return a nested builder that constructs the inner annotation and, at the end of
        // construction, pushes the final Annotation object into this builder's attributes.

        return new AnnotationBuilder(nestedAnnotationType, pool)
        {
            @Override
            public void visitEnd()
            {
                outerBuilder.store(name, createAnnotation());
            };
        };
    }

    @Override
    public AnnotationVisitor visitArray(final String name)
    {
        final List<Object> values = new ArrayList<Object>();

        final Class componentType = elementTypeForArrayAttribute(name);

        final AbstractAnnotationBuilder outerBuilder = this;

        return new AbstractAnnotationBuilder(pool)
        {
            @Override
            protected void store(String name, Object value)
            {
                values.add(value);
            }

            @Override
            public void visitEnd()
            {
                Object array = Array.newInstance(componentType, values.size());

                // Now, empty arrays may be primitive types and will not cast to Object[], but
                // non empty arrays indicate that it was a Class/Enum/Annotation, which can cast
                // to Object[]

                if (values.size() != 0)
                {
                    for (int i = 0; i<values.size(); i++)
                    {
                        Array.set(array, i, values.get(i));
                    }
                }
                outerBuilder.store(name, array);
            }
        };
    }

    @Override
    public void visitEnd()
    {
        // Nothing to do here. Subclasses use this as a chance to store a value into an outer
        // builder.
    }

}
