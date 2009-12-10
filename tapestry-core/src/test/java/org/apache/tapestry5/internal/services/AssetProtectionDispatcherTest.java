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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.internal.services.AssetProtectionDispatcher;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.AssetPathAuthorizer;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.slf4j.Logger;

public class AssetProtectionDispatcherTest extends Assert
{

    @Test
    public void ignores_nonassets() throws IOException
    {
        //shouldn't need any configuration here...
        List<AssetPathAuthorizer> auths = Collections.emptyList();
        Logger logger = createMock(Logger.class);
        AssetProtectionDispatcher disp = new AssetProtectionDispatcher(auths,null,logger);
        Request request = createMock(Request.class);
        expect(request.getPath()).andReturn("start");
        Response response = createMock(Response.class);
        replay(request,response,logger);
        assertFalse(disp.dispatch(request, response));
        verify(request,response,logger);
    }
    
    @Test
    public void checks_authorizers() throws IOException
    {
        Logger logger = createMock(Logger.class);
        List<AssetPathAuthorizer> auths = new ArrayList<AssetPathAuthorizer>();
        AssetPathAuthorizer auth = createMock(AssetPathAuthorizer.class);

        expect(auth.order()).andReturn(Arrays.asList(AssetPathAuthorizer.Order.ALLOW, AssetPathAuthorizer.Order.DENY)).times(2);

        expect(auth.accessAllowed("/cayenne.xml")).andReturn(false);
        expect(auth.accessDenied("/cayenne.xml")).andReturn(true);
        expect(auth.accessAllowed("/org/apache/tapestry/default.css")).andReturn(true);
        auths.add(auth);

        logger.debug("Denying access to /cayenne.xml");
        logger.debug("Allowing access to /org/apache/tapestry/default.css");

        Request request = createMock(Request.class);
        Response response = createMock(Response.class);
        expect(request.getPath()).andReturn(RequestConstants.ASSET_PATH_PREFIX + "/cayenne.xml");
        expect(request.getPath()).andReturn(RequestConstants.ASSET_PATH_PREFIX + "/org/apache/tapestry/default.css");
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "/cayenne.xml");
        
        ClasspathAssetAliasManager manager = createMock(ClasspathAssetAliasManager.class);
        expect(manager.toResourcePath(RequestConstants.ASSET_PATH_PREFIX + "/cayenne.xml")).andReturn("/cayenne.xml");
        expect(manager.toResourcePath(
                RequestConstants.ASSET_PATH_PREFIX + "/org/apache/tapestry/default.css"))
            .andReturn("/org/apache/tapestry/default.css");
        replay(auth,request,response,manager,logger);
        AssetProtectionDispatcher disp = new AssetProtectionDispatcher(auths,manager,logger);
        
        assertTrue(disp.dispatch(request,response));
        assertFalse(disp.dispatch(request, response));
        verify(auth,request,response,logger);
    }
}
