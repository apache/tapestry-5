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
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetRequestHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private final Map<String, AssetRequestHandler> configuration;

    private final String pathPrefix;

    public AssetDispatcher(Map<String, AssetRequestHandler> configuration,

                           @Symbol(SymbolConstants.APPLICATION_VERSION)
                           String applicationVersion,

                           @Symbol(SymbolConstants.APPLICATION_FOLDER) String applicationFolder)
    {
        this.configuration = configuration;

        String folder = applicationFolder.equals("") ? "" : "/" + applicationFolder;

        this.pathPrefix = folder + RequestConstants.ASSET_PATH_PREFIX + applicationVersion + "/";
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        // Remember that the request path does not include the context path, so we can simply start
        // looking for the asset path prefix right off the bat.

        if (!path.startsWith(pathPrefix))
            return false;

        String virtualPath = path.substring(pathPrefix.length());

        int slashx = virtualPath.indexOf('/');

        String virtualFolder = virtualPath.substring(0, slashx);

        AssetRequestHandler handler = configuration.get(virtualFolder);

        if (handler != null)
        {

            String extraPath = virtualPath.substring(slashx + 1);

            boolean handled = handler.handleAssetRequest(request, response, extraPath);

            if (handled)
                return true;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND, path);

        return true;
    }
}
