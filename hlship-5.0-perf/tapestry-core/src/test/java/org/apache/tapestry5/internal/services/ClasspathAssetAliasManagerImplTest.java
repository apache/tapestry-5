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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class ClasspathAssetAliasManagerImplTest extends InternalBaseTestCase
{

    public Map<String, String> configuration()
    {
        Map<String, String> configuration = newMap();

        configuration.put("tapestry/", "org/apache/tapestry5/");
        configuration.put("tapestry-internal/", "org/apache/tapestry5/internal/");
        configuration.put("mylib/", "com/example/mylib/");

        return configuration;
    }

    private static final String OPTIMIZED = "/opt/path";

    @Test(dataProvider = "to_client_url_data")
    public void to_client_url(String resourcePath, String expectedClientURL)
    {
        Request request = mockRequest();
        RequestPathOptimizer optimizer = mockRequestPathOptimizer();

        train_getContextPath(request, "/ctx");

        train_optimizePath(optimizer, "/ctx" + RequestConstants.ASSET_PATH_PREFIX + expectedClientURL, OPTIMIZED);


        replay();

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(request, optimizer, configuration());

        assertEquals(manager.toClientURL(resourcePath), OPTIMIZED);

        verify();
    }

    @DataProvider(name = "to_client_url_data")
    public Object[][] to_client_url_data()
    {
        return new Object[][] { { "foo/bar/Baz.txt", "foo/bar/Baz.txt" },
                { "com/example/mylib/Foo.bar", "mylib/Foo.bar" },
                { "com/example/mylib/nested/Foo.bar", "mylib/nested/Foo.bar" },
                { "org/apache/tapestry5/internal/Foo.bar", "tapestry-internal/Foo.bar" },
                { "org/apache/tapestry5/Foo.bar", "tapestry/Foo.bar" }, };
    }

    @Test(dataProvider = "to_resource_path_data")
    public void to_resource_path(String clientURL, String expectedResourcePath)
    {
        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(null, null, configuration());

        assertEquals(manager.toResourcePath(clientURL), expectedResourcePath);
    }

    @DataProvider(name = "to_resource_path_data")
    public Object[][] to_resource_path_data()
    {
        Object[][] data = to_client_url_data();

        for (Object[] pair : data)
        {
            Object buffer = pair[0];
            pair[0] = RequestConstants.ASSET_PATH_PREFIX + pair[1];
            pair[1] = buffer;
        }

        return data;
    }
}
