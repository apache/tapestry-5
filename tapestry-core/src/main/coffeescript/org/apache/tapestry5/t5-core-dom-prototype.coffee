# Copyright 2012-2013 The Apache Software Foundation
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


# ## t5/core/dom
#
# This is the abstraction layer that allows the majority of components to operate without caring whether the
# underlying infrastructure framework is Prototype, jQuery, or something else.  This implementation is specific
# to Prototype, but Tapestry can be adapted to any infrastreucture framework by re-implementing this module.
#
# The abstraction layer has a number of disadvantages:
#
# * It adds a number of layers of wrapper around the infrastructure framework objects
# * It is leaky; some behaviors will vary slightly based on the active infrastructure framework
# * The abstraction is alien to both Prototype and jQuery developers; it mixes some ideas from both
# * It is much less powerful or expressive than either infrastructure framework used directly
#
# It is quite concievable that some components will require direct access to the infrastructure framework, especially
# those that are wrappers around third party libraries or plugins; however many simple components may need no more than
# the abstract layer and gain the valuable benefit of not caring about the infrastructure framework.
define ["_", "./utils", "prototype"], (_, utils) ->

  # Save a local reference to Prototype.$ ... see notes about some challenges using Prototype, jQuery,
  # and RequireJS together, here: https://github.com/jrburke/requirejs/issues/534
  $ = window.$

  # Fires a native event; something that Prototype does not normally do.
  # Returns true if the event completed normally, false if it was canceled.
  fireNativeEvent = (element, eventName) ->
    if document.createEventObject
      # IE support:
      event = document.createEventObject()
      return element.fireEvent "on#{eventName}", event

    # Everyone else:
    event = document.createEvent "HTMLEvents"
    event.initEvent eventName, true, true
    element.dispatchEvent event
    return not event.defaultPrevented

  # Converts content (provided to `ElementWrapper.update()` or `append()`) into an appropriate type. This
  # primarily exists to validate the value, and to "unpack" an ElementWrapper into a DOM element.
  convertContent = (content) ->
    if _.isString content
      return content

    if _.isElement content
      return content

    if content.constructor?.name is "ElementWrapper"
      return content.element

    throw new Error "Provided value <#{content}> is not valid as DOM element content."

  # Currently don't want to rely on Scriptaculous, since our needs are pretty minor.
  animate = (element, styleName, initialValue, finalValue, duration, callbacks) ->
    styles = {}
    range = finalValue - initialValue
    initialTime = Date.now()
    first = true
    animator = ->
      elapsed = Date.now() - initialTime
      if elapsed >= duration
        styles[styleName] = finalValue
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

    styles[styleName] = initialValue
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

      # This is to satisfy YUICompressor which doesn't seem to like 'char', even
      # though it doesn't appear to be a reserved word.
      this[name] = event[name] for name in ["memo", "type", "char", "key"]

    # Stops the event which prevents further propagation of the DOM event,
    # as well as DOM event bubbling.
    stop: ->
      @nativeEvent.stop()

  # Interface between the dom's event model, and Prototype's.
  #
  # * elements - array of DOM elements (or the document object)
  # * eventNames - array of event names
  # * match - selector to match bubbled elements, or null
  # * handler - event handler function to invoke; it will be passed an `EventWrapper` instance as the first parameter,
  #   and the memo as the second parameter. `this` will be the `ElementWrapper` for the matched element.
  #
  # Event handlers may return false to stop event propogation; this prevents an event from bubbling up, and
  # prevents any browser default behavior from triggering.  This is often easier than accepting the `EventWrapper`
  # object as the first parameter and invoking `stop()`.

  onevent = (elements, eventNames, match, handler) ->
      throw new Error "No event handler was provided." unless handler?

      wrapped = (prototypeEvent) ->
        # Set `this` to be the matched ElementWrapper, rather than the element on which the event is observed
        # (which is often further up the hierarchy).
        elementWrapper = new ElementWrapper prototypeEvent.findElement()
        eventWrapper = new EventWrapper prototypeEvent

        result = handler.call elementWrapper, eventWrapper, eventWrapper.memo

        # If an event handler returns exactly false, then stop the event.
        if result is false
          prototypeEvent.stop()

        return

      for element in elements
        for eventName in eventNames
          Event.on element, eventName, match, wrapped

      return

  # Wraps a DOM element, providing some common behaviors.
  # Exposes the original element as property `element`.
  class ElementWrapper

  # Passed the DOM Element
    constructor: (@element) ->

      # Hides the wrapped element, setting its display to 'none'.
    hide: ->
      @element.hide()

      return this

    # Displays the wrapped element if hidden.
    show: ->
      @element.show()

      return this

    # Removes the wrapped element from the DOM.  It can later be re-attached.
    remove: ->
      @element.remove()

      return this

    # Reads or updates an attribute. With one argument, returns the current value
    # of the attribute. With two arguments, updates the attribute's value, and returns
    # the previous value. Setting an attribute to null is the same as removing it.
    #
    # Alternately, the first attribute can be an object in which case all the keys
    # and values of the object are applied as attributes, and this `ElementWrapper` is returned.
    #
    # * name - the attribute to read or update, or an object of keys and values
    # * value - (optional) the new value for the attribute
    attribute: (name, value) ->

      if _.isObject name
        for name, value of name
            @element.writeAttribute name, value
        return this

      current = @element.readAttribute name
      if arguments.length > 1
        @element.writeAttribute name, value

      return current

    # Moves the cursor to the field.
    focus: ->
      @element.focus()

      return this

    # Returns true if the element has the indicated class name, false otherwise.
    hasClass: (name) ->
      @element.hasClassName name

    # Removes the class name from the element.
    removeClass: (name) ->
      @element.removeClassName name

      return this

    # Adds the class name to the element.
    addClass: (name) ->
      @element.addClassName name

      return this

    # Updates this element with new content, replacing any old content. The new content may be HTML text, or a DOM
    # element, or null (to remove the body of the element). Passing null will remove all content.
    update: (content) ->
      @element.update (content and convertContent content)

      return this

    # Appends new content (Element, ElementWrapper, or HTML markup string) to the body of the element.
    append: (content) ->
      @element.insert bottom: (convertContent content)

      return this

    # Prepends new content (Element, ElementWrapper, or HTML markup string) to the body of the element.
    prepend: (content) ->
      @element.insert top: (convertContent content)

      return this

    # Inserts new content (Element, ElementWrapper, or HTML markup string) into the DOM immediately before
    # this ElementWrapper's element.
    insertBefore: (content) ->
      @element.insert before: (convertContent content)

      return this

    # Inserts new content (Element, ElementWrapper, or HTML markup string) into the DOM immediately after
    # this ElementWrapper's element.
    insertAfter: (content) ->
      @element.insert after: (convertContent content)

      return this

    # Runs an animation to fade-in the element over the specified duration. The element may be hidden (via `hide()`)
    # initially, and will be made visible (with initial opacity 0, which will increase over time) when the animation
    # starts.
    #
    # * duration - animation duration time, in seconds
    # * callback - function invoked after the animation is complete
    fadeIn: (duration, callback) ->
      animate @element, "opacity", 0, 1, duration * 1000,
        onstart: => @element.show()
        oncomplete: callback

      return this

    # Runs an animation to fade out an element over the specified duration. The element should already
    # be visible and fully opaque.
    #
    # * duration - animation duration time, in seconds
    # * callback - function invoked after the animation is complete
    fadeOut: (duration, callback) ->
      animate @element, "opacity", 1, 0, duration * 1000,
        oncomplete: callback

      return this

    # Finds the first child element that matches the CSS selector, wrapped as an ElementWrapper.
    # Returns null if not found.
    findFirst: (selector) ->
      match = @element.down selector

      # Prototype returns undefined if not found, we want to return null.
      if match
        new ElementWrapper match
      else
        return null

    # Finds _all_ child elements matching the CSS selector, returning them
    # as an array of ElementWrappers.
    find: (selector) ->
      matches = @element.select selector

      _.map matches, (e) -> new ElementWrapper e

    # Find the first container element that matches the selector (wrapped as an ElementWrapper),
    # or returns null.
    findContainer: (selector) ->
      container = @element.up selector

      if container
        return new ElementWrapper container
      else
        return null

    # Returns an ElementWrapper for this element's containing element.  The ElementWrapper is created lazily, and
    # cached. Returns null if this element has no parentNode (either because this element is the document object, or
    # because this element is not yet attached to the DOM).
    container: ->
      parentNode = @element.parentNode
      return null unless parentNode

      new ElementWrapper(parentNode)

    # Returns true if this element is visible, false otherwise. This does not check to see if all containers of the
    # element are visible.
    visible: ->
      @element.visible()

    # Returns true if this element is visible, and all parent elements are also visible, up to the document body.
    #
    # Note that in Tapestry 5.3, the search would stop at the nearest form element, not the document body.
    deepVisible: ->
      cursor = this
      while cursor
        return false unless cursor.visible()
        cursor = cursor.container()

        return true if cursor and cursor.element is document.body

      # Bound not reached, meaning that the Element is not currently attached to the DOM.
      return false

    # Fires a named event, passing an optional _memo_ object to event handler functions. This must support
    # common native events (exact list TBD), as well as custom events (in Prototype, custom events must have
    # a prefix that ends with a colon).
    #
    # * eventName - name of event to trigger on the wrapped Element
    # * memo - optional value assocated with the event; available as WrappedeEvent.memo in event handler functions (must
    #   be null for native events). The memo, when provided, should be an object; it is an error if it is a string or other
    #  non-object type..
    #
    # Returns true if the event fully executed, or false if the event was canceled.
    trigger: (eventName, memo) ->
      throw new Error "Attempt to trigger event with null event name" unless eventName?

      unless (_.isNull memo) or (_.isObject memo) or (_.isUndefined memo)
        throw new Error "Event memo may be null or an object, but not a simple type."

      if (eventName.indexOf ':') > 0
        # Custom event is supported directly by Prototype:
        event = @element.fire eventName, memo
        return not event.defaultPrevented

      # Native events take some extra work:
      if memo
        throw new Error "Memo must be null when triggering a native event"

      fireNativeEvent @element, eventName

    # With no parameters, returns the current value of the element (which must be a form control element, such as `<input>` or
    # `<textarea>`). With one parameter, updates the field's value, and returns the previous value. The underlying
    # foundation is responsible for mapping this correctly based on the type of control element.
    # TODO: Define behavior for multi-named elements, such as `<select>`.
    #
    # * newValue - (optional) new value for field
    value: (newValue) ->
      current = @element.getValue()

      if arguments.length > 0
        @element.setValue newValue

      return current

    # Stores or retrieves meta-data on the element. With one parameter, the current value for the name
    # is returned (or undefined). With two parameters, the meta-data is updated and the previous value returned.
    # For Prototype, the meta data is essentially empty (except, perhaps, for some internal keys used to store
    # event handling information).  For jQuery, the meta data may be initialized from data- attributes.
    #
    # * name - name of meta-data value to store or retrieve
    # * value - (optional) new value for meta-data
    meta: (name, value) ->
      current = @element.retrieve name

      if arguments.length > 1
        @element.store name, value

      return current

    # Adds an event handler for one or more events.
    #
    # * events - one or more event names, separated by spaces
    # * match - optional: CSS expression used as a filter; only events that bubble
    #   up to the wrapped element from an originating element that matches the CSS expression
    #   will invoke the handler.
    # * handler - function invoked; the function is passed an `EventWrapper` object, and the
    #   context (`this`) is the `ElementWrapper` for the matched element.
    on: (events, match, handler) ->
      exports.on @element, events, match, handler
      return this

  # converts a selector to an array of DOM elements
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
  # This is very low level; most code will want to go through the `t5/core/ajax` module instead,
  # which adds better handling of exceptions and failures, and handles Tapestry's partial page
  # render reponse keys.
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
            throw new Error message

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


    # TODO: This is not good; we need yet another wrapper of some kind here.
    new Ajax.Request(url, finalOptions)


  # The main export is a function that wraps a DOM element as an ElementWrapper; additional functions are attached as
  # properties.
  #
  # * element - a DOM element, or a string id of a DOM element
  #
  # Returns the ElementWrapper, or null if no element with the id exists
  exports = wrapElement = (element) ->
    if _.isString element
      element = $ element
      return null unless element
    else
      throw new Error "Attempt to wrap a null DOM element" unless element

    # Assume the object is a DOM element, document or window; something that is compatible with the
    # Prototype API (especially with respect to events).
    new ElementWrapper element

  _.extend exports,
    wrap: wrapElement

    # Escape's HTML markup in the string.
    escapeHTML: (str) -> str.escapeHTML()

    ajaxRequest: ajaxRequest

    # Used to add an event handler to an element (possibly from elements below it in the hierarch).
    #
    # * selector - CSS selector used to select elements to attach handler to; alternately,
    #   a single DOM element, or an array of DOM elements. The document is considered an element
    #   for these purposes.
    # * events - one or more event names, separated by spaces
    # * match - optional: CSS expression used as a filter; only events that bubble
    # * up to a selected element from an originating element that matches the CSS expression
    #   will invoke the handler.
    # * handler - function invoked; the function is passed an `EventWrapper` object, and the context (`this`)
    #   is the `ElementWrapper` for the matched element
    on: (selector, events, match, handler) ->
      unless handler?
        handler = match
        match = null

      elements = parseSelectorToElements selector

      onevent elements, (utils.split events), match, handler
      return

    # onDocument() is used to add an event handler to the document object; this is used
    # for global (or default) handlers.
    onDocument: (events, match, handler) ->
      exports.on document, events, match, handler

    # Returns a wrapped version of the document.body element. Because all Tapestry JavaScript occurs
    # inside a block at the end of the document, inside the `<body`> element, it is assumed that
    # it is always safe to get the body.
    body: ->
      bodyWrapper ?= (wrapElement document.body)