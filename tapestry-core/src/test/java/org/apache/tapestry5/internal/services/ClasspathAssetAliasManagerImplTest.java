// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.util.UnknownValueException;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class ClasspathAssetAliasManagerImplTest extends InternalBaseTestCase
{
    private static final String APP_VERSION = "1.2.3";

    public Map<String, String> configuration()
    {
        Map<String, String> configuration = newMap();

        configuration.put("tapestry/4.0", "org/apache/tapestry5/");
        configuration.put("tapestry-internal/3.0", "org/apache/tapestry5/internal/");
        configuration.put("mylib/2.0/", "com/example/mylib/");

        return configuration;
    }

    @Test(dataProvider = "to_client_url_data")
    public void to_client_url(String resourcePath, String expectedClientURL)
    {
        Request request = mockRequest();

        train_getContextPath(request, "/ctx");

        replay();

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(request, APP_VERSION, configuration());

        String expectedPath = "/ctx" + RequestConstants.ASSET_PATH_PREFIX + APP_VERSION + "/" + expectedClientURL;

        assertEquals(manager.toClientURL(resourcePath), expectedPath);

        verify();
    }

    @Test
    public void failure_if_path_not_in_mapped_alias_folder()
    {
        Request request = mockRequest();

        train_getContextPath(request, "");

        replay();

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(request, APP_VERSION, configuration());

        try
        {
            manager.toClientURL("org/example/icons/flag.gif");
            unreachable();
        }
        catch (UnknownValueException ex)
        {
            assertMessageContains(ex, "Unable to create a client URL for classpath resource org/example/icons/flag.gif");

            assertListsEquals(ex.getAvailableValues().getValues(), "com/example/mylib", "org/apache/tapestry5",
                    "org/apache/tapestry5/internal");
        }

        verify();
    }

    @DataProvider
    public Object[][] to_client_url_data()
    {
        return new Object[][]
        {
        { "com/example/mylib/Foo.bar", "mylib/2.0/Foo.bar" },
        { "com/example/mylib/nested/Foo.bar", "mylib/2.0/nested/Foo.bar" },
        { "org/apache/tapestry5/internal/Foo.bar", "tapestry-internal/3.0/Foo.bar" },
        { "org/apache/tapestry5/Foo.bar", "tapestry/4.0/Foo.bar" }, };
    }

    @Test(dataProvider = "to_resource_path_data")
    public void to_resource_path(String clientURL, String expectedResourcePath)
    {
        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(null, APP_VERSION, configuration());

        assertEquals(manager.toResourcePath(clientURL), expectedResourcePath);
    }

    @DataProvider
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
