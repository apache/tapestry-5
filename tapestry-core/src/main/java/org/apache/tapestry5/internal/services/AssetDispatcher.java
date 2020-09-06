// Copyright 2006-2013 The Apache Software Foundation
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
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.services.AssetRequestDispatcher;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.PathConstructor;
import org.apache.tapestry5.services.assets.AssetRequestHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Recognizes requests where the path begins with "/asset/" (actually, as defined by the
 * {@link SymbolConstants#ASSET_PATH_PREFIX} symbol), and delivers the content therein as a bytestream. Also
 * handles requests that are simply polling for a change to the file (including checking the ETag in the
 * request against {@linkplain org.apache.tapestry5.services.assets.StreamableResource#getChecksum() the asset's checksum}.
 *
 * @see ResourceStreamer
 * @see ClasspathAssetAliasManager
 * @see AssetRequestHandler
 */
@UsesMappedConfiguration(AssetRequestHandler.class)
@Marker(AssetRequestDispatcher.class)
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

    private final String requestPathPrefix;

    public AssetDispatcher(Map<String, AssetRequestHandler> configuration,

                           PathConstructor pathConstructor,

                           @Symbol(SymbolConstants.ASSET_PATH_PREFIX)
                           String assetPathPrefix)
    {
        requestPathPrefix = pathConstructor.constructDispatchPath(assetPathPrefix, "");

        for (String path : configuration.keySet())
        {
            AssetRequestHandler handler = configuration.get(path);

            addPath(requestPathPrefix, path, handler);
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

    private void addPath(String prefix, String path, AssetRequestHandler handler)
    {
        String extendedPath = buildPath(prefix, path);

        pathToHandler.put(extendedPath, handler);

        assetPaths.add(extendedPath);
    }

    private String buildPath(String prefix, String path)
    {
        // TODO: Not sure when path would be length 0!
        return path.length() == 0
                ? prefix
                : prefix + path + "/";
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        // Remember that the request path does not include the context path, so we can simply start
        // looking for the asset path prefix right off the bat.

        if (!path.startsWith(requestPathPrefix))
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
