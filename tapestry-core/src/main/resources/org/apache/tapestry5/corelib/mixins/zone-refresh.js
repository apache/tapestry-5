// 
// Copyright 2011 The Apache Software Foundation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//   http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
Tapestry.ZONE_REFRESH_EVENT = "tapestry:zonerefresh";

Tapestry.Initializer.zoneRefresh = function(params)
{
   var zoneRefreshId = params.id + "_refresh";
   
   // This will prevent any more instantiation of  PeriodicalExecuter
   if($(zoneRefreshId))
   {
      return;
   }
   
   // Create a new element and place it at the end of the document. Then we use
   // it for refreshing the zone
   var zoneRefresh = document.createElement("div");
   zoneRefresh.id = zoneRefreshId;
   
   // Link zoneRefresh element to zone
   $T(zoneRefresh).zoneId = params.id;
   document.body.appendChild(zoneRefresh);
   
   // Connect event to zone
   Tapestry.Initializer.updateZoneOnEvent(Tapestry.ZONE_REFRESH_EVENT, zoneRefresh, params.id, params.URL);

   //Create timer
   var timer = new PeriodicalExecuter(function(e)
   {
      zoneRefresh.fire(Tapestry.ZONE_REFRESH_EVENT);
   }, params.period);

   
   //Clear the timer before unload
   Event.observe(window, "beforeunload", function()
   {
      if(timer)
      {
         timer.stop();   
      }
   })
};


