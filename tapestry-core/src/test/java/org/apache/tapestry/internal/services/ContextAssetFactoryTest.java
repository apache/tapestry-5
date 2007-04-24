// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry.Asset;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.Context;
import org.apache.tapestry.services.Request;
import org.testng.annotations.Test;

public class ContextAssetFactoryTest extends InternalBaseTestCase
{
    @Test
    public void root_resource()
    {
        Context context = newContext();
        Request request = newRequest();

        replay();

        AssetFactory factory = new ContextAssetFactory(request, context);

        assertEquals(factory.getRootResource().toString(), "context:/");

        verify();
    }

    @Test
    public void asset_client_URL()
    {
        Context context = newContext();
        Request request = newRequest();

        Resource r = new ContextResource(context, "foo/Bar.txt");

        train_getContextPath(request, "/context");

        replay();

        AssetFactory factory = new ContextAssetFactory(request, context);

        Asset asset = factory.createAsset(r);

        assertSame(asset.getResource(), r);
        assertEquals(asset.toClientURL(), "/context/foo/Bar.txt");
        assertEquals(asset.toString(), asset.toClientURL());

        verify();
    }

}
