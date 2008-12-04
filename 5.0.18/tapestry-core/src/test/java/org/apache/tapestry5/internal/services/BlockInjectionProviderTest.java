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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Id;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.InjectionProvider;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class BlockInjectionProviderTest extends TapestryTestCase
{
    @Test
    public void not_type_block()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        ObjectLocator locator = mockObjectLocator();

        replay();

        InjectionProvider provider = new BlockInjectionProvider();

        assertFalse(provider.provideInjection("myfield", Object.class, locator, ct, model));

        verify();
    }

    protected final Id newId()
    {
        return newMock(Id.class);
    }

    /**
     * This doesn't prove anything; later there will be integration tests that prove that the generated code is valid
     * and works.
     */
    @Test
    public void explicit_block_id_provided_as_annotation()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        ObjectLocator locator = mockObjectLocator();
        Id barneyId = newId();

        String barneyFieldName = "_barneyBlock";

        train_getResourcesFieldName(ct, "rez");

        train_getFieldAnnotation(ct, barneyFieldName, Id.class, barneyId);

        train_value(barneyId, "barney");

        ct.makeReadOnly(barneyFieldName);

        train_extendMethod(
                ct,
                TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                "_barneyBlock = rez.getBlock(\"barney\");");

        replay();

        assertTrue(new BlockInjectionProvider().provideInjection(
                barneyFieldName,
                Block.class,
                locator,
                ct,
                model));

        verify();
    }

    @Test
    public void default_id_for_block_from_field_name()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        ObjectLocator locator = mockObjectLocator();

        String barneyFieldName = "_barney";

        train_getResourcesFieldName(ct, "rez");

        train_getFieldAnnotation(ct, barneyFieldName, Id.class, null);

        ct.makeReadOnly(barneyFieldName);

        train_extendMethod(
                ct,
                TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                "_barney = rez.getBlock(\"barney\");");

        replay();

        assertTrue(new BlockInjectionProvider().provideInjection(
                barneyFieldName,
                Block.class,
                locator,
                ct,
                model));

        verify();
    }

}
