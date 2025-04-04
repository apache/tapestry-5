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

# ## t5/core/zone
#
# Provides a default handler for events related to zones. A zone is any kind of
# client-side element that can be updated; a zone will normally have a unique id.
# Typically, a client-side zone element is rendered by, and corresponds to, a server-side
# core/Zone component; however, certain other components (such as core/ProgressiveDisplay) may
# also be treated as zones.
#
# Most often, a zone is any element with attribute `data-container-type=zone` and corresponds
# to a core/Zone server-side component.
define ["t5/core/dom", "t5/core/events", "t5/core/ajax", "t5/core/console", "t5/core/forms",  "underscore"],

  (dom, events, ajax, console, forms, _) ->
  
    unless (typeof ajax) == "function"
      console.error "ajax variable is not a function, but instead it is " + JSON.stringify(ajax)
      console.error ajax
      throw new Error "ajax variable is not a function"

    # For a given element that may have the `data-update-zone` attribute, locates the
    # zone element. May return null if the zone can not be found (after logging an error
    # to the console).
    #
    # * element - starting point for determining zone
    findZone = (element) ->
      zoneId = element.attr "data-update-zone"

      if zoneId is "^"
        zone = element.findParent "[data-container-type=zone]"

        if zone is null
          throw new Error "Unable to locate containing zone for #{element}."

        return zone

      zone = dom zoneId

      if zone is null
        throw new Error "Unable to locate zone '#{zoneId}'."

      return zone

    dom.onDocument "click", "a[data-update-zone]", (event) ->

      element = @closest "[data-update-zone]"

      unless element
        throw new Error "Could not locate containing element with data-update-zone attribute."

      zone = findZone element

      if zone
        zone.trigger events.zone.refresh,  url: element.attr "href"

      event.nativeEvent.preventDefault()
      return

    dom.onDocument "submit", "form[data-update-zone]", ->

      zone = findZone this

      if zone
        formParameters = forms.gatherParameters this

        zone.trigger events.zone.refresh,
          url: (@attr "action")
          parameters: formParameters

      return false

    dom.onDocument "submit", "form[data-async-trigger]", ->

      formParameters = forms.gatherParameters this

      @addClass "ajax-update"

      ajax (@attr "action"),
        data: formParameters
        complete: => @removeClass "ajax-update"

      return false

    dom.onDocument events.zone.update, (event) ->

      @trigger events.zone.willUpdate

      content = event.memo.content

      # The server may have passed down the empty string for the content; that removes the existing content.
      # On the other hand, the server may have not provided a content key; in that case, content is undefined
      # which means to leave the existing content alone.
      #
      # Note that currently, the willUpdate and didUpdate events are triggered even when the zone is not actually
      # updated. That may be a bug.
      unless content is undefined
        @update content

      @trigger events.initializeComponents
      @trigger events.zone.didUpdate

    dom.onDocument events.zone.refresh, (event) ->

      # This event may be triggered on an element inside the zone, rather than on the zone itself. Scan upwards
      # to find the actual zone.
      zone = @closest "[data-container-type=zone]"

      # A Zone inside a form will render some additional parameters to coordinate updates with the Form on the server.
      attr = zone.attr "data-zone-parameters"

      parameters = attr and JSON.parse attr

      simpleIdParams = if zone.attr "data-simple-ids" then {"t:suppress-namespaced-ids": true}

      ajax event.memo.url,
        data: _.extend { "t:zoneid": zone.element.id }, simpleIdParams, parameters, event.memo.parameters
        success: (response) ->
          zone.trigger events.zone.update, content: response.json?.content

    dom.onDocument "click", "a[data-async-trigger]", (event)->
      link = @closest 'a[data-async-trigger]'

      link.addClass "ajax-update"

      ajax (link.attr "href"),
        complete: -> link.removeClass "ajax-update"

      event.nativeEvent.preventDefault()

      return

    # Locates a zone element by its unique id attribute, and (deferred, to a later event loop cycle),
    # performs a standard refresh of the zone. This is primarily used by the core/ProgressiveDisplay component.
    #
    # * id - client id of the element
    # * url - URL to use to refresh the element.
    deferredZoneUpdate = (id, url) ->

      _.defer ->
        zone = dom id

        if zone is null
          console.error "Could not locate element '#{id}' to update."
          return

        zone.trigger events.zone.refresh, { url }

    # Most of this module is document-level event handlers, but there's also some exports:
    return { deferredZoneUpdate, findZone }
