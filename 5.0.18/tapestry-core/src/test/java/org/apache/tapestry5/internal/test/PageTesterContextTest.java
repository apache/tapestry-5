// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PageTesterContextTest extends Assert
{
    @Test
    public void to_URL() throws IOException
    {
        PageTesterContext context = new PageTesterContext("src/test/app2");
        URL resource = context.getResource("/OpaqueResource.txt");
        InputStream stream = resource.openStream();
        stream.close();
    }

    @Test
    public void to_URL_no_file() throws IOException
    {
        PageTesterContext context = new PageTesterContext("src/test/app2");
        URL resource = context.getResource("/NonExisting.txt");
        assertNull(resource);
    }

    @Test
    public void to_URL_is_dir() throws IOException
    {
        PageTesterContext context = new PageTesterContext("src/test");
        URL resource = context.getResource("/app2");
        assertNull(resource);
    }
}
