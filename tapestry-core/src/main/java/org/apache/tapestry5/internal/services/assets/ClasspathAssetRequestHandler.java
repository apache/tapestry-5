// Copyright 2010, 2013 The Apache Software Foundation
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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.ClasspathAssetProtectionRule;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A handler for asset requests for classpath assets (within a specific folder).
 * Each mapping of the {@link ClasspathAssetAliasManager} gets one of these.
 *
 * @since 5.2.0
 */
public class ClasspathAssetRequestHandler implements AssetRequestHandler
{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ClasspathAssetRequestHandler.class);
    
    private final ResourceStreamer streamer;

    private final AssetSource assetSource;
    
    private final String baseFolder;
    
    private final ClasspathAssetProtectionRule classpathAssetProtectionRule;

    public ClasspathAssetRequestHandler(ResourceStreamer streamer,
                                        AssetSource assetSource, String baseFolder,
                                        ClasspathAssetProtectionRule classpathAssetProtectionRule)
    {
        this.streamer = streamer;
        this.assetSource = assetSource;
        this.baseFolder = baseFolder;
        this.classpathAssetProtectionRule = classpathAssetProtectionRule;
    }

    public boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException
    {
        ChecksumPath path = new ChecksumPath(streamer, baseFolder, extraPath);
        
        final boolean handled;
        if (classpathAssetProtectionRule.block(path.resourcePath) && !path.resourcePath.equals(ChecksumPath.NON_EXISTING_RESOURCE)) 
        {
            if (LOGGER.isWarnEnabled()) 
            {
                LOGGER.warn("Blocked request for classpath asset '" + path.resourcePath + 
                        "'. Contribute a new ClasspathAssetProtectionRule if you need this asset to be publicly accessible.");
            }
            handled = false;
        }
        else
        {
            Resource resource = assetSource.resourceForPath(path.resourcePath);
    
            handled = path.stream(resource);
        }
        return handled;
    }
}
