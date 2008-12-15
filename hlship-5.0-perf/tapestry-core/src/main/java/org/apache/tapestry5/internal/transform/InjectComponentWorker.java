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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformConstants;

/**
 * Recognizes the {@link org.apache.tapestry5.annotations.InjectComponent} annotation, and converts the field into a
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
                componentId = InternalUtils.stripMemberName(fieldName);

            transformation.makeReadOnly(fieldName);

            BodyBuilder builder = new BodyBuilder().addln("try").begin();

            builder.addln(
                    "%s = (%s) %s.getEmbeddedComponent(\"%s\");",
                    fieldName,
                    type,
                    resourcesFieldName,
                    componentId);
            builder.end();
            builder.addln("catch (ClassCastException ex)").begin();
            builder.addln("throw new RuntimeException(%s.formatMessage(%s, \"%s\", \"%s\", \"%s\"), ex);",
                          getClass().getName(), resourcesFieldName, fieldName, type, componentId);
            builder.end();

            transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, builder.toString());
        }
    }

    public static String formatMessage(ComponentResources resources, String fieldName, String fieldType,
                                       String componentId)
    {
        return String.format(
                "Unable to inject component '%s' into field %s of component %s.  Class %s is not assignable to a field of type %s.",
                componentId, fieldName, resources.getCompleteId(),
                resources.getEmbeddedComponent(componentId).getClass().getName(), fieldType);
    }
}
