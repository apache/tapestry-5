// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Provides the page activation context handlers.  This worker must be scheduled before {@link
 * org.apache.tapestry5.internal.transform.OnEventWorker} in order for the added event handler methods to be properly
 * picked up and processed.
 *
 * @see org.apache.tapestry5.annotations.PageActivationContext
 */
public class PageActivationContextWorker implements ComponentClassTransformWorker
{
    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fields = transformation.findFieldsWithAnnotation(PageActivationContext.class);

        if (fields.size() > 1)
            throw new RuntimeException(TransformMessages.illegalNumberOfPageActivationContextHandlers(fields));

        for (String fieldName : fields)
        {
            PageActivationContext annotation = transformation.getFieldAnnotation(fieldName,
                                                                                 PageActivationContext.class);

            String fieldType = transformation.getFieldType(fieldName);

            if (annotation.activate())
            {
                TransformMethodSignature activate
                        = new TransformMethodSignature(Modifier.PROTECTED | Modifier.FINAL, "void",
                                                       "onActivate",
                                                       new String[] { fieldType }, null);
                transformation.addTransformedMethod(activate, fieldName + " = $1;");
            }

            if (annotation.passivate())
            {
                TransformMethodSignature passivate
                        = new TransformMethodSignature(Modifier.PROTECTED | Modifier.FINAL, "java.lang.Object",
                                                       "onPassivate",
                                                       null, null);
                transformation.addTransformedMethod(passivate, "return ($w) " + fieldName + ";");
            }
        }

    }
}
