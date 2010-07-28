// Copyright 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class ClientBehaviorSupportImplTest extends TapestryTestCase
{
    @Test
    public void no_changes()
    {
        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(null, null);

        setup.commit();
    }

    @Test
    public void add_links()
    {
        Link link1 = mockLink("/link1");
        JavaScriptSupport js = mockJavaScriptSupport();

        js.addInitializerCall("linkZone", new JSONObject("linkId", "client1", "zoneId", "zone1", "url", "/link1"));

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(js, null);

        setup.linkZone("client1", "zone1", link1);

        setup.commit();

        verify();
    }

    @Test
    public void add_zones()
    {
        JavaScriptSupport js = mockJavaScriptSupport();
        Environment environment = mockEnvironment();

        expect(environment.peek(FormSupport.class)).andReturn(null).atLeastOnce();

        js.addInitializerCall("zone", new JSONObject("element", "client1"));
        js.addInitializerCall("zone", new JSONObject("element", "client2"));

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(js, environment);

        setup.addZone("client1", null, null);
        setup.addZone("client2", null, null);

        verify();
    }

    @Test
    public void add_zone_inside_form()
    {
        JavaScriptSupport js = mockJavaScriptSupport();
        Environment environment = mockEnvironment();
        FormSupport fs = mockFormSupport();

        expect(environment.peek(FormSupport.class)).andReturn(fs).atLeastOnce();

        expect(fs.getClientId()).andReturn("myform");
        expect(fs.getFormComponentId()).andReturn("MyPage.myform");

        JSONObject expected = new JSONObject(String.format(
                "{element:'client1', parameters:{'%s': 'myform', '%s': 'MyPage.myform' }}",
                RequestConstants.FORM_CLIENTID_PARAMETER, RequestConstants.FORM_COMPONENTID_PARAMETER));

        js.addInitializerCall("zone", expected);

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(js, environment);

        setup.addZone("client1", null, null);

        verify();
    }

    @Test
    public void zones_with_functions()
    {
        JavaScriptSupport js = mockJavaScriptSupport();
        Environment environment = mockEnvironment();

        expect(environment.peek(FormSupport.class)).andReturn(null).atLeastOnce();

        js.addInitializerCall("zone", new JSONObject("{'element':'client1', 'show':'showme' }"));
        js.addInitializerCall("zone", new JSONObject("{'element':'client2', 'update':'updateme' }"));

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(js, environment);

        setup.addZone("client1", "showme", null);
        setup.addZone("client2", null, "updateme");

        verify();
    }

    @Test
    public void zone_function_names_are_converted_to_lower_case()
    {
        JavaScriptSupport js = mockJavaScriptSupport();
        Environment environment = mockEnvironment();

        expect(environment.peek(FormSupport.class)).andReturn(null).atLeastOnce();

        js.addInitializerCall("zone", new JSONObject("{'element':'client1', 'show':'showme' }"));
        js.addInitializerCall("zone", new JSONObject("{'element':'client2', 'update':'updateme' }"));

        replay();

        ClientBehaviorSupportImpl setup = new ClientBehaviorSupportImpl(js, environment);

        setup.addZone("client1", "ShowMe", null);
        setup.addZone("client2", null, "UpdateMe");

        verify();
    }
}
