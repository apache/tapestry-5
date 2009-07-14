// Copyright 2009 The Apache Software Foundation
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

import java.util.List;

import javassist.Modifier;

import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.apache.tapestry5.services.TransformMethodSignature;


/**
 * Looks for the {@link SessionAttribute} annotation and converts read and write access on such 
 * fields into calls to the {@link Session#getAttribute(String)} and 
 * {@link Session#setAttribute(String, Object)}.
 *
 */
public class SessionAttributeWorker implements ComponentClassTransformWorker
{

    private ObjectLocator objectLocator;

    public SessionAttributeWorker(ObjectLocator objectLocator)
    {
        super();
        this.objectLocator = objectLocator;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsWithAnnotation(SessionAttribute.class);

        for (String fieldName : names)
        {
            SessionAttribute annotation = transformation.getFieldAnnotation(
                    fieldName,
                    SessionAttribute.class);

            String sessionKey = annotation.value();

            if ("".equals(sessionKey))
            {
                sessionKey = fieldName;
            }

            String fieldType = transformation.getFieldType(fieldName);

            Request request = objectLocator.getService(Request.class);

            String requestField = transformation.addInjectedField(
                    Request.class,
                    "_request",
                    request);

            replaceReadAccess(transformation, fieldName, fieldType, sessionKey, requestField);
            replaceWriteAccess(transformation, fieldName, fieldType, sessionKey, requestField);
        }
    }

    private void replaceReadAccess(ClassTransformation transformation, String fieldName,
            String fieldType, String sessionKey, String requestField)
    {
        String readMethodName = transformation.newMemberName("read", fieldName);

        TransformMethodSignature readMethodSignature = new TransformMethodSignature(
                Modifier.PRIVATE, fieldType, readMethodName, null, null);

        String body = String.format(
                "return (%s) %s.getSession(true).getAttribute(\"%s\");",
                fieldType,
                requestField,
                sessionKey);

        transformation.addMethod(readMethodSignature, body);
        transformation.replaceReadAccess(fieldName, readMethodName);
    }

    private void replaceWriteAccess(ClassTransformation transformation, String fieldName,
            String fieldType, String sessionKey, String requestField)
    {
        String writeMethodName = transformation.newMemberName("write", fieldName);

        TransformMethodSignature writeSignature = new TransformMethodSignature(Modifier.PRIVATE,
                "void", writeMethodName, new String[]
                { fieldType }, null);

        String body = String.format(
                "%s.getSession(true).setAttribute(\"%s\", $1);",
                requestField,
                sessionKey);

        transformation.addMethod(writeSignature, body);
        transformation.replaceWriteAccess(fieldName, writeMethodName);

    }

}
