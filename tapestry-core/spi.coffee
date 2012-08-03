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

# Service Provider Interface
# This is the core of the abstraction layer that allows the majority of components to operate
# without caring whether the underlying infrastructure framework is Prototype, jQuery, or something else.
# This is the standard SPI, which wraps Prototype ... but does it in a way that makes it relatively
# easy to swap in jQuery instead.

# TODO: The dependency on prototype is temporary while prototype/scriptaculous is part of the core stack.
# Either it will be broken out (likely), or something else will change. This will likely not work
# with JavaScript aggregation enabled.

define ["_", "prototype"], (_) ->

  split = (str) ->
    _(str.split " ").reject (s) -> s is ""

  # Generic view of an Event that is passed to a handler function.
  #
  class Event

    constructor: (@prototypeEvent) ->

    # Stops the event which prevents further propagation of the event,
    # as well as event bubbling.
    stop: ->
      @prototypeEvent.stop()

  # Value returned from on(); an EventHandler is used to stop listening to
  # events, or even pause listening.
  class EventHandler

    # Registers the handler as an event listener for matching elements and event names.
    # elements - array of DOM elements
    # eventNames - array of event names
    # match - selector to match bubbled elements, or null
    # handler - event handler function to invoke; it will be passed an Event instance
    constructor: (elements, eventNames, match, handler) ->

      wrapped = (prototypeEvent, matchedElement) ->
        # Set "this" to be the matched element (jQuery style), rather than
        # the element on which the event is observed.
        handler.call(matchedElement, new Event prototypeEvent)

      # Prototype Event.Handler instances
      @protoHandlers = []

      _.each elements, (element) =>
        _.each eventName, (eventName) =>
          @protoHandlers.push element.on event, match, wrapped

     # Invoked after stop() to restart event listening. Returns this EventHandler instance.
     start: ->

       _.each @protoHandlers, (h) -> h.start()

       this

    # Invoked to stop or pause event listening. Returns this EventHandler instance.
    stop: ->

      _.each @protoHandlers, (h) -> h.stop()

      this

  exports =

    # on() is used to add an event handler
    # selector - CSS selector used to select elements to attach handler to
    # events - one or more event names, separated by spaces
    # match - optional: filters the descendents of the matched elements; the event is
    # only triggered for matching element
    # handler - function invoked; the function is passed an Event object.
    # Returns an EventHandler object, with methods start() and stop().
    on: (selector, events, match, handler) ->

      if handler is null
        handler = match
        match = null

      elements = $$ selector

      return new EventHandler(elements, split events, match, handler)
