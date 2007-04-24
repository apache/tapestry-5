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
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.Request;
import org.testng.annotations.Test;

public class InjectNamedWorkerTest extends InternalBaseTestCase
{

    private static final String WEBREQUEST_CLASS_NAME = Request.class.getName();

    @Test
    public void annotation_has_value()
    {
        ObjectProvider provider = newObjectProvider();
        ServiceLocator locator = newServiceLocator();
        Inject annotation = newMock(Inject.class);
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        Request injected = newRequest();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_value(annotation, "foo:Bar");

        train_getFieldType(ct, "myfield", WEBREQUEST_CLASS_NAME);
        train_toClass(ct, WEBREQUEST_CLASS_NAME, Request.class);

        train_provide(provider, "foo:Bar", Request.class, locator, injected);

        ct.injectField("myfield", injected);

        ct.claimField("myfield", annotation);

        replay();

        InjectNamedWorker worker = new InjectNamedWorker(provider, locator);

        worker.transform(ct, model);

        verify();
    }
}
