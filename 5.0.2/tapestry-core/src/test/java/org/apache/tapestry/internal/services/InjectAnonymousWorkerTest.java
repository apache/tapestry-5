// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.InjectionProvider;
import org.apache.tapestry.services.Request;
import org.testng.annotations.Test;

public class InjectAnonymousWorkerTest extends InternalBaseTestCase
{
    private static final String WEBREQUEST_CLASS_NAME = Request.class.getName();

    @Test
    public void anonymous_injection()
    {
        ServiceLocator locator = newServiceLocator();
        InjectionProvider ip = newMock(InjectionProvider.class);
        Inject annotation = newMock(Inject.class);
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", WEBREQUEST_CLASS_NAME);

        train_provideInjection(ip, "myfield", WEBREQUEST_CLASS_NAME, locator, ct, model, true);

        ct.claimField("myfield", annotation);

        replay();

        ComponentClassTransformWorker worker = new InjectAnonymousWorker(locator, ip);

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void anonymous_injection_not_provided()
    {
        ServiceLocator locator = newServiceLocator();
        InjectionProvider ip = newMock(InjectionProvider.class);
        Inject annotation = newMock(Inject.class);
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", WEBREQUEST_CLASS_NAME);

        train_provideInjection(ip, "myfield", WEBREQUEST_CLASS_NAME, locator, ct, model, false);

        train_getClassName(ct, "foo.Baz");

        replay();

        ComponentClassTransformWorker worker = new InjectAnonymousWorker(locator, ip);

        try
        {
            worker.transform(ct, model);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages.noInjectionFound(
                    "foo.Baz",
                    "myfield",
                    WEBREQUEST_CLASS_NAME));
        }

        verify();
    }
}
