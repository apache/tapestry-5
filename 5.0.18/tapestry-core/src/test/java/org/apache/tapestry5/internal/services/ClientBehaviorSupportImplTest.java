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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class ClientBehaviorSupportImplTest extends TapestryTestCase
{
    @Test
    public void no_changes()
    {
        RenderSupport support = mockRenderSupport();

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(support);

        setup.commit();

        verify();
    }

    @Test
    public void add_links()
    {
        Link link1 = mockLink("/link1");
        Link link2 = mockLink("/link2");
        RenderSupport support = mockRenderSupport();

        support.addInit("linkZone", new JSONArray("['client1', 'zone1', '/link1']"));
        support.addInit("linkZone", new JSONArray("['client2', 'zone2', '/link2']"));

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(support);

        setup.linkZone("client1", "zone1", link1);
        setup.linkZone("client2", "zone2", link2);

        setup.commit();

        verify();
    }

    private Link mockLink(String absoluteURI)
    {
        Link link = mockLink();

        expect(link.toAbsoluteURI()).andReturn(absoluteURI).atLeastOnce();

        return link;
    }

    @Test
    public void add_zones()
    {
        RenderSupport support = mockRenderSupport();

        support.addInit("zone", "client1");
        support.addInit("zone", "client2");

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(support);

        setup.addZone("client1", null, null);
        setup.addZone("client2", null, null);

        setup.commit();

        verify();
    }

    @Test
    public void zones_with_functions()
    {
        RenderSupport support = mockRenderSupport();

        support.addInit("zone", new JSONObject("{'element':'client1', 'show':'showme' }"));
        support.addInit("zone", new JSONObject("{'element':'client2', 'update':'updateme' }"));

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(support);

        setup.addZone("client1", "showme", null);
        setup.addZone("client2", null, "updateme");

        setup.commit();

        verify();
    }

    @Test
    public void zone_function_names_are_converted_to_lower_case()
    {
        RenderSupport support = mockRenderSupport();

        support.addInit("zone", new JSONObject("{'element':'client1', 'show':'showme' }"));
        support.addInit("zone", new JSONObject("{'element':'client2', 'update':'updateme' }"));

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(support);

        setup.addZone("client1", "ShowMe", null);
        setup.addZone("client2", null, "UpdateMe");

        setup.commit();

        verify();
    }
}
