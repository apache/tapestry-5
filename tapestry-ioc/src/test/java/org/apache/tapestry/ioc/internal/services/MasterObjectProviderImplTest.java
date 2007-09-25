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

package org.apache.tapestry.ioc.internal.services;

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.apache.tapestry.services.MasterObjectProvider;
import org.testng.annotations.Test;

public class MasterObjectProviderImplTest extends IOCTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void found_match_first()
    {
        ObjectProvider prov1 = mockObjectProvider();
        ObjectProvider prov2 = mockObjectProvider();
        Class type = Runnable.class;
        AnnotationProvider ap = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();
        Object expected = mockRunnable();

        train_provide(prov1, type, ap, locator, expected);

        List<ObjectProvider> configuration = Arrays.asList(prov1, prov2);

        replay();

        MasterObjectProvider master = new MasterObjectProviderImpl(configuration);

        assertSame(master.provide(type, ap, locator, true), expected);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void found_later_match()
    {
        ObjectProvider prov1 = mockObjectProvider();
        ObjectProvider prov2 = mockObjectProvider();
        Class type = Runnable.class;
        AnnotationProvider ap = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();
        Object expected = mockRunnable();

        train_provide(prov1, type, ap, locator, null);
        train_provide(prov2, type, ap, locator, expected);

        List<ObjectProvider> configuration = Arrays.asList(prov1, prov2);

        replay();

        MasterObjectProvider master = new MasterObjectProviderImpl(configuration);

        assertSame(master.provide(type, ap, locator, true), expected);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void no_match_but_optional()
    {
        ObjectProvider prov1 = mockObjectProvider();
        ObjectProvider prov2 = mockObjectProvider();
        Class type = Runnable.class;
        AnnotationProvider ap = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();

        train_provide(prov1, type, ap, locator, null);
        train_provide(prov2, type, ap, locator, null);

        List<ObjectProvider> configuration = Arrays.asList(prov1, prov2);

        replay();

        MasterObjectProvider master = new MasterObjectProviderImpl(configuration);

        assertNull(master.provide(type, ap, locator, false));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void no_match_and_required()
    {
        ObjectProvider prov1 = mockObjectProvider();
        ObjectProvider prov2 = mockObjectProvider();
        Class type = Runnable.class;
        AnnotationProvider ap = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();
        Object expected = mockRunnable();

        train_provide(prov1, type, ap, locator, null);
        train_provide(prov2, type, ap, locator, null);

        train_getService(locator, type, expected);

        List<ObjectProvider> configuration = Arrays.asList(prov1, prov2);

        replay();

        MasterObjectProvider master = new MasterObjectProviderImpl(configuration);

        assertSame(master.provide(type, ap, locator, true), expected);

        verify();
    }
}
