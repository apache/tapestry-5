// Copyright 2012, 2013 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.AssetDispatcher;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.PathConstructor;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.javascript.ModuleManager;

import java.io.IOException;

/**
 * Handler contributed to {@link AssetDispatcher} with key "modules". It interprets the extra path as a module name,
 * and searches for the corresponding JavaScript module.
 */
public class ModuleDispatcher implements Dispatcher
{
    private final ModuleManager moduleManager;

    private final ResourceStreamer streamer;

    private final OperationTracker tracker;

    private final String prefix;

    public ModuleDispatcher(ModuleManager moduleManager,
                            ResourceStreamer streamer,
                            PathConstructor pathConstructor,
                            @Symbol(SymbolConstants.APPLICATION_VERSION)
                            String applicationVersion,
                            OperationTracker tracker)
    {
        this.moduleManager = moduleManager;
        this.streamer = streamer;
        this.tracker = tracker;

        prefix = pathConstructor.constructDispatchPath("modules", applicationVersion, "");
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String requestPath = request.getPath();

        if (!requestPath.startsWith(prefix))
        {
            return false;
        }

        String extraPath = requestPath.substring(prefix.length());

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

        return tracker.perform(String.format("Streaming module %s", extraPath), new IOOperation<Boolean>()
        {
            public Boolean perform() throws IOException
            {
                Resource resource = moduleManager.findResourceForModule(moduleName);

                if (resource != null)
                {
                    streamer.streamResource(resource);

                    return true;
                }

                return false;
            }
        });
    }
}
