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
import org.apache.tapestry5.plastic.PlasticClass;
import org.testng.annotations.Test;

public class MixinAfterWorkerTest extends InternalBaseTestCase
{
    @Test
    public void annotation_not_present()
    {
        PlasticClass pc = newMock(PlasticClass.class);
        MutableComponentModel model = mockMutableComponentModel();

        expect(pc.hasAnnotation(MixinAfter.class)).andReturn(false);

        replay();

        new MixinAfterWorker().transform(pc, null, model);

        verify();
    }

    @Test
    public void annotation_present()
    {
        PlasticClass pc = newMock(PlasticClass.class);
        MutableComponentModel model = mockMutableComponentModel();


        expect(pc.hasAnnotation(MixinAfter.class)).andReturn(true);

        model.setMixinAfter(true);

        replay();

        new MixinAfterWorker().transform(pc, null, model);

        verify();
    }
}
