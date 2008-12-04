// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.InjectionProvider;
import org.testng.annotations.Test;

public class AssetInjectionProviderTest extends InternalBaseTestCase
{
    @Test
    public void no_path_annotation()
    {
        SymbolSource symbolSource = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();
        ObjectLocator locator = mockObjectLocator();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        String fieldName = "myField";

        train_getFieldAnnotation(ct, fieldName, Path.class, null);

        replay();

        InjectionProvider provider = new AssetInjectionProvider(symbolSource, assetSource);

        assertFalse(provider.provideInjection(fieldName, String.class, locator, ct, model));

        verify();
    }

    @Test
    public void path_annotation_present()
    {
        SymbolSource symbolSource = mockSymbolSource();
        AssetSource assetSource = mockAssetSource();
        ObjectLocator locator = mockObjectLocator();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();
        Path annotation = mockPath();
        Resource baseResource = mockResource();

        String fieldName = "myField";
        Class fieldType = Object.class;
        String value = "${foo}";
        String expanded = "foo.gif";

        train_getFieldAnnotation(ct, fieldName, Path.class, annotation);

        train_value(annotation, value);
        train_expandSymbols(symbolSource, value, expanded);

        train_addInjectedField(ct, AssetSource.class, "assetSource", assetSource, "as");

        train_getBaseResource(model, baseResource);

        train_addInjectedField(ct, Resource.class, "baseResource", baseResource, "br");

        train_getResourcesFieldName(ct, "rez");

        // This only tests that the code is generated as expected (which is a bit brittle), it
        // doesn't prove that the generated code actually works, but we have lots of integration
        // tests for that.

        ct
                .extendConstructor("myField = (java.lang.Object) as.getAsset(br, \"foo.gif\", rez.getLocale());");

        ct.makeReadOnly(fieldName);

        replay();

        InjectionProvider provider = new AssetInjectionProvider(symbolSource, assetSource);

        assertTrue(provider.provideInjection(fieldName, fieldType, locator, ct, model));

        verify();
    }
}
