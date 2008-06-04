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

import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.testng.annotations.Test;

public class MixinAfterWorkerTest extends InternalBaseTestCase
{
    @Test
    public void annotation_not_present()
    {
        ClassTransformation transformation = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_getAnnotation(transformation, MixinAfter.class, null);

        replay();

        new MixinAfterWorker().transform(transformation, model);

        verify();
    }

    @Test
    public void annotation_present()
    {
        ClassTransformation transformation = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        MixinAfter annotation = newMock(MixinAfter.class);

        train_getAnnotation(transformation, MixinAfter.class, annotation);
        model.setMixinAfter(true);

        replay();

        new MixinAfterWorker().transform(transformation, model);

        verify();
    }
}
