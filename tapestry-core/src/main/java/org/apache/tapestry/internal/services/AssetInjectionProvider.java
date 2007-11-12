// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.InjectionProvider;

import static java.lang.String.format;

/**
 * Performs injection of assets, based on the presence of the {@link Path} annotation. This is more
 * useful than the general {@link AssetObjectProvider}, becase relative assets are supported.
 */
public class AssetInjectionProvider implements InjectionProvider
{
    private final SymbolSource _symbolSource;

    private final AssetSource _assetSource;

    public AssetInjectionProvider(SymbolSource symbolSource, AssetSource assetSource)
    {
        _symbolSource = symbolSource;
        _assetSource = assetSource;
    }

    public boolean provideInjection(String fieldName, Class fieldType, ObjectLocator locator,
                                    ClassTransformation transformation, MutableComponentModel componentModel)
    {
        Path path = transformation.getFieldAnnotation(fieldName, Path.class);

        if (path == null) return false;

        String expanded = _symbolSource.expandSymbols(path.value());

        String sourceFieldName = transformation.addInjectedField(
                AssetSource.class,
                "assetSource",
                _assetSource);
        String resourcesFieldName = transformation.getResourcesFieldName();

        String statement = format(
                "%s = (%s) %s.findAsset(%s.getBaseResource(), \"%s\", %s.getLocale());",
                fieldName,
                fieldType.getName(),
                sourceFieldName,
                resourcesFieldName,
                expanded,
                resourcesFieldName);

        transformation.extendConstructor(statement);

        transformation.makeReadOnly(fieldName);

        return true;

    }

}
