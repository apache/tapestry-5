// Copyright 2006-2014 The Apache Software Foundation
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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.AssetAlias;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class ClasspathAssetAliasManagerImplTest extends InternalBaseTestCase
{
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
            new ClasspathAssetAliasManagerImpl(configuration);
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

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(configuration());

        assertEquals(manager.getMappings(), expected);
    }

    @Test(dataProvider = "to_client_url_data")
    public void to_client_url(String resourcePath, String expectedFolder, String expectedPath) throws IOException
    {
        Resource r = mockResource();

        expect(r.getPath()).andReturn(resourcePath);

        replay();

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(configuration());

        AssetAlias alias = manager.extractAssetAlias(r);

        assertEquals(alias.virtualFolder, expectedFolder);
        assertEquals(alias.path, expectedPath);

        verify();
    }

    @Test
    public void can_not_map_resource_path_that_matches_virtual_folder() throws IOException
    {
        Resource r = mockResource();

        expect(r.getPath()).andReturn("com/example/mylib");

        replay();

        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(configuration());

        try
        {
            manager.extractAssetAlias(r);

            unreachable();
        } catch (IllegalArgumentException ex)
        {

        }

        verify();

    }

    @Test
    public void failure_if_path_not_in_mapped_alias_folder()
    {
        ClasspathAssetAliasManager manager = new ClasspathAssetAliasManagerImpl(configuration());
        Resource resource = mockResource();

        expect(resource.getPath()).andReturn("org/example/icons/flag.gif").atLeastOnce();

        replay();

        try
        {
            manager.extractAssetAlias(resource);
            unreachable();
        } catch (UnknownValueException ex)
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
                        {"com/example/mylib/Foo.bar", "mylib", "Foo.bar"},
                        {"com/example/mylib/nested/Foo.bar", "mylib", "nested/Foo.bar"},
                        {"org/apache/tapestry5/internal/Foo.bar", "tapestry-internal", "Foo.bar"},
                        {"org/apache/tapestry5/Foo.bar", "tapestry", "Foo.bar"},
                };
    }

}
