// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.ContextProvider;
import org.apache.tapestry5.services.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AssetResourceLocatorImpl implements AssetResourceLocator
{

    private final ClasspathAssetAliasManager aliasManager;

    private final ResourceCache resourceCache;

    private final AssetFactory contextAssetFactory;

    private final Response response;

    private final String applicationAssetPrefix;

    public AssetResourceLocatorImpl(ClasspathAssetAliasManager aliasManager,

                                    ResourceCache resourceCache,

                                    @Inject @Symbol(SymbolConstants.APPLICATION_VERSION)
                                    String applicationVersion,

                                    @ContextProvider
                                    AssetFactory contextAssetFactory,

                                    Response response)

    {
        this.aliasManager = aliasManager;
        this.resourceCache = resourceCache;
        this.contextAssetFactory = contextAssetFactory;
        this.response = response;

        applicationAssetPrefix = RequestConstants.ASSET_PATH_PREFIX + RequestConstants.CONTEXT_FOLDER + applicationVersion + "/";
    }

    public Resource findResourceForPath(String path) throws IOException
    {
        if (path.startsWith(applicationAssetPrefix))
            return findContextResource(path.substring(applicationAssetPrefix.length()));

        String resourcePath = aliasManager.toResourcePath(path);

        Resource resource = new ClasspathResource(resourcePath);

        if (!resourceCache.requiresDigest(resource)) return resource;

        String file = resource.getFile();

        // Somehow this code got real ugly, but it's all about preventing NPEs when a resource
        // that should have a digest doesn't.

        boolean valid = false;
        Resource result = resource;

        int lastdotx = file.lastIndexOf('.');

        if (lastdotx > 0)
        {
            int prevdotx = file.lastIndexOf('.', lastdotx - 1);

            if (prevdotx > 0)
            {

                String requestDigest = file.substring(prevdotx + 1, lastdotx);

                // Strip the digest out of the file name.

                String realFile = file.substring(0, prevdotx) + file.substring(lastdotx);

                result = resource.forFile(realFile);

                String actualDigest = resourceCache.getDigest(result);

                valid = requestDigest.equals(actualDigest);
            }
        }

        if (valid) return result;

        // TODO: Perhaps we should send an exception here, so that the caller can decide
        // to send the error. I'm not happy with this.
        
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                           ServicesMessages.wrongAssetDigest(result));

        return null;
    }

    private Resource findContextResource(String contextPath)
    {
        return contextAssetFactory.getRootResource().forFile(contextPath);
    }
}
