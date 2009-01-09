// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.test.TapestryTestConstants;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class PageTesterContext implements Context
{
    private final File contextRoot;

    public PageTesterContext(String contextRoot)
    {
        this.contextRoot = new File(TapestryTestConstants.MODULE_BASE_DIR, contextRoot);
    }

    public String getInitParameter(String name)
    {
        return null;
    }

    public URL getResource(String path)
    {
        File f = new File(contextRoot + path);

        if (!f.exists() || !f.isFile())
        {
            return null;
        }
        try
        {
            return f.toURL();
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public List<String> getResourcePaths(String path)
    {
        throw new UnsupportedOperationException("getResourcePaths() is not supported for ContextForPageTester.");
    }

    public Object getAttribute(String name)
    {
        throw new UnsupportedOperationException("getAttribute() is not supported for ContextForPageTester.");
    }

    public List<String> getAttributeNames()
    {
        return Collections.emptyList();
    }

    public String getMimeType(String file)
    {
        return null;
    }

    /**
     * Always returns null.
     */
    public File getRealFile(String path)
    {
        return null;
    }
}
