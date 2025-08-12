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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.services.AssetDispatcher;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.EsShim;
import org.apache.tapestry5.services.javascript.EsShimManager;

/**
 * Handler contributed to {@link AssetDispatcher} with key "es-shims". 
 * It interprets the extra path as a module name and serves the ES shim
 * for it.
 *
 * @see EsShim
 */
public class EsShimDispatcher implements Dispatcher
{
    
    private final ResourceStreamer streamer;

    private final OperationTracker tracker;
    
    private final String requestPrefix;

    private final boolean compress;
    
    private Map<String, StreamableResource> shimMap;

    public EsShimDispatcher(EsShimManager esShimManager,
                            StreamableResourceSource streamableResourceSource,
                            ResourceChangeTracker resourceChangeTracker,
                            ResourceStreamer streamer,
                            OperationTracker tracker,
                            boolean compress)
    {
        this.streamer = streamer;
        this.tracker = tracker;
        this.compress = compress;
        
        final Map<String, Resource> shims = esShimManager.getShims();
        this.shimMap = new HashMap<>(shims.size());
        
        try
        {
            for (String moduleName : shims.keySet())
            {
                shimMap.put(moduleName, streamableResourceSource.getStreamableResource(
                        shims.get(moduleName), StreamableResourceProcessing.COMPRESSION_ENABLED, resourceChangeTracker));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        requestPrefix = esShimManager.getRequestPrefix(compress);
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        if (path.startsWith(requestPrefix))
        {
            String extraPath = path.substring(requestPrefix.length());

            if (!handleModuleRequest(extraPath, response))
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("No ES module shim for path '%s'.", extraPath));
            }

            return true;
        }

        return false;

    }
    
    private boolean handleModuleRequest(String extraPath, Response response) throws IOException
    {
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

        return tracker.perform(String.format("Streaming %s %s",
                compress ? "compressed module" : "module",
                moduleName), new IOOperation<Boolean>()
        {
            public Boolean perform() throws IOException
            {
                StreamableResource resource = shimMap.get(moduleName);

                if (resource != null)
                {
                    final String checksum = resource.getChecksum();
                    return streamer.streamResource(resource, checksum, ResourceStreamer.DEFAULT_OPTIONS);
                }

                return false;
            }
        });
    }

}
