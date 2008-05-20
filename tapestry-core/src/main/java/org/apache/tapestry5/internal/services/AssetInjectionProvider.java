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
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.InjectionProvider;

import static java.lang.String.format;

/**
 * Performs injection of assets, based on the presence of the {@link Path} annotation. This is more useful than the
 * general {@link AssetObjectProvider}, becase relative assets are supported.
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

        if (path == null) return false;

        String expanded = symbolSource.expandSymbols(path.value());

        String sourceFieldName = transformation.addInjectedField(AssetSource.class, "assetSource", assetSource);

        String baseResourceFieldName = transformation.addInjectedField(Resource.class, "baseResource",
                                                                       componentModel.getBaseResource());

        String resourcesFieldName = transformation.getResourcesFieldName();

        String statement = format("%s = (%s) %s.getAsset(%s, \"%s\", %s.getLocale());", fieldName, fieldType.getName(),
                                  sourceFieldName, baseResourceFieldName, expanded, resourcesFieldName);

        transformation.extendConstructor(statement);

        transformation.makeReadOnly(fieldName);

        return true;
    }

}
