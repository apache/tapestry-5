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
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Obtains a value from the {@link Environment} service based on the field type. This is triggered by the presence of
 * the {@link Environmental} annotation.
 */
public class EnvironmentalWorker implements ComponentClassTransformWorker
{
    private final Environment environment;

    public EnvironmentalWorker(Environment environment)
    {
        this.environment = environment;
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

            String type = transformation.getFieldType(name);

            // TODO: Check for primitives

            // Caching might be good for efficiency at some point.

            String methodName = transformation.newMemberName("environment_read", name);

            TransformMethodSignature sig = new TransformMethodSignature(Modifier.PRIVATE, type, methodName, null,
                                                                        null);

            String body = String.format(
                    "return ($r) %s.%s($type);",
                    envField,
                    annotation.value() ? "peekRequired" : "peek");

            transformation.addMethod(sig, body);

            transformation.replaceReadAccess(name, methodName);
            transformation.makeReadOnly(name);
            transformation.removeField(name);
        }
    }

}
