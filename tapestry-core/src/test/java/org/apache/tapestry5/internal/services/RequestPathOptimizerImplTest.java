// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RequestPathOptimizerImplTest extends InternalBaseTestCase
{
    @DataProvider
    public Object[][] uri_optimization_data()
    {
        return new Object[][] { { "/context", "/foo/bar.png", "/context/foo/baz.png", "baz.png" },

                { "/context", "/foo/bar.gif", "/context/foo//baz.gif", "baz.gif" },

                { "/context", "/foo//bar.css", "/context/foo/baz.css", "baz.css" },

                { "", "/foo/bar.css", "/foo/baz.css", "baz.css" },

                { "/reallylongcontexttoensureitisrelative", "/foo/bar/baz/biff.gif",
                        "/reallylongcontexttoensureitisrelative/gnip/gnop.gif", "../../../gnip/gnop.gif" },

                { "", "/foo/bar/baz/biff/yepthisissolongthatabsoluteurlisshorter/dude", "/gnip/gnop",
                        "/gnip/gnop" },

                { "", "/foo/bar", "/foo/bar/baz/bif", "bar/baz/bif" },

                { "", "/foo/bar/baz/bif", "/foo", "/foo" },

                { "/ctx", "/foo/bar/baz/bif", "/ctx/foo", "/ctx/foo" },

                { "/anotherobnoxiouslylongcontextthatiwllforcerelative", "/foo/bar/baz/bif",
                        "/anotherobnoxiouslylongcontextthatiwllforcerelative/foo", "../../../foo" },

                // A couple of better examples, see TAPESTRY-2033

                { "/manager", "", "/manager/asset/foo.gif", "asset/foo.gif" },

                { "", "", "/asset/foo.gif", "asset/foo.gif" },

                { "", "/griddemo.grid.columns.sort/title", "/assets/default.css", "/assets/default.css" },

                { "/example", "/", "/example/assets/tapestry/default.css", "assets/tapestry/default.css" },

                { "/example", "/newaccount", "/example/assets/tapestry/default.css",
                        "assets/tapestry/default.css" },

                { "/verylongcontextname", "/style/app.css", "/verylongcontextname/asset/foo.gif",
                        "../asset/foo.gif" },

                { "", "/eventhandlerdemo.barney/one", "/eventhandlerdemo.clear/anything",
                        "/eventhandlerdemo.clear/anything" },

                { "/verylongcontextname", "/eventhandlerdemo.barney/one",
                        "/verylongcontextname/eventhandlerdemo.clear/anything",
                        "../eventhandlerdemo.clear/anything" },

                { "/verylongcontextname", "/page", "/verylongcontextname/page:sort/foo",
                        "./page:sort/foo" },

                { "", "/page", "/page:sort/foo", "/page:sort/foo" },

                // TAPESTRY-2046

                { "/attendance", "/view/sites", "/attendance/assets/tapestry/tapestry.js",
                        "../assets/tapestry/tapestry.js" },

                // TAPESTRY-2095

                { "", "/", "/component:event", "/component:event" },

                // TAPESTRY-2333

                { "", "/nested/actiondemo/", "/nested/actiondemo.actionlink/2", "../actiondemo.actionlink/2" },

                // Make sure the ./ prefix is added even when the relative path doesn't contain
                // a slash ... otherwise, invalid URL component:event (i.e., "component" protocol, not "http").

                { "/verylongcontextname", "/", "/verylongcontextname/component:event", "./component:event" },

                // Don't optimize away base64-data ('//').  TAPESTRY-2522

                { "/context", "/mypage/action1", "/context/start?t:state:client=Hasc//asc==",
                        "../start?t:state:client=Hasc//asc==" },
                { "/context", "/mypage/action1",
                        "/context/start?t:state:client=H4sIAAAAAAAAAE2OsUoDQRCGJ2rQIFgEwRewnthYWYmeEHKIEvMA4+242bA7u+6uMWeR1tIX8YWsre2srNwDkUz1D//Mx/f+Bf3nHQDopQgTHzVSoGbOmClwyrE9RSOZo5DFxHFpGk54YQ1LvuGYTMolXRm2app9JM1jF+zxhNuP76O33c+f1y3YrmG/8S54KadjlWFYL2hJI0uiR9McjeizGgYPHeSaHD/CGno17IVC+99XIWQYuMvidNcGznDYRSOUjZd0Li9sNMs6wkkzx3tWpLGo4cIl5wWVd2QEnVdsceOxQ8HfDAFWEQ46NezUsJInt1kWgf7trJpVv/V1i1c0AQAA",
                        "../start?t:state:client=H4sIAAAAAAAAAE2OsUoDQRCGJ2rQIFgEwRewnthYWYmeEHKIEvMA4+242bA7u+6uMWeR1tIX8YWsre2srNwDkUz1D//Mx/f+Bf3nHQDopQgTHzVSoGbOmClwyrE9RSOZo5DFxHFpGk54YQ1LvuGYTMolXRm2app9JM1jF+zxhNuP76O33c+f1y3YrmG/8S54KadjlWFYL2hJI0uiR9McjeizGgYPHeSaHD/CGno17IVC+99XIWQYuMvidNcGznDYRSOUjZd0Li9sNMs6wkkzx3tWpLGo4cIl5wWVd2QEnVdsceOxQ8HfDAFWEQ46NezUsJInt1kWgf7trJpVv/V1i1c0AQAA" },
        };
    }

    @Test(dataProvider = "uri_optimization_data")
    public void uri_optimization(String contextPath, String requestPath, String path, String expectedURI)
    {
        Request request = mockRequest();

        train_isXHR(request, false);

        train_getAttribute(request, InternalConstants.GENERATING_RENDERED_PAGE, null);
        train_getContextPath(request, contextPath);
        train_getPath(request, requestPath);

        replay();

        RequestPathOptimizer optimizer = new RequestPathOptimizerImpl(request, false);

        assertEquals(optimizer.optimizePath(path), expectedURI);

        verify();
    }

    @Test
    public void xhr_forces_absolute()
    {
        Request request = mockRequest();
        String path = "/some/path";

        train_isXHR(request, true);

        replay();

        RequestPathOptimizer optimizer = new RequestPathOptimizerImpl(request, false);

        assertSame(optimizer.optimizePath(path), path);

        verify();
    }

    @Test
    public void force_absolute_is_enforced()
    {
        Request request = mockRequest();
        String path = "/some/path";

        replay();

        RequestPathOptimizer optimizer = new RequestPathOptimizerImpl(request, true);

        assertSame(optimizer.optimizePath(path), path);

        verify();
    }

    @Test
    public void generating_to_document_forces_non_optimized()
    {
        Request request = mockRequest();
        String path = "/some/path";

        train_isXHR(request, false);
        train_getAttribute(request, InternalConstants.GENERATING_RENDERED_PAGE, true);

        replay();

        RequestPathOptimizer optimizer = new RequestPathOptimizerImpl(request, false);

        assertSame(optimizer.optimizePath(path), path);

        verify();
    }

    @Test
    public void simple_slash_is_not_optimized()
    {
        Request request = mockRequest();

        replay();

        RequestPathOptimizer optimizer = new RequestPathOptimizerImpl(request, false);

        assertSame(optimizer.optimizePath("/"), "/");

        verify();
    }

}
