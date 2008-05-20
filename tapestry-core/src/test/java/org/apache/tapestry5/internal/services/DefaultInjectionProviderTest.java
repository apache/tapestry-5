// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.Request;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import org.testng.annotations.Test;

public class DefaultInjectionProviderTest extends InternalBaseTestCase
{
    @Test
    public void object_found()
    {
        MasterObjectProvider master = mockMasterObjectProvider();
        ObjectLocator locator = mockObjectLocator();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Request injected = mockRequest();

        expect(master.provide(eq(Request.class), isA(AnnotationProvider.class), eq(locator), eq(false))).andReturn(
                injected);

        ct.injectField("myfield", injected);

        replay();

        DefaultInjectionProvider provider = new DefaultInjectionProvider(master, locator);

        assertTrue(provider.provideInjection("myfield", Request.class, locator, ct, model));

        verify();
    }

    @Test
    public void object_not_found()
    {
        MasterObjectProvider master = mockMasterObjectProvider();
        ObjectLocator locator = mockObjectLocator();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        expect(master.provide(eq(Request.class), isA(AnnotationProvider.class), eq(locator), eq(false))).andReturn(
                null);

        replay();

        DefaultInjectionProvider provider = new DefaultInjectionProvider(master, locator);

        assertFalse(provider.provideInjection("myfield", Request.class, locator, ct, model));

        verify();
    }
}
