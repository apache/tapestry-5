// Copyright 2006-2013 The Apache Software Foundation
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
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.AssetAlias;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.ClasspathProvider;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

/**
 * Generates Assets for files on the classpath.
 *
 * @see AssetDispatcher
 */
@Marker(ClasspathProvider.class)
public class ClasspathAssetFactory extends AbstractAssetFactory
{
    private final ClasspathAssetAliasManager aliasManager;

    public ClasspathAssetFactory(ResponseCompressionAnalyzer compressionAnalyzer,
                                 ResourceChangeTracker resourceChangeTracker,
                                 StreamableResourceSource streamableResourceSource,
                                 AssetPathConstructor assetPathConstructor,
                                 ClasspathAssetAliasManager aliasManager)
    {
        super(compressionAnalyzer, resourceChangeTracker, streamableResourceSource, assetPathConstructor,
                new ClasspathResource(""));

        this.aliasManager = aliasManager;
    }

    public Asset createAsset(Resource resource)
    {
        AssetAlias alias = aliasManager.extractAssetAlias(resource);

        return createAsset(resource, alias.virtualFolder, alias.path);
    }
}
