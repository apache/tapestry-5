// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

import java.lang.reflect.Modifier;

/**
 * Converts fields with the {@link org.apache.tapestry5.annotations.Persist} annotation into persistent fields.
 */
public class PersistWorker implements ComponentClassTransformWorker
{

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String name : transformation.findFieldsWithAnnotation(Persist.class))
        {
            makeFieldPersistent(name, transformation, model);
        }
    }

    /**
     * Making a field persistent: <ul> <li>Need a secondary default field that stores the initial value</li> <li>Store
     * the active value into the default field when the page finishes loading</li> <li>Roll the active value back to the
     * default when the page detaches</li> <ii>On changes to the active field, post the change via the {@link
     * org.apache.tapestry5.internal.InternalComponentResources} </li> <li>When the page attaches, pull the persisted
     * value for the field out of the {@link org.apache.tapestry5.services.PersistentFieldBundle}</li> </ul>
     *
     * @see org.apache.tapestry5.runtime.PageLifecycleListener#restoreStateBeforePageAttach()
     */
    private void makeFieldPersistent(String fieldName, ClassTransformation transformation,
                                     MutableComponentModel model)
    {
        String fieldType = transformation.getFieldType(fieldName);
        Persist annotation = transformation.getFieldAnnotation(fieldName, Persist.class);

        transformation.claimField(fieldName, annotation);

        // Record the type of persistence, until needed later.

        String logicalFieldName = model.setFieldPersistenceStrategy(fieldName, annotation.value());

        String defaultValue = TransformUtils.getDefaultValue(fieldType);

        // Force the field back to its default value (null, 0, false) at the end of each request.

        transformation.extendMethod(
                TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE,
                String.format("%s = %s;", fieldName, defaultValue));

        String resourcesFieldName = transformation.getResourcesFieldName();

        String writeMethodName = transformation.newMemberName("write", fieldName);

        BodyBuilder builder = new BodyBuilder();

        builder.begin();
        builder.addln(
                "%s.persistFieldChange(\"%s\", ($w) $1);",
                resourcesFieldName,
                logicalFieldName);
        builder.addln("%s = $1;", fieldName);
        builder.end();

        transformation.addMethod(new TransformMethodSignature(Modifier.PRIVATE, "void", writeMethodName,
                                                              new String[]
                                                                      { fieldType }, null), builder.toString());

        transformation.replaceWriteAccess(fieldName, writeMethodName);

        builder.clear();
        builder.begin();

        // Check to see if there's a recorded change for this component, this field.

        builder.addln("if (%s.hasFieldChange(\"%s\"))", resourcesFieldName, logicalFieldName);

        String wrapperType = TransformUtils.getWrapperTypeName(fieldType);

        // Get the value, cast it to the correct type (or wrapper type)
        builder.add(
                "  %s = ((%s) %s.getFieldChange(\"%s\"))",
                fieldName,
                wrapperType,
                resourcesFieldName,
                logicalFieldName);

        // For primtive types, add in the method call to unwrap the wrapper type to a primitive type

        String unwrapMethodName = TransformUtils.getUnwrapperMethodName(fieldType);

        if (unwrapMethodName == null)
            builder.addln(";");
        else
            builder.addln(".%s();", unwrapMethodName);

        builder.end();

        transformation.extendMethod(
                TransformConstants.RESTORE_STATE_BEFORE_PAGE_ATTACH_SIGNATURE,
                builder.toString());
    }
}
