// Copyright 2008, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PropertyAccessType;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Provides the getter and setter methods. The methods are added as "existing", meaning that field access to them will
 * be transformed as necessary by other annotations. This worker needs to be scheduled before any worker that might
 * delete a field.
 * 
 * @see org.apache.tapestry5.annotations.Property
 */
public class PropertyWorker implements ComponentClassTransformWorker2
{

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(Property.class))
        {
            createAccessorsForField(field);
        }
    }

    private void createAccessorsForField(PlasticField field)
    {
        PropertyAccessType accessType = toType(field);

        field.createAccessors(accessType);
    }

    private PropertyAccessType toType(PlasticField field)
    {
        Property annotation = field.getAnnotation(Property.class);

        boolean read = annotation.read();
        boolean write = annotation.write();

        if (read && write)
            return PropertyAccessType.READ_WRITE;

        if (read)
            return PropertyAccessType.READ_ONLY;

        if (write)
            return PropertyAccessType.WRITE_ONLY;

        throw new IllegalArgumentException(String.format(
                "@Property annotation on %s.%s should have either read() or write() enabled.", field.getPlasticClass()
                        .getClassName(), field.getName()));
    }
}
