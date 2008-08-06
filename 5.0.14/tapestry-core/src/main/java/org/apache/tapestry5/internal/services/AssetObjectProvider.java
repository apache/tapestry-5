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
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.Core;

/**
 * Exposes assets (in the current locale). The Inject annotation must be supplemented by a {@link Path} annotation, to
 * identify what asset to be injected.
 */
public class AssetObjectProvider implements ObjectProvider
{
    private final AssetSource source;

    private final TypeCoercer typeCoercer;

    private final SymbolSource symbolSource;

    public AssetObjectProvider(@Core AssetSource source,

                               @Builtin TypeCoercer typeCoercer,

                               @Builtin SymbolSource symbolSource)
    {
        this.source = source;
        this.typeCoercer = typeCoercer;
        this.symbolSource = symbolSource;
    }

    /**
     * Provides the asset. If the expression does not identify an asset domain, with a prefix, it is assumed to be a
     * path on the classpath, relative to the root of the classpath.
     *
     * @param objectType the type of object (which must be Object or Asset)
     * @param locator    not used
     */
    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
    {
        Path path = annotationProvider.getAnnotation(Path.class);

        if (path == null) return null;

        String expanded = symbolSource.expandSymbols(path.value());

        Asset asset = source.getAsset(null, expanded, null);

        return typeCoercer.coerce(asset, objectType);
    }
}
