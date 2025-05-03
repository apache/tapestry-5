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

/** 
 * ## t5/core/select
 * 
 * Provides a document event handler that triggers an update a zone when the value
 * of a select element within the zone changes.
 */

import events from "t5/core/events";
import dom from "t5/core/dom";
import zone from "t5/core/zone";

dom.onDocument("change", "select[data-update-zone]", function() {

  // @ts-ignore
  const containingZone = zone.findZone(this);

  if (containingZone) {
    containingZone.trigger(events.zone.refresh, {
      // @ts-ignore
      url: this.attr("data-update-url"),
      parameters: {
        // @ts-ignore
        "t:selectvalue": this.value()
      }
    }
    );
  }

});
