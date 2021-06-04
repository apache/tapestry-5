# Copyright 2012 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# ## t5/core/zone-refresh
define ["t5/core/events", "t5/core/dom", "t5/core/console"],
  (events, dom, console) ->

    # Initialize a timer for the zone at the specified period (in seconds). The zone will be
    # refreshed with the provided URL.
    initialize = (zoneId, period, url) ->
      zone = dom zoneId

      unless zone
        console.err "Zone #{zoneId} not found for periodic refresh."
        return

      # Only one periodic refresh per zone.
      return if zone.meta "periodic-refresh"

      zone.meta "periodic-refresh", true

      executing = false

      # Whenever the zone updates, we can clear the executing flag.

      zone.on events.zone.didUpdate, ->
        executing = false
        return

      cleanUp = ->
        window.clearInterval intervalId
        zone = null
        return

      handler = ->
        # Don't clog things up if the response rate is too slow
        return if executing

        # If the zone element is no longer part of the DOM, stop the
        # timer

        unless (zone.closest 'body')
          cleanUp()
          return

        # Set the flag now, it will clear when the zone updates.
        executing = true

        zone.trigger events.zone.refresh, { url }

      intervalId = window.setInterval handler, period * 1000

      # Not sure if this is needed except for IE:
      (dom window).on "beforeunload", cleanUp

    # export the single function:
    return initialize
