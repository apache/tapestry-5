// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import java.util.List;

import org.apache.tapestry.annotations.InjectComponent;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;

/**
 * Identifies the {@link InjectComponent} annotation and adds code to initialize it to the core
 * component.
 */
public class InjectComponentWorker implements ComponentClassTransformWorker
{

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsWithAnnotation(InjectComponent.class);

        if (names.isEmpty())
            return;

        // I can't imagine a scenario where a component would have more than one
        // field with InjectComponent, but that's the way these APIs work, lists of names.

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("%s core = %s.getCoreComponent();", Component.class.getName(), transformation
                .getResourcesFieldName());

        for (String fieldName : names)
        {
            InjectComponent annotation = transformation.getFieldAnnotation(
                    fieldName,
                    InjectComponent.class);

            String fieldType = transformation.getFieldType(fieldName);

            builder.addln("try");
            builder.begin();
            builder.addln("%s = (%s) core;", fieldName, fieldType);
            builder.end();
            builder.addln("catch (ClassCastException ex)");
            builder.begin();
            builder.addln(
                    "String message = %s.buildCastExceptionMessage(core, \"%s.%s\", \"%s\");",
                    InjectComponentWorker.class.getName(),
                    model.getComponentClassName(),
                    fieldName,
                    fieldType);
            builder.addln("throw new RuntimeException(message, ex);");
            builder.end();

            transformation.makeReadOnly(fieldName);
            transformation.claimField(fieldName, annotation);
        }

        builder.end();

        transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, builder
                .toString());
    }

    public static String buildCastExceptionMessage(Component component, String fieldName,
            String fieldType)
    {
        return ServicesMessages.componentNotAssignableToField(component, fieldName, fieldType);
    }
}
