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
package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;

public class ZoneRefreshDemo
{
   @Persist
   @Property
   private int counter;
   
   @Persist
   @Property
   private int counter2;
   
   @Persist
   @Property
   private int counter3;
   
   @InjectComponent
   private Zone zone2;
   
   @InjectComponent
   private Zone zone3;
   
   @InjectComponent
   private Zone zone4;
   
   void setupRender()
   {
      counter = 0;
      counter2 = 0;
      counter3 = 0;
   }
   
   void onRefreshFromZone()
   {
      counter++;
   }
   
   Zone onRefreshFromZone2()
   {
      counter2++;
      return zone2;
   }
   
   Object onRefreshFromZone3(){
      counter3++;
      return new MultiZoneUpdate(zone3).add(zone4);
   }

}

