// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;

public class PageActivationContextWorkerTest extends TapestryTestCase
{

    private static final String CLASS_NAME = Track.class.getName();

    @Test
    public void activate_dafault_passivate_false()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        PageActivationContext annotation = newMock(PageActivationContext.class);
        ComponentClassTransformWorker worker = new PageActivationContextWorker();

        train_findFieldsWithAnnotation(ct, PageActivationContext.class,
                                       "myfield");
        train_getFieldAnnotation(ct, "myfield", PageActivationContext.class,
                                 annotation);
        train_getFieldType(ct, "myfield", CLASS_NAME);
        expect(annotation.activate()).andReturn(true);

        TransformMethodSignature sig = new TransformMethodSignature(
                Modifier.PROTECTED | Modifier.FINAL, "void", "onActivate",
                new String[]{CLASS_NAME}, null);

        ct.addTransformedMethod(sig, "myfield = $1;");

        expect(annotation.passivate()).andReturn(false);

        replay();

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void activate_false_passivate_default()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        PageActivationContext annotation = newMock(PageActivationContext.class);
        ComponentClassTransformWorker worker = new PageActivationContextWorker();

        train_findFieldsWithAnnotation(ct, PageActivationContext.class,
                                       "myfield");
        train_getFieldAnnotation(ct, "myfield", PageActivationContext.class,
                                 annotation);
        train_getFieldType(ct, "myfield", CLASS_NAME);
        expect(annotation.activate()).andReturn(false);

        expect(annotation.passivate()).andReturn(true);

        TransformMethodSignature sig = new TransformMethodSignature(
                Modifier.PROTECTED | Modifier.FINAL, "java.lang.Object",
                "onPassivate", null, null);

        ct.addTransformedMethod(sig, "return ($w) myfield;");

        replay();

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void activate_false_passivate_false()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        PageActivationContext annotation = newMock(PageActivationContext.class);
        ComponentClassTransformWorker worker = new PageActivationContextWorker();

        train_findFieldsWithAnnotation(ct, PageActivationContext.class,
                                       "myfield");
        train_getFieldAnnotation(ct, "myfield", PageActivationContext.class,
                                 annotation);
        train_getFieldType(ct, "myfield", CLASS_NAME);
        expect(annotation.activate()).andReturn(false);

        expect(annotation.passivate()).andReturn(false);

        replay();

        worker.transform(ct, model);

        verify();
    }

    @Test
    public void illegal_number_of_page_activation_context_handlers()
    {
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        ComponentClassTransformWorker worker = new PageActivationContextWorker();

        train_findFieldsWithAnnotation(ct, PageActivationContext.class,
                                       "myfield", "myfield2");

        replay();

        try
        {
            worker.transform(ct, model);
            fail("did not throw");
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }

        verify();
    }


}
