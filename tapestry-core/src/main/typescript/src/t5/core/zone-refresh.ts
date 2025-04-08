// Copyright 2012, 2025 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// ## t5/core/zone-refresh

import events from "t5/core/events";
import dom from "t5/core/dom";
import console from "t5/core/console";

// Initialize a timer for the zone at the specified period (in seconds). The zone will be
// refreshed with the provided URL.
const initialize = function(zoneId, period, url) {
  let zone = dom(zoneId);

  if (!zone) {
    console.err(`Zone ${zoneId} not found for periodic refresh.`);
    return;
  }

  // Only one periodic refresh per zone.
  if (zone.meta("periodic-refresh")) { return; }

  zone.meta("periodic-refresh", true);

  let executing = false;

  // Whenever the zone updates, we can clear the executing flag.

  zone.on(events.zone.didUpdate, function() {
    executing = false;
  });

  const cleanUp = function() {
    window.clearInterval(intervalId);
    zone = null;
  };

  const handler = function() {
    // Don't clog things up if the response rate is too slow
    if (executing) { return; }

    // If the zone element is no longer part of the DOM, stop the
    // timer

    if (!zone.closest('body')) {
      cleanUp();
      return;
    }

    // Set the flag now, it will clear when the zone updates.
    executing = true;

    return zone.trigger(events.zone.refresh, { url });
  };

  var intervalId = window.setInterval(handler, period * 1000);

  // Not sure if this is needed except for IE:
  return (dom(window)).on("beforeunload", cleanUp);
};

    // export the single function:
export default initialize;
