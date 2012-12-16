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


# ##core/grid
#
# Adds support for in-place updates of the Grid component.  The Grid renders a
# div[data-zone] around the table, and code here intercepts clicks on links that
# are inside a div[data-inplace-grid-links].
#
define ["core/dom", "core/events", "core/console"],

  (dom, events, console) ->

    dom.onDocument "[data-inplace-grid-links] a", ->

      zone = this.findContainer "[data-container-type=zone]"

      unless zone
        console.error "Unable to find containing zone for live update of grid."
        return false

      zone.trigger events.zone.refresh, url: this.attribute "href"

      return false

    return null
