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

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;

import java.lang.reflect.Modifier;

import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.MethodSignature;
import org.apache.tapestry.services.TransformConstants;
import org.testng.annotations.Test;

/**
 * I'd prefer to test these in terms of the behavior of the final class, rather than the generated
 * Javassist method bodies, but that'll have to be saved for later integration tests.
 */
public class OnEventWorkerTest extends InternalBaseTestCase
{

    private static final String BOILERPLATE_1 = "if ($1.isAborted()) return $_;";

    private static final String BOILERPLATE_2 = "$_ = true;";

    @Test
    public void no_methods_with_annotation()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();

        MethodSignature sig = new MethodSignature("foo");

        train_findMethods(ct, sig);
        train_getMethodAnnotation(ct, sig, OnEvent.class, null);

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void minimal_case()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        MethodSignature signature = new MethodSignature("foo");

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, new String[0]);
        train_component(annotation, new String[0]);

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo()\");",
                "foo();",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void filter_by_event_type()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        String[] eventTypes = new String[]
        { "gnip", "gnop" };

        MethodSignature signature = new MethodSignature("foo");

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, eventTypes);

        train_addInjectedField(ct, String[].class, "eventTypes", eventTypes, "_v");

        train_component(annotation, new String[0]);

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                "if ($1.matchesByEventType(_v))",
                "{",
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo()\");",
                "foo();",
                "}",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    protected final void train_addInjectedField(ClassTransformation ct, String suggestedName,
            String actualName, String... values)
    {
        expect(ct.addInjectedField(eq(String[].class), eq(suggestedName), aryEq(values)))
                .andReturn(actualName);
    }

    @Test
    public void by_convention_on_event_type()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();

        MethodSignature signature = new MethodSignature("onSubmit");

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, null);

        train_addInjectedField(ct, "eventTypes", "_v", "Submit");

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                "if ($1.matchesByEventType(_v))",
                "{",
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.onSubmit()\");",
                "onSubmit();",
                "}",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void filter_by_component_id()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        String[] componentIds = new String[]
        { "zork" };

        MethodSignature signature = new MethodSignature("foo");

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, new String[0]);
        train_component(annotation, componentIds);

        train_addInjectedField(ct, java.lang.String[].class, "componentIds", componentIds, "_ids");

        train_getResourcesFieldName(ct, "_res");

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                "if ($1.matchesByComponentId(_res, _ids))",
                "{",
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo()\");",
                "foo();",
                "}",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void by_convention_on_component_id()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();

        MethodSignature signature = new MethodSignature("onAnyEventFromZork");

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, null);

        train_addInjectedField(ct, "componentIds", "_ids", "Zork");

        train_getResourcesFieldName(ct, "_res");

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                "if ($1.matchesByComponentId(_res, _ids))",
                "{",
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.onAnyEventFromZork()\");",
                "onAnyEventFromZork();",
                "}",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void filter_by_both()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        String[] eventTypes = new String[]
        { "gnip", "gnop" };
        String[] componentIds = new String[]
        { "zork" };

        MethodSignature signature = new MethodSignature("foo");

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, eventTypes);

        train_addInjectedField(ct, java.lang.String[].class, "eventTypes", eventTypes, "_v");

        train_component(annotation, componentIds);

        train_addInjectedField(ct, java.lang.String[].class, "componentIds", componentIds, "_ids");

        train_getResourcesFieldName(ct, "_res");

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                "if ($1.matchesByEventType(_v))",
                "{",
                "if ($1.matchesByComponentId(_res, _ids))",
                "{",
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo()\");",
                "foo();",
                "}",
                "}",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();

    }

    @Test
    public void method_with_non_void_return_value()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        MethodSignature signature = new MethodSignature(Modifier.PRIVATE, "java.lang.String",
                "foo", null, null);

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, new String[0]);
        train_component(annotation, new String[0]);

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo()\");",
                "if ($1.storeResult(($w) foo())) return true;",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void method_with_non_primitive_parameter()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        MethodSignature signature = new MethodSignature(Modifier.PRIVATE, "void", "foo",
                new String[]
                { "java.lang.String" }, null);

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, new String[0]);
        train_component(annotation, new String[0]);

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo(java.lang.String)\");",
                "foo((java.lang.String)$1.coerceContext(0, \"java.lang.String\"));",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void method_with_primitive_parameter()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        MethodSignature signature = new MethodSignature(Modifier.PRIVATE, "void", "foo",
                new String[]
                { "boolean" }, null);

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, new String[0]);
        train_component(annotation, new String[0]);

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo(boolean)\");",
                "foo(((java.lang.Boolean)$1.coerceContext(0, \"java.lang.Boolean\")).booleanValue());",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();

    }

    @Test
    public void method_with_multiple_parameters()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        MethodSignature signature = new MethodSignature(Modifier.PRIVATE, "void", "foo",
                new String[]
                { "java.lang.String", "java.lang.Integer" }, null);

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, new String[0]);
        train_component(annotation, new String[0]);

        train_getClassName(ct, "foo.Bar");

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                BOILERPLATE_2,
                "$1.setSource(this, \"foo.Bar.foo(java.lang.String, java.lang.Integer)\");",
                "foo((java.lang.String)$1.coerceContext(0, \"java.lang.String\"), ",
                "(java.lang.Integer)$1.coerceContext(1, \"java.lang.Integer\"));",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    @Test
    public void method_include_context()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        OnEvent annotation = newOnEvent();

        MethodSignature signature = new MethodSignature(Modifier.PRIVATE, "void", "foo",
                new String[]
                { "java.lang.String", OnEventWorker.OBJECT_ARRAY_TYPE, "java.lang.Integer" }, null);

        train_findMethods(ct, signature);

        train_getMethodAnnotation(ct, signature, OnEvent.class, annotation);

        train_value(annotation, new String[0]);
        train_component(annotation, new String[0]);

        train_getClassName(ct, "foo.Bar");

        // Notice that the context doesn't affect the indexing of the other parameters. Though it is
        // unlikely that a method would use both a context array and explicit context parameters.

        train_extendMethod(
                ct,
                TransformConstants.HANDLE_COMPONENT_EVENT,
                "{",
                BOILERPLATE_1,
                BOILERPLATE_2,
                String.format("$1.setSource(this, \"foo.Bar.%s\");", signature
                        .getMediumDescription()),
                "foo((java.lang.String)$1.coerceContext(0, \"java.lang.String\"), ",
                "$1.getContext(), ",
                "(java.lang.Integer)$1.coerceContext(1, \"java.lang.Integer\"));",
                "}");

        replay();

        new OnEventWorker().transform(ct, model);

        verify();
    }

    protected final OnEvent newOnEvent()
    {
        return newMock(OnEvent.class);
    }

    protected final void train_value(OnEvent annotation, String[] values)
    {
        expect(annotation.value()).andReturn(values);
    }

    protected final void train_component(OnEvent annotation, String[] values)
    {
        expect(annotation.component()).andReturn(values);
    }
}
