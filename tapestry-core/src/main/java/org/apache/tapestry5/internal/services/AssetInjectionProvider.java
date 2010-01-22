// Copyright 2007, 2008, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import java.util.Locale;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.InjectionProvider;

/**
 * Performs injection of assets, based on the presence of the {@link Path} annotation. This is more
 * useful than the
 * general {@link AssetObjectProvider}, because relative assets are supported.
 */
public class AssetInjectionProvider implements InjectionProvider
{
    private final SymbolSource symbolSource;

    private final AssetSource assetSource;

    public AssetInjectionProvider(SymbolSource symbolSource, AssetSource assetSource)
    {
        this.symbolSource = symbolSource;
        this.assetSource = assetSource;
    }

    public boolean provideInjection(String fieldName, Class fieldType, ObjectLocator locator,
            ClassTransformation transformation, MutableComponentModel componentModel)
    {
        Path path = transformation.getFieldAnnotation(fieldName, Path.class);

        if (path == null)
            return false;

        final String expanded = symbolSource.expandSymbols(path.value());

        final Resource baseResource = componentModel.getBaseResource();

        ComponentValueProvider<Asset> provider = new ComponentValueProvider<Asset>()
        {

            public Asset get(ComponentResources resources)
            {
                Locale locale = resources.getLocale();

                return assetSource.getAsset(baseResource, expanded, locale);
            }
        };

        transformation.injectFieldIndirect(fieldName, provider);

        return true;
    }

}
