// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.Request;

/**
 * Implementation of {@link AssetFactory} for assets that are part of the web application context.
 *
 * @see org.apache.tapestry5.internal.services.ContextResource
 */
public class ContextAssetFactory implements AssetFactory
{
    private final Request request;

    private final String pathPrefix;

    private final Resource rootResource;

    public ContextAssetFactory(Request request, Context context,

                               @Inject @Symbol(SymbolConstants.APPLICATION_VERSION)
                               String applicationVersion)
    {
        this.request = request;

        pathPrefix = RequestConstants.ASSET_PATH_PREFIX + RequestConstants.CONTEXT_FOLDER
                + applicationVersion + "/";

        rootResource = new ContextResource(context, "/");
    }

    public Asset createAsset(final Resource resource)
    {
        final String completePath = request.getContextPath() + pathPrefix + resource.getPath();

        return new Asset()
        {
            public Resource getResource()
            {
                return resource;
            }

            public String toClientURL()
            {
                return completePath;
            }

            /**
             * Returns the client URL, which is essential to allow informal parameters of type
             * Asset to generate a proper value.
             */
            @Override
            public String toString()
            {
                return toClientURL();
            }
        };
    }

    /**
     * Returns the root {@link org.apache.tapestry5.internal.services.ContextResource}.
     */
    public Resource getRootResource()
    {
        return rootResource;
    }
}
