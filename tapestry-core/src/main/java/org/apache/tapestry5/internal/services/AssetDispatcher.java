// Copyright 2006, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetRequestHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Recognizes requests where the path begins with "/asset/" and delivers the content therein as a bytestream. Also
 * handles requests that are simply polling for a change to the file.
 *
 * @see ResourceStreamer
 * @see ClasspathAssetAliasManager
 * @see AssetRequestHandler
 */
@UsesMappedConfiguration(AssetRequestHandler.class)
public class AssetDispatcher implements Dispatcher
{
    /**
     * Keyed on extended path name, which includes the pathPrefix first and a trailing slash.
     */
    private final Map<String, AssetRequestHandler> pathToHandler = CollectionFactory.newMap();

    /**
     * List of path prefixes in the pathToHandler, sorted be descending length.
     */
    private final List<String> assetPaths = CollectionFactory.newList();

    private final String pathPrefix;

    public AssetDispatcher(Map<String, AssetRequestHandler> configuration,

                           @Symbol(SymbolConstants.APPLICATION_VERSION)
                           String applicationVersion,

                           @Symbol(SymbolConstants.APPLICATION_FOLDER)
                           String applicationFolder,

                           @Symbol(SymbolConstants.ASSET_PATH_PREFIX)
                           String assetPathPrefix
                           )
    {
        String folder = applicationFolder.equals("") ? "" : "/" + applicationFolder;

        this.pathPrefix = folder + assetPathPrefix + applicationVersion + "/";

        for (String path : configuration.keySet())
        {
            String extendedPath = this.pathPrefix + path + "/";

            pathToHandler.put(extendedPath, configuration.get(path));

            assetPaths.add(extendedPath);
        }

        // Sort by descending length

        Collections.sort(assetPaths, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return o2.length() - o1.length();
            }
        });
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        // Remember that the request path does not include the context path, so we can simply start
        // looking for the asset path prefix right off the bat.

        if (!path.startsWith(pathPrefix))
        {
            return false;
        }

        for (String extendedPath : assetPaths)
        {

            if (path.startsWith(extendedPath))
            {
                AssetRequestHandler handler = pathToHandler.get(extendedPath);

                String extraPath = path.substring(extendedPath.length());

                boolean handled = handler.handleAssetRequest(request, response, extraPath);

                if (handled)
                {
                    return true;
                }
            }
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND, path);

        return true;
    }
}
