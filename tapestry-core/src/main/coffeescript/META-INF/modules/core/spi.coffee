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
define ["_", "prototype"], (_) ->

  domLoaded = false
  # When the document has loaded, convert `domReady` to just execute the callback immediately.
  $(document).observe "dom:loaded", ->
    domLoaded = true

  # _internal_: splits the string into words separated by whitespace
  split = (str) ->
    _(str.split " ").reject (s) -> s is ""

  # _internal_: Fires a native event; something that Prototype does not normally do.
  fireNativeEvent = (element, eventName) ->
    if document.createEventObject
      # IE support:
      event = document.createEventObject()
      element.fireEvent "on#{eventName}", event
    else
      # Everyone else:
      event = document.createEvent "HTMLEvents"
      event.initEvent eventName, true, true
      element.dispatchEvent event

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

  # _internal_: Currently don't want to rely on Scriptaculous, since our needs are pretty minor.
  animate = (element, styleName, initial, final, duration, callbacks) ->
    styles = {}
    range = final - initial
    initialTime = Date.now()
    first = true
    animator = ->
      elapsed = Date.now() - initialTime
      if elapsed >= duration
        styles[styleName] = final
        element.setStyle styles
        window.clearInterval timeoutID
        callbacks.oncomplete and callbacks.oncomplete()

      # TODO: Add an easein/easeout function

      newValue = initial + range * (elapsed / duration)

      element.setStyle styles

      if first
        callbacks.onstart and callbacks.onstart()
        first = false

    timeoutID = window.setInterval animator

    styles[styleName] = initial
    element.setStyle styles

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
  # * elements - array of DOM elements
  # * eventNames - array of event names
  # * match - selector to match bubbled elements, or null
  # * handler - event handler function to invoke; it will be passed an EventWrapper instance as the first parameter,
  #   and the memo as the second parameter. `this` will be the ElementWrapper for the matched element.
  class EventHandler
    constructor: (elements, eventNames, match, handler) ->
      throw new Error("No event handler was provided.") unless handler?

      wrapped = (prototypeEvent) ->
        # Set `this` to be the matched ElementWrapper, rather than the element on which the event is observed.
        elementWrapper = new ElementWrapper prototypeEvent.findElement()
        eventWrapper = new EventWrapper prototypeEvent

        handler.call elementWrapper, eventWrapper, eventWrapper.memo

      # Prototype Event.Handler instances
      @protoHandlers = []

      _.each elements, (element) =>
        _.each eventNames, (eventName) =>
            @protoHandlers.push Event.on element, eventName, match, wrapped

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

    # Passed the DOM Element
    constructor: (@element) ->

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

    # Removes the named attribute, if present.
    #
    # Returns this ElementWrapper
    removeAttribute: (name) ->

      @element.writeAttribute name, null
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

    # Appends new content (Element, ElementWrapper, or HTML markup string) to the body of the element.
    #
    # Returns this ElementWrapper.
    append: (content) ->
      @element.insert bottom: (convertContent content)
      this

    # Prepends new content (Element, ElementWrapper, or HTML markup string) to the body of the element.
    #
    # Returns this ElementWrapper
    prepend: (content) ->
      @element.insert top: (convertContent content)
      this

    # Inserts new content (Element, ElementWrapper, or HTML markup string) into the DOM immediately before
    # this ElementWrapper's element.
    #
    # Returns this ElementWrapper
    insertBefore: (content) ->
      @element.insert before: (convertContent content)
      this

    # Runs an animation to fade-in the element over the specified duration. The element may be hidden (via `hide()`)
    # initially, and will be made visible (with initial opacity 0, which will increase over time) when the animation
    # starts.
    #
    # * duration - animation duration time, in seconds
    # * callback - function invoked after the animation is complete
    #
    # Returns this ElementWrapper
    fadeIn: (duration, callback) ->
      animate @element, "opacity", 0, 1, duration * 1000,
        onstart: => @element.show()
        oncomplete: callback

      this

    # Runs an animation to fade out an element over the specified duration. The element should already
    # be visible and fully opaque.
    #
    # * duration - animation duration time, in seconds
    # * callback - function invoked after the animation is complete
    #
    # Returns this ElementWrapper
    fadeOut: (duration, callback) ->
      animate @element, "opacity", 1, 0, duration * 1000,
        oncomplete: callback

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

    # Finds all child elements matching the CSS selector, returning them
    # as an array of ElementWrappers.
    findAll: (selector) ->
      matches = @element.select selector

      _.map matches, (e) -> new ElementWrapper e

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

    # Fires a named event, passing an optional _memo_ object to event handler functions. This must support
    # common native events (exact list TBD), as well as native events (in Prototype, native events must have
    # a prefix that ends with a colon).
    #
    # * eventName - name of event to trigger on the wrapped Element
    # * memo - optional value assocated with the event; available as WrappedeEvent.memo in event handler functions (must
    # be null for native events)
    #
    # Returns this ElementWrapper.
    trigger: (eventName, memo) ->
      throw new Error("Attempt to trigger event with null event name") unless eventName?

      if (eventName.indexOf ':') > 0
        # Custom event is supported directly by Prototype:
        @element.fire eventName, memo
      else
        # Native events take some extra work:
        throw new Error("Memo must be null when triggering a native event") if memo

        fireNativeEvent @element, eventName

      this

    # Returns the current value of the element (which must be a form control element, such as `<input>` or
    # `<textarea>`).
    # TODO: Define behavior for multi-named elements, such as `<select>`.

    getValue: ->
      @element.getValue()

    # Updates the value for the element (whichmust be a form control element).
    #
    # Returns this ElementWrapper
    setValue: (newValue) ->
      @element.setValue newValue

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
  #
  # This is very low level; most code will want to go through the `core/ajax` module instead,
  # which adds better handling of exceptions and failures, and handles Tapestry's
  #
  # * options.method - "post", "get", etc., default: "post".
  #   Adds a "_method" parameter and uses "post" to handle "delete", etc.
  # * options.contentType - default "context "application/x-www-form-urlencoded"
  # * options.parameters - optional, additional key/value pairs
  # * options.onsuccess - handler to invoke on success. Passed the XMLHttpRequest transport object.
  #   Default does nothing.
  # * options.onfailure - handler to invoke on failure (server responds with a non-2xx code).
  #   Passed the response. Default will throw the exception
  # * options.onexception - handler to invoke when an exception occurs (often means the server is unavailable).
  #   Passed the exception. Default will generate an exception message and throw an `Error`.
  #
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
          throw exception

      onFailure: (response) ->
        if options.onfailure
          options.onfailure response
        else
          message = "Request to #{url} failed with status #{response.getStatus()}"
          text = response.getStatusText()
          if not _.isEmpty text
            message += " -- #{text}"
          message += "."

          if options.onfailure
            options.onfailure response, message
          else
            throw new Error(message)

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
    # Returns this modules exports, for chained calls. If the DOM has already loaded, the callback is invoked
    # immediately.
    domReady: (callback) ->
      if domLoaded
        callback()
      else
        $(document).observe "dom:loaded", callback

      exports

    # on() is used to add an event handler
    #
    # * selector - CSS selector used to select elements to attach handler to; alternately,
    #   a single DOM element, or an array of DOM elements
    # * events - one or more event names, separated by spaces
    # * match - optional: CSS expression used as a filter; only events that bubble
    # * up to a selected element from an originating element that matches the CSS expression
    #   will invoke the handler.
    # * handler - function invoked; the function is passed an Event object.
    #
    # Returns an EventHandler object, making it possible to turn event notifications on or off.
    on: (selector, events, match, handler) ->
      unless handler?
        handler = match
        match = null

      elements = parseSelectorToElements selector

      return new EventHandler(elements, (split events), match, handler)

    # Returns an ElementWrapper for the provided DOM element that includes key behaviors:
    #
    # * element - a DOM element, or the window, or the unique id of a DOM element
    #
    # Returns the ElementWrapper, or null if no DOM element with the given id exists.
    wrap: (element) ->
      if _.isString element
        element = $ element
        return null unless element
      else
        throw new Error("Attempt to wrap a null DOM element") unless element

      new ElementWrapper element

    # Returns a wrapped version of the document.body element. Care must be take to not invoke this function before the
    # body element exists; typically only after the DOM has loaded, such as a `domReady()` callback.
    body: -> bodyWrapper ?= (exports.wrap document.body)

    # Returns the current dimensions of the viewport. An object with keys `width` and `height` (in pixels) is returned.
    viewportDimensions: -> document.viewport.getDimensions()