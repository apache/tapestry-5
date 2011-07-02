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

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.testng.annotations.Test;

public class ZoneRefreshTest extends SeleniumTestCase
{
   @Test
   public void test_if_zone_with_void_event_handler_works() throws Exception
   {
      openBaseURL();
      clickAndWait("link=Zone Refresh Demo");
      checkZoneValues("zone", 3);
   }
   
   @Test
   public void test_if_zone_with_single_zone_event_handler_works() throws Exception
   {
      openBaseURL();
      clickAndWait("link=Zone Refresh Demo");
      checkZoneValues("zone2", 3);
   }
   
   @Test
   public void test_if_zone_with_multiple_zone_event_handler_works() throws Exception
   {
      openBaseURL();
      clickAndWait("link=Zone Refresh Demo");
      checkZoneValues("zone4", 3);
   }
   

   private void checkZoneValues(String zone, int times) throws Exception
   {
      Thread.sleep(300);
      for(int i = 0; i < times; ++i)
      {
         assertText(zone, String.valueOf(i));
         Thread.sleep(1000);
      }
   }

}
