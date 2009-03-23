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

import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import java.io.IOException;

public class VirtualAssetDispatcher implements Dispatcher
{
    private static final String PATH_PREFIX = RequestConstants.ASSET_PATH_PREFIX + RequestConstants.VIRTUAL_FOLDER;

    private final VirtualAssetStreamer streamer;

    public VirtualAssetDispatcher(VirtualAssetStreamer streamer)
    {
        this.streamer = streamer;
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        if (!path.startsWith(PATH_PREFIX))
            return false;

        // PATH_PREFIX includes the slash.

        String fileName = path.substring(PATH_PREFIX.length());

        int dotx = fileName.lastIndexOf('.');

        String clientData = fileName.substring(0, dotx);

        streamer.streamVirtualAsset(clientData);

        return true;
    }

}
