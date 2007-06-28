// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.eq;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.ClasspathResource;
import org.apache.tapestry.services.ClasspathAssetAliasManager;
import org.apache.tapestry.services.Dispatcher;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.testng.annotations.Test;

public class AssetDispatcherTest extends InternalBaseTestCase
{
    private static final String SMILEY_CLIENT_URL = "/assets/app1/pages/smiley.png";

    private static final String SMILEY_PATH = "org/apache/tapestry/integration/app1/pages/smiley.png";

    private static final Resource SMILEY = new ClasspathResource(SMILEY_PATH);

    @Test
    public void not_an_asset_request() throws Exception
    {
        Request request = mockRequest();

        train_getPath(request, "/foo/bar/Baz.gif");

        replay();

        Dispatcher d = new AssetDispatcher(null, null, null);

        assertFalse(d.dispatch(request, null));

        verify();
    }

    @Test
    public void unprotected_asset() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();
        ResourceCache cache = mockResourceCache();
        ResourceStreamer streamer = mockResourceStreamer();

        train_getPath(request, SMILEY_CLIENT_URL);

        train_toResourcePath(aliasManager, SMILEY_CLIENT_URL, SMILEY_PATH);

        train_requiresDigest(cache, SMILEY, false);

        train_getDateHeader(request, AssetDispatcher.IF_MODIFIED_SINCE_HEADER, -1);

        streamer.streamResource(SMILEY);

        replay();

        Dispatcher d = new AssetDispatcher(streamer, aliasManager, cache);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    @Test
    public void protected_asset_without_an_extension() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();
        ResourceCache cache = mockResourceCache();
        ResourceStreamer streamer = mockResourceStreamer();

        String clientURL = "/assets/app1/pages/smiley_png";
        String resourcePath = "org/apache/tapestry/integration/app1/pages/smiley_png";

        train_getPath(request, clientURL);

        train_toResourcePath(aliasManager, clientURL, resourcePath);

        train_requiresDigest(cache, new ClasspathResource(resourcePath), true);

        response.sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains(resourcePath));

        replay();

        Dispatcher d = new AssetDispatcher(streamer, aliasManager, cache);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    @Test
    public void protected_asset_with_incorrect_digest_in_url() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();
        ResourceCache cache = mockResourceCache();
        ResourceStreamer streamer = mockResourceStreamer();

        String clientURL = "/assets/app1/pages/smiley.WRONG.png";
        String resourcePath = "org/apache/tapestry/integration/app1/pages/smiley.WRONG.png";

        train_getPath(request, clientURL);

        train_toResourcePath(aliasManager, clientURL, resourcePath);

        train_requiresDigest(cache, new ClasspathResource(resourcePath), true);

        train_getDigest(cache, SMILEY, "RIGHT");

        response.sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains(SMILEY_PATH));

        replay();

        Dispatcher d = new AssetDispatcher(streamer, aliasManager, cache);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    @Test
    public void protected_asset_wth_correct_digest_in_url() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();
        ResourceCache cache = mockResourceCache();
        ResourceStreamer streamer = mockResourceStreamer();

        String clientURL = TapestryConstants.ASSET_PATH_PREFIX + "app1/pages/smiley.RIGHT.png";
        String resourcePath = "org/apache/tapestry/integration/app1/pages/smiley.RIGHT.png";

        train_getPath(request, clientURL);

        train_toResourcePath(aliasManager, clientURL, resourcePath);

        train_requiresDigest(cache, new ClasspathResource(resourcePath), true);

        train_getDigest(cache, SMILEY, "RIGHT");

        train_getDateHeader(request, AssetDispatcher.IF_MODIFIED_SINCE_HEADER, -1);

        streamer.streamResource(SMILEY);

        replay();

        Dispatcher d = new AssetDispatcher(streamer, aliasManager, cache);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    protected final void train_getDigest(ResourceCache cache, Resource resource, String digest)
    {
        expect(cache.getDigest(resource)).andReturn(digest).atLeastOnce();
    }

    @Test
    public void protected_asset_without_digest() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();
        ResourceCache cache = mockResourceCache();
        ResourceStreamer streamer = mockResourceStreamer();

        train_getPath(request, SMILEY_CLIENT_URL);

        train_toResourcePath(aliasManager, SMILEY_CLIENT_URL, SMILEY_PATH);

        train_requiresDigest(cache, SMILEY, true);

        response.sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains(SMILEY_PATH));

        replay();

        Dispatcher d = new AssetDispatcher(streamer, aliasManager, cache);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    @Test
    public void client_cache_upto_date() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();
        ResourceCache cache = mockResourceCache();
        ResourceStreamer streamer = mockResourceStreamer();
        long now = System.currentTimeMillis();

        train_getPath(request, SMILEY_CLIENT_URL);

        train_toResourcePath(aliasManager, SMILEY_CLIENT_URL, SMILEY_PATH);

        train_requiresDigest(cache, SMILEY, false);

        train_getDateHeader(request, AssetDispatcher.IF_MODIFIED_SINCE_HEADER, now);

        train_getTimeModified(cache, SMILEY, now - 1000);

        response.sendError(HttpServletResponse.SC_NOT_MODIFIED, "");

        replay();

        Dispatcher d = new AssetDispatcher(streamer, aliasManager, cache);

        assertTrue(d.dispatch(request, response));

        verify();
    }

    @Test
    public void client_cache_out_of_date() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ClasspathAssetAliasManager aliasManager = mockClasspathAssetAliasManager();
        ResourceCache cache = mockResourceCache();
        ResourceStreamer streamer = mockResourceStreamer();
        long now = System.currentTimeMillis();

        train_getPath(request, SMILEY_CLIENT_URL);

        train_toResourcePath(aliasManager, SMILEY_CLIENT_URL, SMILEY_PATH);

        train_requiresDigest(cache, SMILEY, false);

        train_getDateHeader(request, AssetDispatcher.IF_MODIFIED_SINCE_HEADER, now - 1000);

        train_getTimeModified(cache, SMILEY, now + 1000);

        streamer.streamResource(SMILEY);

        replay();

        Dispatcher d = new AssetDispatcher(streamer, aliasManager, cache);

        assertTrue(d.dispatch(request, response));

        verify();
    }
}
