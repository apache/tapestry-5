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

# ##core/zone
#
# Provides a default handler for events related to zones. A zone is any kind of
# client-side element that can be updated; a zone will normally have a unique id.
# Typically, a client-side zone element is rendered by, and corresponds to, a server-side
# core/Zone component; however, certain other components (such as core/ProgressiveDisplay) may
# also be treated as zones.
define ["core/spi", "core/events", "core/ajax", "core/console", "_"],

  (spi, events, ajax, console, _) ->
    spi.onDocument events.zone.update, (event) ->

      this.trigger events.zone.willUpdate

      # TODO: purge existing children?

      content = event.memo.content

      # The server may have passed down the empty string for the content; that removes the existing content.
      # On the other hand, the server may have not provided a content key; in that case, content is undefined
      # which means to leave the existing content alone.
      #
      # Note that currently, the willUpdate and didUpdate events are triggered even when the zone is not actually
      # updated. That may be a bug.
      unless content is undefined
        this.update content

      this.trigger events.zone.didUpdate

    spi.onDocument events.zone.refresh, (event) ->

      # A Zone inside a form will render some additional parameters to coordinate updates with the Form on the server.
      parameters = this.getAttribute "data-zone-parameters"

      parameters = if parameters is null then null else JSON.parse(parameters)

      ajax event.memo.url,
        parameters: _.extend { "t:zoneid": this.element.id }, parameters, event.memo.parameters
        onsuccess: (reply) =>
          this.trigger events.zone.update, content: reply.responseJSON?.content

    # Locates a zone element by its unique id attribute, and (deferred, to a later event loop cycle),
    # performs a standard refresh of the zone. This is primarily used by the core/ProgressiveDisplay component.
    #
    # * id - client id of the element
    # * url - URL to use to refresh the element.
    deferredZoneUpdate = (id, url) ->

      _.defer ->
        zone = spi id

        if zone is null
          console.error "Could not locate element '#{id}' to update."
          return

        zone.trigger events.zone.refresh, { url }


    return { deferredZoneUpdate }