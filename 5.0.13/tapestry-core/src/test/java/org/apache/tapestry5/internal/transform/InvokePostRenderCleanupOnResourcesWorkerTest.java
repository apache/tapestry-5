// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformConstants;
import org.testng.annotations.Test;

public class InvokePostRenderCleanupOnResourcesWorkerTest extends InternalBaseTestCase
{
    @Test
    public void not_a_root_transformation()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_isRootTransformation(ct, false);

        replay();

        ComponentClassTransformWorker worker = new InvokePostRenderCleanupOnResourcesWorker();

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void invocation_added_for_root_transformation()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_isRootTransformation(ct, true);

        train_getResourcesFieldName(ct, "rez");

        train_extendMethod(ct, TransformConstants.POST_RENDER_CLEANUP_SIGNATURE, "rez.postRenderCleanup();");

        replay();

        ComponentClassTransformWorker worker = new InvokePostRenderCleanupOnResourcesWorker();

        worker.transform(ct, model);

        verify();
    }

    protected final void train_isRootTransformation(ClassTransformation transformation, boolean isRoot)
    {
        expect(transformation.isRootTransformation()).andReturn(isRoot).atLeastOnce();
    }

}
