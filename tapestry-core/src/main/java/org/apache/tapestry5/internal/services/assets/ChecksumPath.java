// Copyright 2013 The Apache Software Foundation
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
import org.apache.tapestry5.internal.services.ResourceStreamer;

import java.io.IOException;

/**
 * Utility used by {@link org.apache.tapestry5.services.assets.AssetRequestHandler} implementations
 * where the first folder in the extra path is actually a computed checksum of the resource's content.
 *
 * @since 5.4
 */
public class ChecksumPath
{
    static final String NON_EXISTING_RESOURCE = "_________________________";

    public final String checksum;

    public final String resourcePath;

    private final ResourceStreamer streamer;

    public ChecksumPath(ResourceStreamer streamer, String baseFolder, String extraPath)
    {
        this.streamer = streamer;
        int slashx = extraPath.indexOf('/');

        checksum = extraPath.substring(0, slashx);

        String morePath = extraPath.substring(slashx + 1);
        
        // Slashes at the end of the path should be dropped because
        // they don't make sense. TAP5-2663
        while (morePath.endsWith("/")) 
        {
            morePath = morePath.substring(0, morePath.length() - 1);
        }

        if (!isBlank(morePath)) 
        {
            resourcePath = baseFolder == null
                    ? morePath
                    : baseFolder + "/" + morePath;
        }
        else {
            // When we only have something which looks like a checksum but no actual path.
            // For example, /assets/META-INF/
            resourcePath = NON_EXISTING_RESOURCE;
        }
    }

    /**
     * If the resource exists and the checksum is correct, stream it to the client and return true. Otherwise,
     * return false.
     *
     * @param resource
     *         to stream
     * @return true if streamed, false otherwise
     * @throws IOException
     */
    public boolean stream(Resource resource) throws IOException
    {
        if (resource == null || !resource.exists())
        {
            return false;
        }


        return streamer.streamResource(resource, checksum, ResourceStreamer.DEFAULT_OPTIONS);
    }
    
    /**
     * Copied from StringUtils since it's the only method we want from it.
     */
    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
