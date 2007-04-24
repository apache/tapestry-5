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

package org.apache.tapestry.internal.services;

import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ApplicationStateManager;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.MethodSignature;

/**
 * Looks for the {@link ApplicationState} annotation and converts read and write access on such
 * fields into calls to the {@link ApplicationStateManager}.
 */
public class ApplicationStateWorker implements ComponentClassTransformWorker
{
    private final ApplicationStateManager _applicationStateManager;

    private final ClassLoader _classLoader = Thread.currentThread().getContextClassLoader();

    public ApplicationStateWorker(ApplicationStateManager applicationStateManager)
    {
        _applicationStateManager = applicationStateManager;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsWithAnnotation(ApplicationState.class);

        if (names.isEmpty())
            return;

        String managerFieldName = transformation.addInjectedField(
                ApplicationStateManager.class,
                "applicationStateManager",
                _applicationStateManager);

        for (String fieldName : names)
        {
            String fieldType = transformation.getFieldType(fieldName);

            Class fieldClass = null;

            try
            {
                fieldClass = _classLoader.loadClass(fieldType);
            }
            catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(ex);

            }

            String typeField = transformation.addInjectedField(
                    Class.class,
                    fieldName + "_type",
                    fieldClass);

            replaceRead(transformation, fieldName, fieldType, managerFieldName, typeField);

            replaceWrite(transformation, fieldName, fieldType, managerFieldName, typeField);

            transformation.removeField(fieldName);
        }
    }

    private void replaceWrite(ClassTransformation transformation, String fieldName,
            String fieldType, String managerFieldName, String typeField)
    {
        String writeMethodName = transformation.newMemberName("write", fieldName);

        MethodSignature writeSignature = new MethodSignature(Modifier.PRIVATE, "void",
                writeMethodName, new String[]
                { fieldType }, null);

        String body = String.format("%s.set(%s, $1);", managerFieldName, typeField);

        transformation.addMethod(writeSignature, body);

        transformation.replaceWriteAccess(fieldName, writeMethodName);
    }

    private void replaceRead(ClassTransformation transformation, String fieldName,
            String fieldType, String managerFieldName, String typeField)
    {

        String readMethodName = transformation.newMemberName("read", fieldName);

        MethodSignature readMethodSignature = new MethodSignature(Modifier.PRIVATE, fieldType,
                readMethodName, null, null);

        String body = String.format(
                "return (%s) %s.get(%s);",
                fieldType,
                managerFieldName,
                typeField);

        transformation.addMethod(readMethodSignature, body);

        transformation.replaceReadAccess(fieldName, readMethodName);
    }
}
