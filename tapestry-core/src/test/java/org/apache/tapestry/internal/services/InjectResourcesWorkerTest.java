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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.InjectionProvider;
import org.apache.tapestry.services.Request;
import org.testng.annotations.Test;

public class InjectResourcesWorkerTest extends InternalBaseTestCase
{
    private static final String WEBREQUEST_CLASS_NAME = Request.class.getName();

    @Test
    public void anonymous_injection()
    {
        ObjectLocator locator = mockObjectLocator();
        InjectionProvider ip = newMock(InjectionProvider.class);
        Inject annotation = newMock(Inject.class);
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", WEBREQUEST_CLASS_NAME);

        train_provideInjection(ip, "myfield", WEBREQUEST_CLASS_NAME, locator, ct, model, true);

        ct.claimField("myfield", annotation);

        replay();

        ComponentClassTransformWorker worker = new InjectResourcesWorker(locator, ip);

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void anonymous_injection_not_provided()
    {
        ObjectLocator locator = mockObjectLocator();
        InjectionProvider ip = newMock(InjectionProvider.class);
        Inject annotation = newMock(Inject.class);
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", WEBREQUEST_CLASS_NAME);

        train_provideInjection(ip, "myfield", WEBREQUEST_CLASS_NAME, locator, ct, model, false);

        replay();

        ComponentClassTransformWorker worker = new InjectResourcesWorker(locator, ip);

        // Does the work but doesn't claim the field, since there was no match.

        worker.transform(ct, model);

        verify();
    }
}
