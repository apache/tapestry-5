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

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.SetupRender;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;
import org.apache.tapestry.services.TransformMethodSignature;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;

/**
 * Of course, we're committing the cardinal sin of testing the code that's generated, rather than
 * the *behavior* of the generated code. Fortunately, we back all this up with lots and lots of
 * integration testing.
 */
public class ComponentLifecycleMethodWorkerTest extends TapestryTestCase
{
    @Test
    public void no_methods_with_annotation()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        TransformMethodSignature sig = new TransformMethodSignature("someRandomMethod");

        train_findMethods(tf, sig);

        train_getMethodAnnotation(tf, sig, SetupRender.class, null);

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }

    @Test
    public void added_lifecycle_method_is_ignored()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findMethods(tf, TransformConstants.SETUP_RENDER_SIGNATURE);

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }

    @Test
    public void void_method()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        SetupRender annotation = newSetupRender();

        TransformMethodSignature sig = new TransformMethodSignature("aMethod");

        train_findMethods(tf, sig);

        train_getMethodAnnotation(tf, sig, SetupRender.class, annotation);

        train_isRootClass(model, false);

        train_addMethod(
                tf,
                TransformConstants.SETUP_RENDER_SIGNATURE,
                "{ super.setupRender($$); if ($2.isAborted()) return;  aMethod(); }");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }

    @Test
    public void match_on_method_name()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        TransformMethodSignature sig = new TransformMethodSignature("setupRender");

        train_findMethods(tf, sig);

        train_isRootClass(model, false);

        train_addMethod(
                tf,
                TransformConstants.SETUP_RENDER_SIGNATURE,
                "{ super.setupRender($$); if ($2.isAborted()) return;  setupRender(); }");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }

    protected final SetupRender newSetupRender()
    {
        return newMock(SetupRender.class);
    }

    @Test
    public void multiple_methods_reverse_order()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        SetupRender annotation = newSetupRender();

        TransformMethodSignature siga = new TransformMethodSignature("aMethod");
        TransformMethodSignature sigb = new TransformMethodSignature("bMethod");

        train_findMethods(tf, siga, sigb);

        train_getMethodAnnotation(tf, siga, SetupRender.class, annotation);
        train_getMethodAnnotation(tf, sigb, SetupRender.class, annotation);

        train_isRootClass(model, false);

        train_addMethod(
                tf,
                TransformConstants.SETUP_RENDER_SIGNATURE,
                "{ bMethod(); aMethod(); super.setupRender($$); }");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, true);

        worker.transform(tf, model);

        verify();
    }

    @Test
    public void multiple_methods_parent_class_reverse_order()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        SetupRender annotation = newSetupRender();

        TransformMethodSignature siga = new TransformMethodSignature("aMethod");
        TransformMethodSignature sigb = new TransformMethodSignature("bMethod");

        train_findMethods(tf, siga, sigb);

        train_getMethodAnnotation(tf, siga, SetupRender.class, annotation);
        train_getMethodAnnotation(tf, sigb, SetupRender.class, annotation);

        train_isRootClass(model, true);

        train_addMethod(tf, TransformConstants.SETUP_RENDER_SIGNATURE, "{ bMethod(); aMethod(); }");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, true);

        worker.transform(tf, model);

        verify();

    }

    @Test
    public void method_in_base_class()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        SetupRender annotation = newSetupRender();

        TransformMethodSignature sig = new TransformMethodSignature("aMethod");

        train_findMethods(tf, sig);

        train_getMethodAnnotation(tf, sig, SetupRender.class, annotation);

        train_isRootClass(model, true);

        train_addMethod(tf, TransformConstants.SETUP_RENDER_SIGNATURE, "{ aMethod(); }");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();

    }

    @Test
    public void method_with_markup_writer_parameter()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        SetupRender annotation = newSetupRender();

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "void", "aMethod", new String[]
                {MarkupWriter.class.getName()}, null);

        train_findMethods(tf, sig);

        train_getMethodAnnotation(tf, sig, SetupRender.class, annotation);

        train_isRootClass(model, false);

        train_addMethod(
                tf,
                TransformConstants.SETUP_RENDER_SIGNATURE,
                "{ super.setupRender($$); if ($2.isAborted()) return; aMethod($1); }");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();

    }

    @Test
    public void nonvoid_method()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        SetupRender annotation = newSetupRender();

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PROTECTED, "boolean", "aMethod", null,
                                                                    null);

        train_findMethods(tf, sig);

        train_getMethodAnnotation(tf, sig, SetupRender.class, annotation);
        train_getMethodIdentifier(tf, sig, "biff.Baz.aMethod()");

        train_isRootClass(model, false);

        train_addMethod(
                tf,
                TransformConstants.SETUP_RENDER_SIGNATURE,
                "{",
                "super.setupRender($$);",
                "if ($2.isAborted()) return; ",
                "$2.setSource(this, \"biff.Baz.aMethod()\");",
                "if ($2.storeResult(($w) aMethod())) return;",
                "}");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }

    @Test
    public void multiple_methods()
    {
        ClassTransformation tf = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        SetupRender annotation = newSetupRender();

        TransformMethodSignature siga = new TransformMethodSignature(Modifier.PROTECTED, "boolean", "aMethod", null,
                                                                     null);
        TransformMethodSignature sigb = new TransformMethodSignature(Modifier.PUBLIC, "void", "bMethod", new String[]
                {MarkupWriter.class.getName()}, null);

        String ida = "aMethod()";

        train_findMethods(tf, siga, sigb);

        train_getMethodAnnotation(tf, siga, SetupRender.class, annotation);
        train_getMethodIdentifier(tf, siga, ida);

        train_getMethodAnnotation(tf, sigb, SetupRender.class, annotation);

        train_isRootClass(model, false);

        train_addMethod(
                tf,
                TransformConstants.SETUP_RENDER_SIGNATURE,
                "{ super.setupRender($$);",
                "if ($2.isAborted()) return;",
                "$2.setSource(this, \"aMethod()\");",
                "if ($2.storeResult(($w) aMethod())) return;",
                "bMethod($1); }");

        replay();

        ComponentClassTransformWorker worker = new ComponentLifecycleMethodWorker(
                TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);

        worker.transform(tf, model);

        verify();
    }

}
