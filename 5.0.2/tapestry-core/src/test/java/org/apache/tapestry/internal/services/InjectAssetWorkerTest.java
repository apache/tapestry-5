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
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.ClassTransformation;
import org.testng.annotations.Test;

public class InjectAssetWorkerTest extends InternalBaseTestCase
{
    @Test
    public void asset_field_without_annotation()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();

        train_findFieldsOfType(ct, InjectAssetWorker.ASSET_TYPE_NAME, "_fred");

        train_getFieldAnnotation(ct, "_fred", Inject.class, null);

        replay();

        new InjectAssetWorker(null).transform(ct, model);

        verify();
    }

    @Test
    public void asset_field_annotation_has_blank_value()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        Inject annotation = newMock(Inject.class);

        train_findFieldsOfType(ct, InjectAssetWorker.ASSET_TYPE_NAME, "_fred");

        train_getFieldAnnotation(ct, "_fred", Inject.class, annotation);

        train_value(annotation, "");

        replay();

        new InjectAssetWorker(null).transform(ct, model);

        verify();
    }

    @Test
    public void asset_field_with_full_annotation()
    {
        ClassTransformation ct = newClassTransformation();
        MutableComponentModel model = newMutableComponentModel();
        Inject annotation = newMock(Inject.class);
        AssetSource source = newMock(AssetSource.class);
        Resource r = newResource();

        train_findFieldsOfType(ct, InjectAssetWorker.ASSET_TYPE_NAME, "_fred");

        train_getFieldAnnotation(ct, "_fred", Inject.class, annotation);

        train_value(annotation, "foo.gif");

        train_addInjectedField(ct, AssetSource.class, "assetSource", source, "as");

        train_getBaseResource(model, r);

        train_addInjectedField(ct, Resource.class, "baseResource", r, "res");
        train_getResourcesFieldName(ct, "resources");

        ct.makeReadOnly("_fred");
        ct.claimField("_fred", annotation);

        train_extendConstructor(
                ct,
                "{",
                "_fred = as.findAsset(res, \"foo.gif\", resources.getLocale());",
                "}");

        replay();

        new InjectAssetWorker(source).transform(ct, model);

        verify();
    }
}
