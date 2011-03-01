// Copyright 2009, 2010, 2011 The Apache Software Foundation
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

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.Response;

public class AssetResourceLocatorImpl implements AssetResourceLocator
{
    private final ResourceDigestManager digestManager;

    private final Response response;

    private final AssetSource assetSource;

    public AssetResourceLocatorImpl(ResourceDigestManager digestManager, Response response, AssetSource assetSource)
    {
        this.digestManager = digestManager;
        this.response = response;
        this.assetSource = assetSource;
    }

    public Resource findClasspathResourceForPath(String path) throws IOException
    {
        Resource resource = assetSource.resourceForPath(path);

        if (!digestManager.requiresDigest(resource))
            return resource;

        return validateChecksumOfClasspathResource(resource);
    }

    /**
     * Validates the checksum encoded into the resource, and returns the true resource (with the checksum
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

                String actualDigest = digestManager.getDigest(result);

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
