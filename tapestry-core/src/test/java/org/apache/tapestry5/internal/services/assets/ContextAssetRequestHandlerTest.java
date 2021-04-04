// Copyright 2010, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.test.ioc.TestBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

public class ContextAssetRequestHandlerTest extends TestBase
{
    @DataProvider
    public Object[][] invalid_paths()
    {
        return new Object[][]
                {
                        {"web-Inf/classes/hibernate.cfg.xml"},
                        {"Meta-Inf/MANIFEST.mf"},
                        {"Index.tml"},
                        {"folder/FolderIndex.TML"},
                        {"\\WEB-INF/something.jpg"},
                        {"\\//WEB-INF/something.jpg"},
                        {"//WEB-INF/something.jpg"},
                        {"//\\\\WEB-INF/something.jpg"}
                };
    }

    @Test(dataProvider = "invalid_paths")
    public void ensure_assets_are_rejected(String path) throws IOException
    {
        ContextAssetRequestHandler handler = new ContextAssetRequestHandler(null, null);

        assertFalse(handler.handleAssetRequest(null, null, "fake-checksum/" + path),
                "Handler should return false for invalid path.");
    }
}
