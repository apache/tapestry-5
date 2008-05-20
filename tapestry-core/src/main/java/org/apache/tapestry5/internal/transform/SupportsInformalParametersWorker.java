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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;

/**
 * Checks for the {@link SupportsInformalParameters} annotation, settting the corresponding flag on the model if
 * present.
 */
public class SupportsInformalParametersWorker implements ComponentClassTransformWorker
{

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        if (transformation.getAnnotation(SupportsInformalParameters.class) != null)
            model.enableSupportsInformalParameters();
    }

}
