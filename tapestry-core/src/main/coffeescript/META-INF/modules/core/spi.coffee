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


# ##core/spi (Service Provider Interface)
#
# This is the core of the abstraction layer that allows the majority of components to operate without caring whether the
# underlying infrastructure framework is Prototype, jQuery, or something else.  This is the standard SPI, which wraps
# Prototype ... but does it in a way that makes it relatively easy to swap in jQuery instead.
#
# TODO: Define a dependency on "prototype" when that's exposed as a stub module.
define ["_", "core/console"], (_, console) ->

  # _internal_: splits the string into words separated by whitespace
  split = (str) ->
    _(str.split " ").reject (s) -> s is ""

  # _internal_: Converts content (provided to `ElementWrapper.update()` or `append()`) into an appropriate type. This
  # primarily exists to validate the value, and to "unpack" an ElementWrapper into a DOM element.
  convertContent = (content) ->
    if _.isString content
      return content

    if _.isElement content
      return content

    if content.constructor?.name is "ElementWrapper"
      return content.element

    throw new Error "Provided value <#{content}> is not valid as DOM element content."

    # Generic view of an DOM event that is passed to a handler function.
  #
  # Properties:
  #
  # * nativeEvent - the native Event object, which may provide additional information.
  # * memo - the object passed to `ElementWrapper.trigger()`.
  # * type - the name of the event that was triggered.
  # * char - the character value of the pressed key, if a printable character, as a string.
  # * key -The key value of the pressed key. This is the same as the `char` property for printable keys,
  #  or a key name for others.
  class EventWrapper

   constructor: (event) ->
      @nativeEvent = event
      @memo = event.memo
      @type = event.type

      @char = event.char
      @key = event.key

    # Stops the event which prevents further propagation of the DOM event,
    # as well as DOM event bubbling.
    stop: ->
      @nativeEvent.stop()

  # Value returned from `on()`; an EventHandler is used to stop listening to
  # events, or even temporarily pause listening.
  #
  # Registers the handler as an event listener for matching elements and event names.
  #
  # Note: it is possible to add handlers for events on the window object, but
  # `start()` and `stop()` do not do anything for such events.
  #
  # * elements - array of DOM elements
  # * eventNames - array of event names
  # * match - selector to match bubbled elements, or null
  # * handler - event handler function to invoke; it will be passed an EventWrapper instance
  class EventHandler
    constructor: (elements, eventNames, match, handler) ->
      throw new Error("No event handler was provided.") unless handler?

      wrapped = (prototypeEvent, matchedElement) ->
        # Set `this` to be the matched element (jQuery style), rather than
        # the element on which the event is observed.
        handler.call(matchedElement, new EventWrapper prototypeEvent)

      # Prototype Event.Handler instances
      @protoHandlers = []

      _.each elements, (element) =>
        _.each eventNames, (eventName) =>
          if element is window
            unless _.isEmpty match
              throw Error("Matching of elements by selector is not supported for window events.")
            Event.observe element, eventName, wrapped
          else
            @protoHandlers.push element.on eventName, match, wrapped

    # Invoked after `stop()` to restart event listening.
    #
    # Returns this EventHandler instance.
    start: ->
      _.each @protoHandlers, (h) -> h.start()

      this

    # Invoked to stop event listening. Listening can be re-instanted with `start()`.
    #
    # Returns this EventHandler instance.
    stop: ->
      _.each @protoHandlers, (h) -> h.stop()

      this

  # Wraps a DOM element, providing some common behaviors.
  # Exposes the original element as property `element`.
  class ElementWrapper

   constructor: (element) ->
      @element = $(element)

    # Hides the wrapped element, setting its display to 'none'.
    #
    # Returns this ElementWrapper.
    hide: ->
      @element.hide()
      this

    # Displays the wrapped element if hidden.
    #
    # Returns this ElementWrapper.
    show: ->
      @element.show()
      this

    # Removes the wrapped element from the DOM.  It can later be re-attached.
    #
    # Returns this ElementWrapper.
    remove: ->
      @element.remove()
      this

    # Returns the value of an attribute as a string, or null if the attribute
    # does not exist.
    getAttribute: (name) ->
      @element.readAttribute name

    # Set the value of the attribute to the given value.
    #
    # Returns this ElementWrapper.
    #
    # Note: Prototype has special support for values null, true, and false that may not be duplicated by other
    # implementations of the SPI.
    setAttribute: (name, value) ->
      # TODO: case where name is an object, i.e., multiple attributes in a single call.
      # Well, you can just do it, but its not guaranteed to work the same across
      # different SPIs.
      @element.writeAttribute name, value
      this

    # Returns true if the element has the indicated class name, false otherwise.
    hasClass: (name) ->
      @element.hasClassName name

    # Removes the class name from the element, then returns this ElementWrapper.
    removeClass: (name) ->
      @element.removeClassName name
      this

    # Adds the class name to the element, then returns this ElementWrapper.
    addClass: (name) ->
      @element.addClassName name
      this

    # Updates this element with new content, replacing any old content. The new content may be HTML text, or a DOM
    # element, or null (to remove the body of the element).
    #
    # Returns this ElementWrapper.
    update: (content) ->
      @element.update (convertContent content)
      this

    # Appends new content (Element or HTML markup string) to the element.
    #
    # Returns this ElementWrapper.
    append: (content) ->
      @element.insert bottom: (convertContent content)
      this

    # Finds the first child element that matches the CSS selector.
    #
    # Returns the ElementWrapper for the child element, or null if not found.
    find: (selector) ->
      match = @element.down selector

      # Prototype returns undefined if not found, we want to return null.
      if match
        new ElementWrapper match
      else
        return null

    # Returns an ElementWrapper for this element's containing element. The ElementWrapper is created lazily, and
    # cached. Returns null if this element has no parentNode (either because this element is the document object, or
    # because this element is not yet attached to the DOM).
    getContainer: ->
      unless @container
        return null unless element.parentNode
        @container = new ElementWrapper(element.parentNode)

      @container

    # Returns true if this element is visible, false otherwise. This does not check to see if all containers of the
    # element are visible.
    visible: ->
      @element.visible()

    # Fires a named event, passing an optional _memo_ object to event handler functions.
    #
    # Returns this ElementWrapper.
    #
    # eventName - name of event to trigger on the wrapped Element
    # memo - optional value assocated with the event; available as WrappedeEvent.memo in event handler functions
    trigger: (eventName, memo) ->
      throw new Error("Attempt to trigger event with null event name") unless eventName?

      @element.fire eventName, memo
      this

    # Adds an event handler for one or more events.
    #
    # events - one or more event names, separated by spaces
    # match - optional: CSS expression used as a filter; only events that bubble
    # up to the wrapped element from an originating element that matches the CSS expression
    # will invoke the handler.
    # handler - function invoked; the function is passed an EventWrapper object.
    #
    # Returns an EventHandler object, making it possible to turn event observation on or off.
    on: (events, match, handler) ->
      exports.on @element, events, match, handler

  parseSelectorToElements = (selector) ->
    if _.isString selector
      return $$ selector

    # Array is assumed to be array of DOM elements
    if _.isArray selector
      return selector

    # Assume its a single DOM element

    [selector]

  bodyWrapper = null

  # Performs an asynchronous Ajax request, invoking callbacks when it completes.
  # * options.method - "post", "get", etc., default: "post".
  #   Adds a "_method" parameter and uses "post" to handle "delete", etc.
  # * options.contentType - default "context "application/x-www-form-urlencoded"
  # * options.parameters - optional, additional key/value pairs
  # * options.onsuccess - handler to invoke on success. Passed the XMLHttpRequest transport object.
  #   Default does nothing.
  # * options.onfailure - handler to invoke on failure (server responds with a non-2xx code).
  #   Passed the response. Default will log and error to the console.
  # * options.onexception - handler to invoke when an exception occurs (often means the server is unavailable).
  #   Passed the exception. Default will log an error to the cnsole and throw the exception.
  # TODO: Clarify what the response object looks like and/or wrap the Prototype Ajax.Response object.
  # TODO: Define what the return value is, or return exports
  ajaxRequest = (url, options = {}) ->
    finalOptions =
      method: options.method or "post"
      contentType: options.contentType or "application/x-www-form-urlencoded"
      parameters: options.parameters or {}
      onException: (ajaxRequest, exception) ->
        if options.onexception
          options.onexception exception
        else
          console.error "Request to #{url} failed with #{exception}"
          throw exception

      onFailure: (response) ->
        if options.onfailure
          options.onfailure response
        else
          message = "Request to #{url} failed with status #{response.getStatus()}"
          text = response.getStatusText()
          if not _.isEmpty text
            message += " -- #{text}"

          console.error message + "."

      onSuccess: (response) ->

        # Prototype treats status == 0 as success, even though it may
        # indicate that the server didn't respond.
        if (not response.getStatus()) or (not response.request.success())
          finalOptions.onFailure(response)
          return

        # Tapestry 5.3 includes lots more exception catching ... that just got in the way
        # of identifying the source of problems.  That's been stripped out.
        # Still sorting out how this will all work, especially in terms
        # of the abstraction.
        options.onsuccess and options.onsuccess(response)

    new Ajax.Request(url, finalOptions)

  exports =
    ajaxRequest: ajaxRequest

    # Invokes the callback only once the DOM has finished loading all elements (other resources, such as images, may
    # still be in-transit). This is a safe time to search the DOM, modify attributes, and attach event handlers.
    # Returns this modules exports, for chained calls.
    domReady: (callback) ->
      document.observe "dom:loaded", callback

      exports

    # on() is used to add an event handler
    # * selector - CSS selector used to select elements to attach handler to; alternately,
    #   a single DOM element, or an array of DOM elements
    # * events - one or more event names, separated by spaces
    # * match - optional: CSS expression used as a filter; only events that bubble
    # * up to a selected element from an originating element that matches the CSS expression
    #   will invoke the handler.
    # * handler - function invoked; the function is passed an Event object.
    # Returns an EventHandler object, making it possible to turn event notifications on or off.
    on: (selector, events, match, handler) ->
      unless handler?
        handler = match
        match = null

      elements = parseSelectorToElements selector

      return new EventHandler(elements, (split events), match, handler)

    # Returns an ElementWrapper for the provided DOM element that includes key behaviors:
    # * element - a DOM element, or the window, or the unique id of a DOM element
    # Returns the ElementWrapper.
    wrap: (element) ->
      throw new Error("Attempt to wrap a null DOM element") unless element
      new ElementWrapper element

    # Returns a wrapped version of the document.body element. Care must be take to not invoke this function before the
    # body element exists; typically only after the DOM has loaded, such as a `domReady()` callback.
    body: -> bodyWrapper ?= (exports.wrap document.body)

    # Returns the current dimensions of the viewport. An object with keys `width` and `height` (in pixels) is returned.
    viewportDimensions: -> document.viewport.getDimensions()