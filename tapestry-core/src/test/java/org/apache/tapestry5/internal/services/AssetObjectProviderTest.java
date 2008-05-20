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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.AssetSource;
import org.testng.annotations.Test;

public class AssetObjectProviderTest extends InternalBaseTestCase
{

    @Test
    public void no_path_annotation()
    {
        AssetSource source = mockAssetSource();
        ObjectLocator locator = mockObjectLocator();
        AnnotationProvider annotationProvider = mockAnnotationProvider();
        TypeCoercer typeCoercer = mockTypeCoercer();
        SymbolSource symbolSource = mockSymbolSource();

        train_getAnnotation(annotationProvider, Path.class, null);

        replay();

        ObjectProvider provider = new AssetObjectProvider(source, typeCoercer, symbolSource);

        assertNull(provider.provide(Asset.class, annotationProvider, locator));

        verify();
    }

    @Test
    public void normal_conversion()
    {
        AssetSource source = mockAssetSource();
        ObjectLocator locator = mockObjectLocator();
        Asset asset = mockAsset();
        String path = "${foo}";
        String expanded = "foo/bar/baz.gif";
        AnnotationProvider annotationProvider = mockAnnotationProvider();
        TypeCoercer typeCoercer = mockTypeCoercer();
        Path pathAnnotation = mockPath();
        SymbolSource symbolSource = mockSymbolSource();

        train_getAnnotation(annotationProvider, Path.class, pathAnnotation);
        train_value(pathAnnotation, path);
        train_expandSymbols(symbolSource, path, expanded);
        train_getAsset(source, null, expanded, null, asset);
        train_coerce(typeCoercer, asset, Asset.class, asset);

        replay();

        ObjectProvider provider = new AssetObjectProvider(source, typeCoercer, symbolSource);

        Asset result = provider.provide(Asset.class, annotationProvider, locator);

        assertSame(result, asset);

        verify();
    }
}
