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
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.InjectionProvider;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.Test;

public class InjectWorkerTest extends InternalBaseTestCase
{
    private static final String REQUEST_CLASS_NAME = Request.class.getName();

    @Test
    public void anonymous_injection()
    {
        ObjectLocator locator = mockObjectLocator();
        InjectionProvider ip = newMock(InjectionProvider.class);
        Inject annotation = newInject();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", REQUEST_CLASS_NAME);
        train_toClass(ct, REQUEST_CLASS_NAME, Request.class);

        train_provideInjection(ip, "myfield", Request.class, locator, ct, model, true);

        ct.claimField("myfield", annotation);

        replay();

        ComponentClassTransformWorker worker = new InjectWorker(locator, ip);

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void anonymous_injection_not_provided()
    {
        ObjectLocator locator = mockObjectLocator();
        InjectionProvider ip = newMock(InjectionProvider.class);
        Inject annotation = newInject();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", REQUEST_CLASS_NAME);
        train_toClass(ct, REQUEST_CLASS_NAME, Request.class);

        train_provideInjection(ip, "myfield", Request.class, locator, ct, model, false);

        replay();

        ComponentClassTransformWorker worker = new InjectWorker(locator, ip);

        // Does the work but doesn't claim the field, since there was no match.

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void injection_provider_threw_exception()
    {
        ObjectLocator locator = mockObjectLocator();
        InjectionProvider ip = newMock(InjectionProvider.class);
        Inject annotation = newInject();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        RuntimeException failure = new RuntimeException("Oops.");

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", REQUEST_CLASS_NAME);
        train_toClass(ct, REQUEST_CLASS_NAME, Request.class);

        expect(ip.provideInjection("myfield", Request.class, locator, ct, model)).andThrow(failure);

        train_getClassName(ct, "foo.bar.Baz");

        replay();

        ComponentClassTransformWorker worker = new InjectWorker(locator, ip);

        try
        {
            worker.transform(ct, model);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Error obtaining injected value for field foo.bar.Baz.myfield: Oops.");
            assertSame(ex.getCause(), failure);
        }

        verify();
    }

    protected final Inject newInject()
    {
        return newMock(Inject.class);
    }
}
