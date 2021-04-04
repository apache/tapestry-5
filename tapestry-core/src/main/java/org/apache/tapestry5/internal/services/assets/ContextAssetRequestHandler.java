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
import org.apache.tapestry5.services.assets.AssetRequestHandler;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Handles requests for context assets, screening out attempt to
 * access anything under WEB-INF or META-INF.
 *
 * @since 5.2.0
 */
public class ContextAssetRequestHandler implements AssetRequestHandler
{
    private final ResourceStreamer resourceStreamer;

    private final Resource rootContextResource;

    private final Pattern illegal = Pattern.compile("^([\\\\/]*((web|meta)-inf.*)|(.*\\.tml$))", Pattern.CASE_INSENSITIVE);

    public ContextAssetRequestHandler(ResourceStreamer resourceStreamer, Resource rootContextResource)
    {
        this.resourceStreamer = resourceStreamer;
        this.rootContextResource = rootContextResource;
    }

    public boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException
    {
        ChecksumPath path = new ChecksumPath(resourceStreamer, null, extraPath);

        if (illegal.matcher(path.resourcePath).matches())
        {
            return false;
        }

        return path.stream(rootContextResource.forFile(path.resourcePath));
    }

}
