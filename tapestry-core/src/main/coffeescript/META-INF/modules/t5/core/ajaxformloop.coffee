# Copyright 2012, 2013 The Apache Software Foundation
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

# ## t5/core/ajaxformloop
#
# Provides handlers related to the core/AjaxFormLoop component (as well as core/AddRowLink and
# core/RemoveRowLink).
define ["./dom", "./events", "./console", "./ajax", "./builder"],
  (dom, events, console, ajax, builder) ->

    # "afl" is short for "AjaxFormLoop".
    AFL_SELECTOR = "[data-container-type=core/AjaxFormLoop]"
    FRAGMENT_TYPE = "core/ajaxformloop-fragment"

    dom.onDocument "click", "#{AFL_SELECTOR} [data-afl-behavior=remove]", ->

      afl = this.findContainer AFL_SELECTOR

      unless afl
        console.error "Enclosing element for AjaxFormLoop remove row link not found."
        return false

      url = afl.attribute "data-remove-row-url"

      ajax url,
        parameters:
          "t:rowvalue": this.attribute "data-afl-row-value"
        onsuccess: =>
          # The server has removed the row from persistent storage, lets
          # do the same on the UI.

          fragment = this.findContainer "[data-container-type=#{FRAGMENT_TYPE}]"

          # TODO: Fire some before & after events, to allow for animation.

          # The fragment takes with it the hidden fields that control form submission
          # for its portion of the form.
          fragment.remove()

      return false

    dom.onDocument "click", "#{AFL_SELECTOR} [data-afl-behavior=insert-before] [data-afl-trigger=add]", ->

      afl = this.findContainer AFL_SELECTOR

      insertionPoint = this.findContainer "[data-afl-behavior=insert-before]"

      url = afl.attribute "data-inject-row-url"

      ajax url,
        onsuccess: (response) =>
          content = response.json?.content

          # Create a new element with the same type (usually "div") and class as this element.
          # It will contain the new content.
          newElement = builder insertionPoint.element.tagName,
              class: insertionPoint.element.className,
              "data-container-type": FRAGMENT_TYPE

          newElement.update content

          insertionPoint.insertBefore newElement

          # Trigger this event, to inform the world that the zone-like new element has been updated
          # with content.
          newElement.trigger events.zone.didUpdate

      return false

    # This module is all event handlers, and no exported functions.
    return null