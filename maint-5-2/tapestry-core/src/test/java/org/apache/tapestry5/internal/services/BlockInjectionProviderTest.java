// Copyright 2007, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Id;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.InjectionProvider;
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
}
