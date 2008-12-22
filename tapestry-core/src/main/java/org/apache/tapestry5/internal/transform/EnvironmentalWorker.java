// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Obtains a value from the {@link Environment} service based on the field type. This is triggered by the presence of
 * the {@link Environmental} annotation.
 */
public class EnvironmentalWorker implements ComponentClassTransformWorker
{
    private final Environment environment;

    private final ClassLoader classLoader;

    public EnvironmentalWorker(Environment environment, @Builtin ClassFactory servicesLayerClassFactory)
    {
        this.environment = environment;

        classLoader = servicesLayerClassFactory.getClassLoader();
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsWithAnnotation(Environmental.class);

        if (names.isEmpty())
            return;

        // TODO: addInjectField should be smart about if the field has already been injected (with
        // the same type)
        // for this transformation, or the parent transformation.

        String envField = transformation.addInjectedField(
                Environment.class,
                "environment",
                environment);

        for (String name : names)
        {
            Environmental annotation = transformation.getFieldAnnotation(name, Environmental.class);

            transformation.claimField(name, annotation);

            String typeName = transformation.getFieldType(name);

            // TODO: Check for primitives

            // TAP5-417: Calls to javassist.runtime.Desc.getType() are showing up as method hot spots.

            Class type = null;

            try
            {
                type = classLoader.loadClass(typeName);
            }
            catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }

            // TAP5-417: Changed the code to use EnvironmentalAccess, which encapsulates
            // efficient caching.

            String injectedTypeFieldName = transformation.addInjectedField(Class.class, "type", type);

            // First we need (at page attach) to acquire the closure for the type.

            String accessFieldName = transformation.addField(Modifier.PRIVATE, EnvironmentalAccess.class.getName(),
                                                             name + "_access");

            String attachBody = String.format("%s = %s.getAccess(%s);",
                                              accessFieldName, envField, injectedTypeFieldName);

            transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_ATTACH_SIGNATURE, attachBody);

            // Clear the closure field when the page detaches.  We'll get a new one when we next attach.

            transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE,
                                        accessFieldName + " = null;");

            // Now build a read method that invokes peek() or peekRequired() on the closure. The closure
            // is responsible for safe caching of the environmental value.

            String methodName = transformation.newMemberName("environment_read", name);

            TransformMethodSignature sig = new TransformMethodSignature(Modifier.PRIVATE, typeName, methodName, null,
                                                                        null);

            String body = String.format(
                    "return ($r) %s.%s();",
                    accessFieldName,
                    annotation.value() ? "peekRequired" : "peek");

            transformation.addMethod(sig, body);

            transformation.replaceReadAccess(name, methodName);
            transformation.makeReadOnly(name);
            transformation.removeField(name);
        }
    }
}
