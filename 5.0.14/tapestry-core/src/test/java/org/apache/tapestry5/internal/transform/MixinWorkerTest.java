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

import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.TransformConstants;
import org.testng.annotations.Test;

public class MixinWorkerTest extends InternalBaseTestCase
{
    @Test
    public void no_fields_with_mixin_annotation()
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        ClassTransformation transformation = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsWithAnnotation(transformation, Mixin.class);

        replay();

        new MixinWorker(resolver).transform(transformation, model);

        verify();
    }

    @Test
    public void field_with_explicit_type()
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        ClassTransformation transformation = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Mixin annotation = newMixin("Bar");

        train_findFieldsWithAnnotation(transformation, Mixin.class, "fred");
        train_getFieldAnnotation(transformation, "fred", Mixin.class, annotation);
        train_getFieldType(transformation, "fred", "foo.bar.Baz");

        train_resolveMixinTypeToClassName(resolver, "Bar", "foo.bar.BazMixin");

        model.addMixinClassName("foo.bar.BazMixin");

        transformation.makeReadOnly("fred");

        train_getResourcesFieldName(transformation, "rez");

        train_extendMethod(
                transformation,
                TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                "fred = (foo.bar.Baz) rez.getMixinByClassName(\"foo.bar.BazMixin\");");

        transformation.claimField("fred", annotation);

        replay();

        new MixinWorker(resolver).transform(transformation, model);

        verify();
    }

    @Test
    public void field_with_no_specific_mixin_type()
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        ClassTransformation transformation = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Mixin annotation = newMixin("");

        train_findFieldsWithAnnotation(transformation, Mixin.class, "fred");
        train_getFieldAnnotation(transformation, "fred", Mixin.class, annotation);
        train_getFieldType(transformation, "fred", "foo.bar.Baz");

        model.addMixinClassName("foo.bar.Baz");

        transformation.makeReadOnly("fred");

        train_getResourcesFieldName(transformation, "rez");

        train_extendMethod(
                transformation,
                TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                "fred = (foo.bar.Baz) rez.getMixinByClassName(\"foo.bar.Baz\");");

        transformation.claimField("fred", annotation);

        replay();

        new MixinWorker(resolver).transform(transformation, model);

        verify();

    }

    protected final void train_resolveMixinTypeToClassName(ComponentClassResolver resolver,
                                                           String mixinType, String mixinClassName)
    {
        expect(resolver.resolveMixinTypeToClassName(mixinType)).andReturn(mixinClassName);
    }

    private Mixin newMixin(String value)
    {
        Mixin annotation = newMock(Mixin.class);

        expect(annotation.value()).andReturn(value);

        return annotation;
    }
}
