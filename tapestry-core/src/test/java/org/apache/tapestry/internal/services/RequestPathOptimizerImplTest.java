// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.Request;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RequestPathOptimizerImplTest extends InternalBaseTestCase
{
    @DataProvider(name = "uri_optimization")
    public Object[][] uri_optimization_data()
    {
        return new Object[][]{{"/context", "/foo/bar.png", "/context/foo/baz.png", "baz.png"},

                              {"/context", "/foo/bar.gif", "/context/foo//baz.gif", "baz.gif"},

                              {"/context", "/foo//bar.css", "/context/foo/baz.css", "baz.css"},

                              {"", "/foo/bar.css", "/foo/baz.css", "baz.css"},

                              {"/reallylongcontexttoensureitisrelative", "/foo/bar/baz/biff.gif",
                               "/reallylongcontexttoensureitisrelative/gnip/gnop.gif", "../../../gnip/gnop.gif"},

                              {"", "/foo/bar/baz/biff/yepthisissolongthatabsoluteurlisshorter/dude", "/gnip/gnop",
                               "/gnip/gnop"},

                              {"", "/foo/bar", "/foo/bar/baz/bif", "bar/baz/bif"},

                              {"", "/foo/bar/baz/bif", "/foo", "/foo"},

                              {"/ctx", "/foo/bar/baz/bif", "/ctx/foo", "/ctx/foo"},

                              {"/anotherobnoxiouslylongcontextthatiwllforcerelative", "/foo/bar/baz/bif",
                               "/anotherobnoxiouslylongcontextthatiwllforcerelative/foo", "../../../foo"},

                              // A couple of better examples, see TAPESTRY-2033

                              {"/manager", "", "/manager/asset/foo.gif", "asset/foo.gif"},

                              {"", "", "/asset/foo.gif", "asset/foo.gif"},

                              {"/verylongcontextname", "/style/app.css", "/verylongcontextname/asset/foo.gif",
                               "../asset/foo.gif"},

                              {"", "/eventhandlerdemo.barney/one", "/eventhandlerdemo.clear/anything",
                               "/eventhandlerdemo.clear/anything"},

                              {"/verylongcontextname", "/eventhandlerdemo.barney/one",
                               "/verylongcontextname/eventhandlerdemo.clear/anything",
                               "../eventhandlerdemo.clear/anything"}

        };
    }

    @Test(dataProvider = "uri_optimization")
    public void uri_optimization(String contextPath, String requestPath, String path, String expectedURI)
    {
        Request request = mockRequest();

        train_isXHR(request, false);

        train_getContextPath(request, contextPath);
        train_getPath(request, requestPath);

        replay();

        RequestPathOptimizer optimizer = new RequestPathOptimizerImpl(request, false);

        assertEquals(optimizer.optimizePath(path), expectedURI);

        verify();
    }

    @Test
    public void force_full_is_a_pass_through()
    {
        Request request = mockRequest();
        String path = "/some/path";

        train_isXHR(request, true);

        replay();

        RequestPathOptimizer optimizer = new RequestPathOptimizerImpl(request, false);

        assertSame(optimizer.optimizePath(path), path);

        verify();
    }

    private void train_isXHR(Request request, boolean isXHR)
    {
        expect(request.isXHR()).andReturn(isXHR).atLeastOnce();
    }
}
