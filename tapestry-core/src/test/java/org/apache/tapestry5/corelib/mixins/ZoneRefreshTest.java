// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class ZoneRefreshTest extends TapestryTestCase
{
   
   @Test
   public void check_add_javascript_calls_component_resources_to_create_url()
   {
      Object [] context = new Object[]{ "something", "somewhere" };
      ComponentResources resources = mockComponentResources();
      JavaScriptSupport javaScriptSupport = mockJavaScriptSupport();
      Link link = mockLink();
      
      Zone zone = mockZone();
      
      ZoneRefresh zoneRefresh = new ZoneRefresh(context, resources, javaScriptSupport, zone);
      
      expect(resources.createEventLink("zoneRefresh", context)).andReturn(link);
      expect(link.toAbsoluteURI()).andReturn("mylink");
      
      JSONObject params = new JSONObject();
      params.put("period", 0);
      params.put("id", zone.getClientId());
      params.put("URL", "mylink");
      javaScriptSupport.addInitializerCall(InitializationPriority.LATE, "zoneRefresh", params);
      
      replay();
      
      zoneRefresh.addJavaScript();
      
      verify();
   }

   private Zone mockZone()
   {
      return new Zone()
      {
         @Override
         public String getClientId()
         {
            return "zoneId";
         }
         
      };
   }

}
