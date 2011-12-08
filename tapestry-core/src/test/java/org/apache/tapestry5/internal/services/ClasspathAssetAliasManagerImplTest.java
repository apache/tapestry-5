// Copyright 2006, 2007, 2009, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.internal.services.assets.AssetPathConstructorImpl;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.BaseURLSource;
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
        Map<String, String> configuration = CollectionFactory.newMap();

        configuration.put("tapestry", "org/apache/tapestry5");
        configuration.put("tapestry-internal", "org/apache/tapestry5/internal");
        configuration.put("mylib", "com/example/mylib");

        return configuration;
    }

    @Test
    public void slash_not_allowed_as_alias()
    {
        Map<String, String> configuration = CollectionFactory.newMap();

        configuration.put("slash/at/end/", "com/myco/old/style/library");

        try
        {
            new ClasspathAssetAliasManagerImpl(null, configuration);
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Contribution of folder name 'slash/at/end/' is invalid as it may not start with or end with a slash");
        }
    }

    @Test
    public void get_mappings()
    {
        // Notice how all the trailing slashes (which are tolerated but not wanted)
        // have been removed.

        Map<String, String> expected = CollectionFactory.newCaseInsensitiveMap();

        expected.put("tapestry", "org/apache/tapestry5");
        expected.put("tapestry-internal", "org/apache/tapestry5/internal");
        expected.put("mylib", "com/example/mylib");

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(null, configuration());

        assertEquals(manager.getMappings(), expected);
    }

    @Test(dataProvider = "to_client_url_data")
    public void to_client_url(String resourcePath, String expectedClientURL)
    {
        Request request = mockRequest();

        BaseURLSource baseURLSource = newMock(BaseURLSource.class);

        train_getContextPath(request, "/ctx");

        replay();

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(
                new AssetPathConstructorImpl(request,
                baseURLSource, APP_VERSION, "", false, "/assets/"), configuration());

        String expectedPath = "/ctx/assets/" + APP_VERSION + "/" + expectedClientURL;
        assertEquals(manager.toClientURL(resourcePath), expectedPath);

        verify();
    }

    @Test
    public void failure_if_path_not_in_mapped_alias_folder()
    {
        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(null, configuration());

        try
        {
            manager.toClientURL("org/example/icons/flag.gif");
            unreachable();
        } catch (UnknownValueException ex)
        {
            assertMessageContains(ex, "Unable to create a client URL for classpath resource org/example/icons/flag.gif");

            assertListsEquals(ex.getAvailableValues().getValues(), "com/example/mylib", "org/apache/tapestry5",
                    "org/apache/tapestry5/internal");
        }
    }

    @DataProvider
    public Object[][] to_client_url_data()
    {
        return new Object[][]
                {
                        {"com/example/mylib/Foo.bar", "mylib/Foo.bar"},
                        {"com/example/mylib/nested/Foo.bar", "mylib/nested/Foo.bar"},
                        {"org/apache/tapestry5/internal/Foo.bar", "tapestry-internal/Foo.bar"},
                        {"org/apache/tapestry5/Foo.bar", "tapestry/Foo.bar"},};
    }

}
