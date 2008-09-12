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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import static org.apache.tapestry5.services.TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE;
import static org.apache.tapestry5.services.TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;

public class UnclaimedFieldWorkerTest extends InternalBaseTestCase
{
    @Test
    public void no_fields()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findUnclaimedFields(ct);

        replay();

        new UnclaimedFieldWorker().transform(ct, model);

        verify();
    }

    @Test
    public void normal()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findUnclaimedFields(ct, "_fred");

        train_getFieldModifiers(ct, "_fred", Modifier.PRIVATE);

        train_getFieldType(ct, "_fred", "foo.Bar");

        expect(ct.addField(Modifier.PRIVATE, "foo.Bar", "_fred_default")).andReturn(
                "_$fred_default");

        ct.extendMethod(CONTAINING_PAGE_DID_LOAD_SIGNATURE, "_$fred_default = _fred;");
        ct.extendMethod(CONTAINING_PAGE_DID_DETACH_SIGNATURE, "_fred = _$fred_default;");

        replay();

        new UnclaimedFieldWorker().transform(ct, model);

        verify();
    }

    @Test
    public void final_fields_are_skipped()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findUnclaimedFields(ct, "_fred");

        train_getFieldModifiers(ct, "_fred", Modifier.PRIVATE | Modifier.FINAL);

        replay();

        new UnclaimedFieldWorker().transform(ct, model);

        verify();
    }

    protected final void train_getFieldModifiers(ClassTransformation transformation,
                                                 String fieldName, int modifiers)
    {
        expect(transformation.getFieldModifiers(fieldName)).andReturn(modifiers).atLeastOnce();
    }
}
