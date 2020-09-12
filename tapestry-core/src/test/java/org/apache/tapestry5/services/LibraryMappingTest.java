// Copyright 2010, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.test.ioc.TestBase;
import org.testng.annotations.Test;

public class LibraryMappingTest extends TestBase
{
    @Test
    public void valid_mapping()
    {
        LibraryMapping lm = new LibraryMapping("folder", "root.package");

        assertEquals(lm.getPathPrefix(), "folder");
        assertEquals(lm.getRootPackage(), "root.package");

        assertEquals(lm.toString(), "LibraryMapping[folder, root.package]");
    }

    @Test
    public void invalid_mapping()
    {
        try
        {
            new LibraryMapping("lib/", "root.package");
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Library names may not start with or end with a slash");
        }
    }
}
