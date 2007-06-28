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

import org.apache.tapestry.annotations.Id;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.TransformConstants;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

/**
 * Tests a couple of edge cases where there's nothing to inject.
 */
public class InjectBlockWorkerTest extends TapestryTestCase
{
    @Test
    public void no_fields_of_type_block()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsOfType(ct, InjectBlockWorker.BLOCK_TYPE_NAME);

        replay();

        new InjectBlockWorker().transform(ct, model);

        verify();
    }

    @Test
    public void field_missing_annotation()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsOfType(ct, InjectBlockWorker.BLOCK_TYPE_NAME, "fred");

        train_getResourcesFieldName(ct, "rez");

        train_getFieldAnnotation(ct, "fred", Inject.class, null);

        replay();

        new InjectBlockWorker().transform(ct, model);

        verify();
    }

    protected final Id newId()
    {
        return newMock(Id.class);
    }

    /**
     * This doesn't prove anything; later there will be integration tests that prove that the
     * generated code is valid and works.
     */
    @Test
    public void fields_with_annotations()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Inject fredAnnotation = mockInject();
        Inject barneyAnnotation = mockInject();
        Id barneyId = newId();

        String barneyFieldName = "_barneyBlock";
        String fredFieldName = "fred";

        train_findFieldsOfType(
                ct,
                InjectBlockWorker.BLOCK_TYPE_NAME,
                fredFieldName,
                barneyFieldName);

        train_getResourcesFieldName(ct, "rez");

        train_getFieldAnnotation(ct, fredFieldName, Inject.class, fredAnnotation);

        train_getFieldAnnotation(ct, fredFieldName, Id.class, null);

        ct.makeReadOnly(fredFieldName);
        ct.claimField(fredFieldName, fredAnnotation);

        train_getFieldAnnotation(ct, barneyFieldName, Inject.class, barneyAnnotation);
        train_getFieldAnnotation(ct, barneyFieldName, Id.class, barneyId);

        train_value(barneyId, "barney");

        ct.makeReadOnly(barneyFieldName);
        ct.claimField(barneyFieldName, barneyAnnotation);

        train_extendMethod(
                ct,
                TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                "{",
                "fred = rez.getBlock(\"fred\");",
                "_barneyBlock = rez.getBlock(\"barney\");",
                "}");

        replay();

        new InjectBlockWorker().transform(ct, model);

        verify();
    }

}
