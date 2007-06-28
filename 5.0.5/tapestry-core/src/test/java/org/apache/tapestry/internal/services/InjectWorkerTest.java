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

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;

import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.Request;
import org.testng.annotations.Test;

public class InjectWorkerTest extends InternalBaseTestCase
{
    private static final String WEBREQUEST_CLASS_NAME = Request.class.getName();

    @Test
    public void annotation_has_value()
    {
        ObjectProvider provider = mockObjectProvider();
        ObjectLocator locator = mockObjectLocator();
        Inject annotation = newMock(Inject.class);
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Request injected = mockRequest();

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", WEBREQUEST_CLASS_NAME);
        train_toClass(ct, WEBREQUEST_CLASS_NAME, Request.class);

        expect(provider.provide(eq(Request.class), isA(AnnotationProvider.class), eq(locator)))
                .andReturn(injected);

        ct.injectField("myfield", injected);

        ct.claimField("myfield", annotation);

        replay();

        InjectWorker worker = new InjectWorker(provider, locator);

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void provide_object_fails()
    {
        ObjectProvider provider = mockObjectProvider();
        ObjectLocator locator = mockObjectLocator();
        Inject annotation = newMock(Inject.class);
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Throwable cause = new RuntimeException("Injection failed.");

        train_findFieldsWithAnnotation(ct, Inject.class, "myfield");
        train_getFieldAnnotation(ct, "myfield", Inject.class, annotation);

        train_getFieldType(ct, "myfield", WEBREQUEST_CLASS_NAME);
        train_toClass(ct, WEBREQUEST_CLASS_NAME, Request.class);

        expect(provider.provide(eq(Request.class), isA(AnnotationProvider.class), eq(locator)))
                .andThrow(cause);
        train_getClassName(ct, "foo.pages.Bar");

        replay();

        InjectWorker worker = new InjectWorker(provider, locator);

        try
        {
            worker.transform(ct, model);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Error obtaining injected value for field foo.pages.Bar.myfield: Injection failed.");
            assertSame(ex.getCause(), cause);
        }

        verify();
    }
}
