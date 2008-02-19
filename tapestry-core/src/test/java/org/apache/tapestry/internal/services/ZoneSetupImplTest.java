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

import org.apache.tapestry.PageRenderSupport;
import static org.apache.tapestry.internal.services.ZoneSetupImpl.ZONE_INITIALIZER_STRING;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class ZoneSetupImplTest extends TapestryTestCase
{
    @Test
    public void no_changes()
    {
        PageRenderSupport support = mockPageRenderSupport();

        replay();

        ZoneSetupImpl setup = new ZoneSetupImpl(support);

        setup.writeInitializationScript();

        verify();
    }

    @Test
    public void add_links()
    {
        PageRenderSupport support = mockPageRenderSupport();

        JSONObject template = new JSONObject("{ zones: [], links: [['client1', 'zone1'], ['client2', 'zone2']] }");

        support.addScript(ZONE_INITIALIZER_STRING, template.getJSONArray("zones"), template.getJSONArray("links"));

        replay();

        ZoneSetupImpl setup = new ZoneSetupImpl(support);

        setup.linkZone("client1", "zone1");
        setup.linkZone("client2", "zone2");

        setup.writeInitializationScript();

        verify();
    }

    @Test
    public void add_zones()
    {
        PageRenderSupport support = mockPageRenderSupport();

        JSONObject template = new JSONObject("{ zones: [ {div:'client1'}, {div:'client2'} ], links:[] }");

        support.addScript(ZONE_INITIALIZER_STRING, template.getJSONArray("zones"), template.getJSONArray("links"));

        replay();

        ZoneSetupImpl setup = new ZoneSetupImpl(support);

        setup.addZone("client1", null, null);
        setup.addZone("client2", null, null);

        setup.writeInitializationScript();

        verify();
    }

    @Test
    public void zones_with_functions()
    {
        PageRenderSupport support = mockPageRenderSupport();


        JSONObject template = new JSONObject(
                "{ zones: [ {div:'client1', show:'showme'}, {div:'client2', update:'updateme'} ], links:[] }");

        support.addScript(ZONE_INITIALIZER_STRING, template.getJSONArray("zones"), template.getJSONArray("links"));

        replay();

        ZoneSetupImpl setup = new ZoneSetupImpl(support);

        setup.addZone("client1", "showme", null);
        setup.addZone("client2", null, "updateme");

        setup.writeInitializationScript();

        verify();
    }

    @Test
    public void zone_function_names_are_converted_to_lower_case()
    {
        PageRenderSupport support = mockPageRenderSupport();

        JSONObject template = new JSONObject(
                "{ zones: [ {div:'client1', show:'showme'}, {div:'client2', update:'updateme'} ], links:[] }");

        support.addScript(ZONE_INITIALIZER_STRING, template.getJSONArray("zones"), template.getJSONArray("links"));

        replay();

        ZoneSetupImpl setup = new ZoneSetupImpl(support);

        setup.addZone("client1", "ShowMe", null);
        setup.addZone("client2", null, "UpdateMe");

        setup.writeInitializationScript();

        verify();
    }
}
