// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.annotations.Meta;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.testng.annotations.Test;

public class MetaWorkerTest extends InternalBaseTestCase
{
    @Test
    public void no_annotation()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();

        train_getAnnotation(ct, Meta.class, null);

        replay();

        new MetaWorker().transform(ct, model);

        verify();
    }

    @Test
    public void has_meta_data()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
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
