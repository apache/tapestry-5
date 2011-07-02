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
if (!Tapestry.ZoneRefresh)
{
   Tapestry.ZoneRefresh = {};
}

Tapestry.Initializer.zoneRefresh = function(params)
{
   //  Ensure a valid period. Not required as PeriodicalUpdater already takes care of it
   // but will will skip unnecessary steps
   if(params.period <= 0)
   {
      return;   
   }
         
   // If the timer is already present, don't create a new one
   if (Tapestry.ZoneRefresh[params.id])
   {
      // Timer already in use
      return;
   }

   // Set zone
   var element = $(params.id);
   $T(element).zoneId = params.id;

   // Function to be called for each refresh
   var keepUpdatingZone = function(e)
   {
      try 
      {
         var zoneManager = Tapestry.findZoneManager(element);
         zoneManager.updateFromURL(params.URL);
      }
      catch(e)
      {
         e.stop();
         Tapestry.error(Tapestry.Messages.invocationException, {
            fname : "Tapestry.Initializer.zoneRefresh",
            params : params,
            exception : e
            });
      }
   };

   // Create and store the executor
   Tapestry.ZoneRefresh[params.id] = new PeriodicalExecuter(keepUpdatingZone, params.period);
};

// Before unload clear all the timers
Event.observe(window, "beforeunload", function()
{
   if (Tapestry.ZoneRefresh)
   {
      for ( var propertyName in Tapestry.ZoneRefresh)
      {
         var property = Tapestry.ZoneRefresh[propertyName];
         property.stop();
      }
   }
});
