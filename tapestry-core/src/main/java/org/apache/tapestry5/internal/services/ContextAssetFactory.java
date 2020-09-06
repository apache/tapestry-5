// Copyright 2006, 2007, 2008, 2009, 2010, 2013 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

/**
 * Implementation of {@link AssetFactory} for assets that are part of the web application context.
 *
 * @see org.apache.tapestry5.internal.services.ContextResource
 */
public class ContextAssetFactory extends AbstractAssetFactory
{

    public ContextAssetFactory(ResponseCompressionAnalyzer compressionAnalyzer,
                               ResourceChangeTracker resourceChangeTracker,
                               StreamableResourceSource streamableResourceSource,
                               AssetPathConstructor assetPathConstructor,
                               Context context)
    {
        super(compressionAnalyzer, resourceChangeTracker, streamableResourceSource, assetPathConstructor,
                new ContextResource(context, "/"));
    }

    public Asset createAsset(final Resource resource)
    {
        return createAsset(resource, RequestConstants.CONTEXT_FOLDER, resource.getPath());
    }
}
