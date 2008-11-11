// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.lang.reflect.Modifier;

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
        for (String fieldName : transformation.findFieldsWithAnnotation(Property.class))
        {
            Property annotation = transformation.getFieldAnnotation(fieldName, Property.class);

            String propertyName = InternalUtils.capitalize(InternalUtils.stripMemberName(fieldName));

            String fieldType = transformation.getFieldType(fieldName);

            if (annotation.read())
            {
                TransformMethodSignature getter
                        = new TransformMethodSignature(Modifier.PUBLIC | Modifier.FINAL, fieldType,
                                                       "get" + propertyName,
                                                       null, null);

                transformation.addTransformedMethod(getter, "return " + fieldName + ";");
            }

            if (annotation.write())
            {
                TransformMethodSignature setter
                        = new TransformMethodSignature(Modifier.PUBLIC | Modifier.FINAL, "void", "set" + propertyName,
                                                       new String[] {fieldType}, null);

                transformation.addTransformedMethod(setter, fieldName + " = $1;");
            }

            // The field is NOT claimed, because we want annotation for the fields to operate.
        }
    }
}
