// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import org.apache.tapestry5.internal.IOOperation;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.AssetDispatcher;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.javascript.ModuleManager;

import java.io.IOException;

/**
 * Handler contributed to {@link AssetDispatcher} with key "module-root". It interprets the extra path as a module name,
 * and searches for the corresponding JavaScript module.
 */
public class ModuleAssetRequestHandler implements AssetRequestHandler
{
    private final ModuleManager moduleManager;

    private final ResourceStreamer streamer;

    private final OperationTracker tracker;

    public ModuleAssetRequestHandler(ModuleManager moduleManager, ResourceStreamer streamer, OperationTracker tracker)
    {
        this.moduleManager = moduleManager;
        this.streamer = streamer;
        this.tracker = tracker;
    }

    @Override
    public boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException
    {
        // Ensure request ends with '.js'.  That's the extension tacked on by RequireJS because it expects there
        // to be a hierarchy of static JavaScript files here.

        int dotx = extraPath.lastIndexOf('.');

        if (dotx < 0)
        {
            return false;
        }

        if (!extraPath.substring(dotx + 1).equals("js"))
        {
            return false;
        }

        final String moduleName = extraPath.substring(0, dotx);

        final Holder<Boolean> handledHolder = Holder.create(false);

        TapestryInternalUtils.performIO(tracker, String.format("Streaming module %s", extraPath), new IOOperation()
        {
            public void perform() throws IOException
            {
                Resource resource = moduleManager.findResourceForModule(moduleName);

                if (resource != null)
                {
                    streamer.streamResource(resource);

                    handledHolder.put(true);
                }
            }
        });

        return handledHolder.get();
    }
}
