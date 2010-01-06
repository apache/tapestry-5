// Copyright 2010 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.PageReset;
import org.apache.tapestry5.internal.structure.PageResetListener;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformMethodSignature;

/**
 * Implementation of the {@link PageReset} annotation. Makes the component implement
 * {@link PageResetListener} and,
 * optionally,
 * 
 * @since 5.2.0
 */
public class PageResetAnnotationWorker implements ComponentClassTransformWorker
{
    private static final String META_KEY = "tapestry.page-reset-listener";

    private static final TransformMethodSignature CONTAINING_PAGE_DID_RESET = new TransformMethodSignature(
            "containingPageDidReset");

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<TransformMethodSignature> methods = transformation
                .findMethodsWithAnnotation(PageReset.class);

        if (methods.isEmpty())
            return;

        String resourcesFieldName = transformation.getResourcesFieldName();

        if (model.getMeta(META_KEY) == null)
        {
            transformation.addImplementedInterface(PageResetListener.class);

            transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                    String.format("%s.addPageResetListener(this);", resourcesFieldName));

            model.setMeta(META_KEY, "true");
        }

        for (TransformMethodSignature sig : methods)
        {
            boolean valid = sig.getParameterTypes().length == 0
                    && sig.getReturnType().equals("void") && sig.getExceptionTypes().length == 0;

            if (!valid)
                throw new RuntimeException(
                        String
                                .format(
                                        "Method %s of class %s is invalid: methods with the @PageReset annotation must return void, and have no parameters or thrown exceptions.",
                                        sig, model.getComponentClassName()));

            transformation.extendMethod(CONTAINING_PAGE_DID_RESET, sig.getMethodName() + "();");
        }

    }
}
