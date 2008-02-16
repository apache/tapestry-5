// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.Asset;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class PartialRenderPageRenderSupportTest extends TapestryTestCase
{
    @Test
    public void allocate_ids()
    {
        PageRenderSupport prs = new PartialRenderPageRenderSupport("");

        assertEquals(prs.allocateClientId("foo"), "foo");
        assertEquals(prs.allocateClientId("foo"), "foo_0");
    }

    @Test
    public void allocate_ids_with_uid()
    {
        PageRenderSupport prs = new PartialRenderPageRenderSupport(":uid");

        assertEquals(prs.allocateClientId("foo"), "foo:uid");
        assertEquals(prs.allocateClientId("foo"), "foo:uid_0");

    }

    @Test
    public void add_links_do_nothing()
    {
        PageRenderSupport prs = new PartialRenderPageRenderSupport("");

        Asset asset = mockAsset();

        replay();

        prs.addScriptLink(asset);
        prs.addClasspathScriptLink("foo/bar.js");
        prs.addStylesheetLink(asset, null);

        verify();
    }

    @Test
    public void add_script_and_update()
    {
        PartialRenderPageRenderSupport prs = new PartialRenderPageRenderSupport("");

        JSONObject reply = new JSONObject();


        prs.update(reply);


        assertEquals(reply.toString(), "{}");


        prs.addScript("x = %d;", 10);

        prs.update(reply);

        assertEquals(reply.getString("script"), "x = 10;");
    }
}
