// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.assets;

import java.io.IOException;

import org.apache.tapestry5.internal.services.AssetResourceLocator;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetRequestHandler;

/**
 * A handler for asset requests for classpath assets (within a specific folder).
 * Each mapping of the {@link ClasspathAssetAliasManager} gets one of these.
 * 
 * @since 5.2.0
 */
public class ClasspathAssetRequestHandler implements AssetRequestHandler
{
    private final ResourceStreamer streamer;

    private final AssetResourceLocator assetResourceLocator;

    private final String baseFolder;

    public ClasspathAssetRequestHandler(ResourceStreamer streamer, AssetResourceLocator assetResourceLocator,
            String baseFolder)
    {
        this.streamer = streamer;
        this.assetResourceLocator = assetResourceLocator;
        this.baseFolder = baseFolder;
    }

    public boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException
    {
        String assetPath = baseFolder + "/" + extraPath;

        Resource resource = assetResourceLocator.findClasspathResourceForPath(assetPath);

        if (resource == null)
            return false;

        streamer.streamResource(resource);

        return true;
    }
}
