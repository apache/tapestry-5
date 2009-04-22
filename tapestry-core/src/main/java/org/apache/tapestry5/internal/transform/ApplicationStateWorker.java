// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformMethodSignature;

import static java.lang.String.format;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Looks for the {@link ApplicationState} and {@link org.apache.tapestry5.annotations.SessionState} annotations and
 * converts read and write access on such fields into calls to the {@link ApplicationStateManager}.
 */
public class ApplicationStateWorker implements ComponentClassTransformWorker
{
    private final ApplicationStateManager applicationStateManager;

    private final ComponentClassCache componentClassCache;

    public ApplicationStateWorker(ApplicationStateManager applicationStateManager,
                                  ComponentClassCache componentClassCache)
    {
        this.applicationStateManager = applicationStateManager;
        this.componentClassCache = componentClassCache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        Map<String, Boolean> fields = CollectionFactory.newMap();

        List<String> asoNames = transformation.findFieldsWithAnnotation(ApplicationState.class);

        for (String name : asoNames)
        {
            ApplicationState applicationState = transformation.getFieldAnnotation(name, ApplicationState.class);

            fields.put(name, applicationState.create());
        }


        List<String> ssoNames = transformation.findFieldsWithAnnotation(SessionState.class);

        for (String name : ssoNames)
        {
            SessionState sessionState = transformation.getFieldAnnotation(name, SessionState.class);

            fields.put(name, sessionState.create());
        }


        if (fields.isEmpty()) return;

        String managerFieldName = transformation.addInjectedField(ApplicationStateManager.class,
                                                                  "applicationStateManager", applicationStateManager);

        for (String fieldName : InternalUtils.sortedKeys(fields))
        {
            processField(fieldName, managerFieldName, transformation, fields.get(fieldName));
        }
    }

    private void processField(String fieldName, String managerFieldName, ClassTransformation transformation,
                              boolean create)
    {
        String fieldType = transformation.getFieldType(fieldName);

        Class fieldClass = componentClassCache.forName(fieldType);

        String typeFieldName = transformation.addInjectedField(Class.class, fieldName + "_type", fieldClass);

        replaceRead(transformation, fieldName, fieldType, managerFieldName, typeFieldName, create);

        replaceWrite(transformation, fieldName, fieldType, managerFieldName, typeFieldName);

        transformation.removeField(fieldName);

        String booleanFieldName = fieldName + "Exists";

        if (transformation.isField(booleanFieldName) && transformation.getFieldType(booleanFieldName).equals("boolean"))
        {
            replaceFlagRead(transformation, booleanFieldName, typeFieldName, managerFieldName);
        }
    }

    private void replaceFlagRead(ClassTransformation transformation, String booleanFieldName, String typeFieldName,
                                 String managerFieldName)
    {
        String readMethodName = transformation.newMemberName("read", booleanFieldName);

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PRIVATE, "boolean", readMethodName, null,
                                                                    null);

        String body = format("return %s.exists(%s);", managerFieldName, typeFieldName);

        transformation.addMethod(sig, body);

        transformation.replaceReadAccess(booleanFieldName, readMethodName);
        transformation.makeReadOnly(booleanFieldName);
        transformation.removeField(booleanFieldName);
    }

    private void replaceWrite(ClassTransformation transformation, String fieldName, String fieldType,
                              String managerFieldName, String typeFieldName)
    {
        String writeMethodName = transformation.newMemberName("write", fieldName);

        TransformMethodSignature writeSignature = new TransformMethodSignature(Modifier.PRIVATE, "void",
                                                                               writeMethodName,
                                                                               new String[] { fieldType },
                                                                               null);

        String body = format("%s.set(%s, $1);", managerFieldName, typeFieldName);

        transformation.addMethod(writeSignature, body);

        transformation.replaceWriteAccess(fieldName, writeMethodName);
    }

    private void replaceRead(ClassTransformation transformation, String fieldName, String fieldType,
                             String managerFieldName, String typeFieldName, boolean create)
    {
        String readMethodName = transformation.newMemberName("read", fieldName);

        TransformMethodSignature readMethodSignature = new TransformMethodSignature(Modifier.PRIVATE, fieldType,
                                                                                    readMethodName, null, null);

        String methodName = create ? "get" : "getIfExists";

        String body = format("return (%s) %s.%s(%s);", fieldType, managerFieldName, methodName, typeFieldName);

        transformation.addMethod(readMethodSignature, body);

        transformation.replaceReadAccess(fieldName, readMethodName);
    }
}
