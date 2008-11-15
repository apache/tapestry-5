// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.testng.annotations.Test;

public class MetaWorkerTest extends InternalBaseTestCase
{
    @Test
    public void no_annotation()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_getAnnotation(ct, Meta.class, null);

        replay();

        new MetaWorker().transform(ct, model);

        verify();
    }

    @Test
    public void has_meta_data()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Meta annotation = newMock(Meta.class);

        train_getAnnotation(ct, Meta.class, annotation);

        expect(annotation.value()).andReturn(new String[]
                { "foo=bar", "baz=biff" });

        model.setMeta("foo", "bar");
        model.setMeta("baz", "biff");

        replay();

        new MetaWorker().transform(ct, model);

        verify();
    }
}
