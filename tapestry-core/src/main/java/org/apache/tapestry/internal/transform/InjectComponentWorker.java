// Copyright  2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.transform;

import org.apache.tapestry.annotations.InjectComponent;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;

/**
 * Recognizes the {@link org.apache.tapestry.annotations.InjectComponent} annotation, and converts the field into a
 * read-only field containing the component.  The id of the component may be explicitly stated or will be determined
 * from the field name.
 */
public class InjectComponentWorker implements ComponentClassTransformWorker
{

    public void transform(ClassTransformation transformation,
                          MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(InjectComponent.class))
        {
            InjectComponent annotation = transformation.getFieldAnnotation(fieldName, InjectComponent.class);

            String type = transformation.getFieldType(fieldName);

            String resourcesFieldName = transformation.getResourcesFieldName();

            String componentId = annotation.value();
            if (InternalUtils.isBlank(componentId))
                componentId = InternalUtils.stripMemberPrefix(fieldName);

            transformation.makeReadOnly(fieldName);

            String body = String.format(
                    "%s = (%s) %s.getEmbeddedComponent(\"%s\");",
                    fieldName,
                    type,
                    resourcesFieldName,
                    componentId);

            transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, body);
        }

    }
}
