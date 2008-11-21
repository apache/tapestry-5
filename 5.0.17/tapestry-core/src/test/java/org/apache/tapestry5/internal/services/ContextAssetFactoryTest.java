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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.Test;

public class ContextAssetFactoryTest extends InternalBaseTestCase
{

    @Test
    public void root_resource()
    {
        Context context = mockContext();
        Request request = mockRequest();

        replay();

        AssetFactory factory = new ContextAssetFactory(request, context, null);

        assertEquals(factory.getRootResource().toString(), "context:/");

        verify();
    }

    @Test
    public void asset_client_URL()
    {
        Context context = mockContext();
        Request request = mockRequest();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();

        Resource r = new ContextResource(context, "foo/Bar.txt");

        train_getContextPath(request, "/context");

        train_optimizePath(optimizer, "/context/foo/Bar.txt", "/opt/path1");
        train_optimizePath(optimizer, "/context/foo/Bar.txt", "/opt/path2");

        replay();

        AssetFactory factory = new ContextAssetFactory(request, context, optimizer);

        Asset asset = factory.createAsset(r);

        assertSame(asset.getResource(), r);
        assertEquals(asset.toClientURL(), "/opt/path1");

        // In real life, toString() is the same as toClientURL(), but we're testing
        // that the optimize method is getting called, basically.

        assertEquals(asset.toString(), "/opt/path2");

        verify();
    }

}
