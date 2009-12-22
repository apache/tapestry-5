// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

/**
 * Dispatcher that handles whether to allow or deny access to particular 
 * assets. Actual work of authorizing a particular url is handled by
 * implementations of AssetPathAuthorizer. Configuration is an ordered
 * list of AssetPathAuthorizers.  Each authorizer specifies an order of
 * operations as a list (see AssetPathAuthorizer.Order).
 *
 */
public class AssetProtectionDispatcher implements Dispatcher
{
    
    private final Collection<AssetPathAuthorizer> authorizers;
    private final ClasspathAssetAliasManager assetAliasManager;
    private final Logger logger;
    
    public AssetProtectionDispatcher(
            List<AssetPathAuthorizer> auths,
            ClasspathAssetAliasManager manager,
            Logger logger)
    {
        this.authorizers = Collections.unmodifiableList(auths);
        this.assetAliasManager = manager;
        this.logger = logger;
    }

    public boolean dispatch(Request request, Response response)
            throws IOException
    {
        String path = request.getPath();
        //we only protect assets, and don't examine any other url's.
        if (!path.startsWith(RequestConstants.ASSET_PATH_PREFIX))
        {
            return false;
        }
        String resourcePath = assetAliasManager.toResourcePath(path);
        for(AssetPathAuthorizer auth : authorizers)
        {
            for(AssetPathAuthorizer.Order o : auth.order())
            {
                if (o == AssetPathAuthorizer.Order.ALLOW)
                {
                    if (auth.accessAllowed(resourcePath))
                    {
                        logger.debug("Allowing access to " + resourcePath);
                        return false;
                    }
                }
                else
                {
                    if (auth.accessDenied(resourcePath))
                    {
                        logger.debug("Denying access to " + resourcePath);
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,resourcePath);
                        return true;
                    }
                }
            }
        }
        //if we get here, no Authorizer had anything useful to say about the resourcePath.
        //so let it fall through.
        logger.debug("Fell through the list of authorizers. Allowing access to: " + resourcePath);
        return false;
    }

}
