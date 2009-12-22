// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Recognizes requests where the path begins with "/asset/" and delivers the content therein as a bytestream. Also
 * handles requests that are simply polling for a change to the file.
 *
 * @see ResourceStreamer
 * @see ClasspathAssetAliasManager
 * @see ResourceCache
 */
public class AssetDispatcher implements Dispatcher
{
    private final ResourceStreamer streamer;

    private final ClasspathAssetAliasManager aliasManager;

    private final ResourceCache resourceCache;

    static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    public AssetDispatcher(ResourceStreamer streamer, ClasspathAssetAliasManager aliasManager,
                           ResourceCache resourceCache)
    {
        this.streamer = streamer;
        this.aliasManager = aliasManager;
        this.resourceCache = resourceCache;
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        // Remember that the request path does not include the context path, so we can simply start
        // looking for the asset path prefix right off the bat.

        if (!path.startsWith(RequestConstants.ASSET_PATH_PREFIX)) return false;

        // ClassLoaders like their paths to start with a leading slash.

        String resourcePath = aliasManager.toResourcePath(path);

        Resource resource = findResourceAndValidateDigest(response, resourcePath);

        if (resource == null) return true;

        if (!resource.exists())
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ServicesMessages
                    .assetDoesNotExist(resource));
            return true;
        }

        long ifModifiedSince = 0;

        try
        {
            ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE_HEADER);
        }
        catch (IllegalArgumentException ex)
        {
            // Simulate the header being missing if it is poorly formatted.

            ifModifiedSince = -1;
        }

        if (ifModifiedSince > 0)
        {
            long modified = resourceCache.getTimeModified(resource);

            if (ifModifiedSince >= modified)
            {
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED, "");
                return true;
            }
        }

        streamer.streamResource(resource);

        return true;
    }

    /**
     * @param response     used to send errors back to the client
     * @param resourcePath the path to the requested resource, from the request
     * @return the resource for the path, with the digest stripped out of the URL, or null if the digest is invalid (and
     *         an error has been sent back to the client)
     * @throws IOException
     */
    private Resource findResourceAndValidateDigest(Response response, String resourcePath) throws IOException
    {
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

        if (!valid)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ServicesMessages
                    .wrongAssetDigest(result));
            result = null;
        }

        return result;
    }
}
