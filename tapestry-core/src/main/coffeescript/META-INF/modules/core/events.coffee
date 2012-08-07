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

# This module defines logical names for all events that Tapestry-controlled elements
# trigger or listener for. Prototype requires that all custom events have a namespace prefix; jQuery appears to
# allow it without issue.
define [], ->
  zone: # Invoked on a zone element to force an update to its content. The event memo is the
  # new content (an Element, or a string containing HTML markup). A top-level handler
  # is responsible for the actual update; it fires an event on the element just before,
  # and just after, the content is changed.
    update: "t5:zone-update"

    # Triggered just before the content in a Zone will be updated.
    willUpdate: "t5:zone-will-update"

    # Triggered just afer the content in a Zone has updated. If the zone was not visible,
    # it is made visible after its content is changed, and before this event is triggered.
    didUpdate: "t5:zone-did-update"