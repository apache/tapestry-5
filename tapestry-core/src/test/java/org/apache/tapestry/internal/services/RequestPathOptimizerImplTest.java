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
        return new Object[][]{

                {"/context", "/foo/bar", "foo/baz", "baz"},

                {"/context", "/foo/bar", "foo//baz", "baz"},

                {"/context", "/foo//bar", "foo/baz", "baz"},

                {"", "/foo/bar", "foo/baz", "baz"},

                {"/reallylongcontexttoensureitisrelative", "/foo/bar/baz/biff", "gnip/gnop", "../../../gnip/gnop"},

                {"", "/foo/bar/baz/biff/yepthisissolongthatabsoluteurlisshorter/dude", "gnip/gnop", "/gnip/gnop"},

                {"", "/foo/bar", "/foo/bar/baz/bif", "bar/baz/bif"},

                {"", "/foo/bar/baz/bif", "foo", "/foo"},

                {"/ctx", "/foo/bar/baz/bif", "foo", "/ctx/foo"},

                {"/anotherobnoxiouslylongcontextthatiwllforcerelative", "/foo/bar/baz/bif", "foo", "../../../foo"}

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

        assertEquals(optimizer.optimizePath(expectedURI), expectedURI);

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
