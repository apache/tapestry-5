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

import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class RenderPhaseMethodWorkerTest extends TapestryTestCase
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

        ComponentClassTransformWorker worker = new RenderPhaseMethodWorker(
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

        ComponentClassTransformWorker worker = new RenderPhaseMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }
}
