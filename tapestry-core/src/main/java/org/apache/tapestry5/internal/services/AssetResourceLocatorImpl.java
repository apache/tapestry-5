// Copyright 2009, 2010 The Apache Software Foundation
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

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.internal.AssetConstants;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetPathConstructor;

public class AssetResourceLocatorImpl implements AssetResourceLocator
{
    private final ResourceCache resourceCache;

    private final Response response;

    private final AssetSource assetSource;

    private final String contextAssetPathPrefix;

    private final String assetPathPrefix;
    
    private final Map<String, String> classpathMappings;

    public AssetResourceLocatorImpl(ResourceCache resourceCache,

    Response response,

    AssetSource assetSource, ClasspathAssetAliasManager aliasManager,

    AssetPathConstructor assetPathConstructor)
    {
        this.resourceCache = resourceCache;
        this.response = response;
        this.assetSource = assetSource;

        classpathMappings = aliasManager.getMappings();

        contextAssetPathPrefix = assetPathConstructor.constructAssetPath(RequestConstants.CONTEXT_FOLDER, "");
        
        assetPathPrefix = assetPathConstructor.getAssetPathPrefix();
        
    }

    public Resource findResourceForAssetPath(String path) throws IOException
    {
        if (path.startsWith(contextAssetPathPrefix))
        {
            String assetPath = String.format("%s:%s", AssetConstants.CONTEXT, path.substring(contextAssetPathPrefix
                    .length() + 1));

            return assetSource.resourceForPath(assetPath);
        }

        // TODO: We need some work in this area to support more than just classpath and context assets
        // but any asset.

        // The path provided has been mangled into an asset URL for a classpath asset. Let's unmangle it.

        // Strip off the asset path prefix, leaving just the virtual folder and path below it.
        
        String virtualPath = path.substring(assetPathPrefix.length());
        
        int slashx = virtualPath.indexOf('/');
        String virtualFolder = virtualPath.substring(0, slashx);
        String extraPath = virtualPath.substring(slashx + 1);

        String assetPath = classpathMappings.get(virtualFolder) + "/" + extraPath;

        return findClasspathResourceForPath(assetPath);
    }

    public Resource findClasspathResourceForPath(String path) throws IOException
    {

        Resource resource = assetSource.resourceForPath(path);

        if (!resourceCache.requiresDigest(resource))
            return resource;

        return validateChecksumOfClasspathResource(resource);
    }

    /**
     * Validates the checksome encoded into the resource, and returns the true resource (with the checksum
     * portion removed from the file name).
     */
    private Resource validateChecksumOfClasspathResource(Resource resource) throws IOException
    {
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

        if (valid)
            return result;

        // TODO: Perhaps we should send an exception here, so that the caller can decide
        // to send the error. I'm not happy with this.

        response.sendError(HttpServletResponse.SC_FORBIDDEN, ServicesMessages.wrongAssetDigest(result));

        return null;
    }
}
