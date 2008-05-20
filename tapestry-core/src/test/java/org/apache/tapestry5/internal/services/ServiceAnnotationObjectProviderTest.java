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

import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class ServiceAnnotationObjectProviderTest extends TapestryTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void no_annotation()
    {
        Class objectType = Runnable.class;
        AnnotationProvider provider = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();

        train_getAnnotation(provider, Service.class, null);

        replay();

        ObjectProvider objectProvider = new ServiceAnnotationObjectProvider();

        assertNull(objectProvider.provide(objectType, provider, locator));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void annotation_present()
    {
        Class objectType = Runnable.class;
        AnnotationProvider provider = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();
        Service service = newMock(Service.class);
        String serviceId = "JiffyPop";
        Runnable instance = mockRunnable();

        train_getAnnotation(provider, Service.class, service);

        expect(service.value()).andReturn(serviceId);

        train_getService(locator, serviceId, objectType, instance);

        replay();

        ObjectProvider objectProvider = new ServiceAnnotationObjectProvider();

        assertSame(objectProvider.provide(objectType, provider, locator), instance);

        verify();
    }
}
