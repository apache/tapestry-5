// Copyright 2008, 2010 The Apache Software Foundation
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

import java.lang.reflect.Modifier;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.FieldAccess;
import org.apache.tapestry5.services.TransformField;
import org.apache.tapestry5.services.TransformMethodSignature;

/**
 * Provides the getter and setter methods. The methods are added as "existing", meaning that field access to them will
 * be transformed as necessary by other annotations. This worker needs to be scheduled before any worker that might
 * delete a field.
 * 
 * @see org.apache.tapestry5.annotations.Property
 */
public class PropertyWorker implements ComponentClassTransformWorker
{
    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchFieldsWithAnnotation(Property.class))
        {
            createAccessorsForField(transformation, field);
        }
    }

    private void createAccessorsForField(ClassTransformation transformation, TransformField field)
    {
        Property annotation = field.getAnnotation(Property.class);

        String propertyName = InternalUtils.capitalize(InternalUtils.stripMemberName(field.getName()));

        if (annotation.read())
            addGetter(transformation, field, propertyName);

        if (annotation.write())
            addSetter(transformation, field, propertyName);
    }

    private void addSetter(ClassTransformation transformation, TransformField field, String propertyName)
    {
        TransformMethodSignature setter = new TransformMethodSignature(Modifier.PUBLIC, "void", "set" + propertyName,
                new String[]
                { field.getType() }, null);

        final FieldAccess access = field.getAccess();

        transformation.createMethod(setter).addAdvice(new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                access.write(invocation.getInstance(), invocation.getParameter(0));
            }
        });
    }

    private void addGetter(ClassTransformation transformation, TransformField field, String propertyName)
    {
        TransformMethodSignature getter = new TransformMethodSignature(Modifier.PUBLIC, field.getType(), "get"
                + propertyName, null, null);

        final FieldAccess access = field.getAccess();

        transformation.createMethod(getter).addAdvice(new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.overrideResult(access.read(invocation.getInstance()));
            }
        });
    }
}
