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

import org.apache.tapestry5.internal.services.VirtualAssetStreamer;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetRequestHandler;

/**
 * Processes requests for virtual assets, passing off such requests to the {@link VirtualAssetStreamer} service.
 * 
 * @since 5.2.0
 */
public class VirtualAssetRequestHandler implements AssetRequestHandler
{
    private final VirtualAssetStreamer streamer;

    public VirtualAssetRequestHandler(VirtualAssetStreamer streamer)
    {
        this.streamer = streamer;
    }

    public boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException
    {
        // To make the virtual asset look like an ordinary file, a fake ".js" suffix is added, which is
        // stripped off here.

        int dotx = extraPath.lastIndexOf('.');

        String clientData = extraPath.substring(0, dotx);

        streamer.streamVirtualAsset(clientData);

        return true;
    }

}
