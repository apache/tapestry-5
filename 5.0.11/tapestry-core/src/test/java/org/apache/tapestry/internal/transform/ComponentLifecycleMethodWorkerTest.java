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

package org.apache.tapestry.internal.transform;

import org.apache.tapestry.annotations.SetupRender;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;
import org.apache.tapestry.services.TransformMethodSignature;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

/**
 * Of course, we're committing the cardinal sin of testing the code that's generated, rather than the *behavior* of the
 * generated code. Fortunately, we back all this up with lots and lots of integration testing.
 */
public class ComponentLifecycleMethodWorkerTest extends TapestryTestCase
{
    @Test
    public void no_methods_with_annotation()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        TransformMethodSignature sig = new TransformMethodSignature("someRandomMethod");

        train_findMethods(tf, sig);

        train_getMethodAnnotation(tf, sig, SetupRender.class, null);

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }

    @Test
    public void added_lifecycle_method_is_ignored()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findMethods(tf, TransformConstants.SETUP_RENDER_SIGNATURE);

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }
}
