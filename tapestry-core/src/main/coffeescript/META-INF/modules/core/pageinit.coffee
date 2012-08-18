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

# ##core/pageinit
#
# Module that defines functions used for page initialization.
# The initialize function is passed an array of initializers; each initializer is itself
# an array. The first value in the initializer defines the name of the module to invoke.
# The module name may also indicate the function exported by the module, as a suffix following a colon:
# e.g., "my/module:myfunc".
# Any additional values in the initializer are passed to the function. The context of the function (this) is null.
define ["_", "core/console", "core/spi", "core/events"],
  (_, console, spi, events) ->
    pathPrefix = null

    # Borrowed from Prototype:
    isOpera = Object.prototype.toString.call(window.opera) == '[object Opera]'
    isIE = !!window.attachEvent && !isOpera

    rebuildURL = (path) ->
      return path if path.match /^https?:/

      # See Tapestry.rebuildURL() for an error about the path not starting with a leading '/'
      # We'll assume that doesn't happen.

      if !pathPrefix
        l = window.location
        pathPrefix = "#{l.protocol}//#{l.host}"

      return pathPrefix + path

    rebuildURLOnIE =
      if isIE then rebuildURL else _.identity

    addStylesheets = (newStylesheets) ->
      return unless newStylesheets

      # Figure out which stylesheets are loaded; adjust for IE, and especially, IE 9
      # which can report a stylesheet's URL as null (not empty string).
      loaded = _.chain(document.styleSheets)
      .pluck("href")
      .without("")
      .without(null)
      .map(rebuildURLOnIE)

      insertionPoint = _.find(document.styleSheets, (ss) -> ss.ownerNode.rel is "stylesheet t-ajax-insertion-point")

      # Most browsers support document.head, but older IE doesn't:
      head = document.head or document.getElementsByTagName("head")[0]

      _.chain(newStylesheets)
      .map((ss) -> { href: rebuildURL(ss.href), media: ss.media })
      .reject((ss) -> loaded.contains(ss.href).value())
      .each((ss) ->
        element = document.createElement "link"
        element.setAttribute "type", "text/css"
        element.setAttribute "rel", "stylesheet"
        element.setAttribute "href", ss.href
        if ss.media
          element.setAttribute "media", ss.media

        if insertionPoint
          head.insertBefore element, insertionPoint.ownerNode
        else
          head.appendChild element

        console.debug "Added stylesheet #{ss.href}"
      )

      return

    invokeInitializer = (tracker, qualifiedName, initArguments) ->
      [moduleName, functionName] = qualifiedName.split ':'

      require [moduleName], (moduleLib) ->

        # Some modules export nothing but do some full-page initialization, such as adding
        # event handlers to the body.
        if not functionName and
          initArguments.length is 0 and
          not _.isFunction moduleLib
            tracker()
            return

        fn = if functionName? then moduleLib[functionName] else moduleLib

        console.debug "Invoking #{qualifiedName} with " + JSON.stringify(initArguments)
        fn.apply null, initArguments


        tracker()

    exports =
    # Passed a list of initializers, executes each initializer in order. Due to asynchronous loading
    # of modules, the exact order in which initializer functions are invoked is not predictable.
      initialize: (inits = [], callback) ->
        callbackCountdown = inits.length + 1

        # tracker gets invoked once after each require/callback, plus once extra
        # (to handle the case where there are no inits). When the count hits zero,
        # it invokes the callback (if there is one).
        tracker = ->
          callbackCountdown--

          if callbackCountdown is 0 and callback
            callback()

        # First value in each init is the qualified module name; anything after
        # that are arguments to be passed to the identified function.
        for [qualifiedName, initArguments...] in inits
          invokeInitializer tracker, qualifiedName, initArguments

        tracker()

      # Pre-loads a number of libraries in order. When the last library is loaded,
      # invokes the callback (with no parameters).
      loadLibraries: (libraries, callback) ->
        reducer = (callback, library) -> ->
          console.debug "Loading library #{library}"
          require [library], callback

        finalCallback = _.reduceRight libraries, reducer, callback

        finalCallback.call null

      # Loads all the libraries, in order. It then executes the immediate initializations.
      # After that, it waits for the DOM to be ready and executes the other initializations.
      loadLibrariesAndInitialize: (libraries, immediateInits, otherInits) ->
        exports.loadLibraries libraries, ->
          exports.initialize immediateInits

          spi.domReady -> exports.initialize otherInits

      evalJavaScript: (js) ->
        require ["core/compat/tapestry"], ->
          console.debug "Evaluating: #{js}"
          eval js

      # Passed the response from an Ajax request, when the request is successful.
      # This is used for any request that attaches partial-page-render data to the main JSON object
      # response.  If no such data is attached, the callback is simply invoked immediately.
      # Otherwise, Tapestry processes the partial-page-render data. This may involve loading some number
      # of JavaScript libraries and CSS style sheets, and a number of direct updates to the DOM. After DOM updates,
      # the callback is invoked, passed the response (with any Tapestry-specific data removed).
      # After the callback is invoked, page initializations occur.  This method returns null.
      # response - the Ajax response object
      # callback - invoked after scripts are loaded, but before page initializations occur (may be null)
      # Returns null
      handlePartialPageRenderResponse: (response, callback) ->

        # Capture the partial page response portion of the overall response, and
        # then remove it so it doesn't interfere elsewhere.
        responseJSON = response.responseJSON or {}
        partial = responseJSON._tapestry
        delete responseJSON._tapestry

        # Extreme case: the data has a redirectURL which forces an immediate redirect to the URL.
        # No other initialization or callback invocation occurs.
        if partial?.redirectURL
          window.location.href = partial.redirectURL
          return

        addStylesheets partial?.stylesheets

        # Temporary ugliness: ensuring Tapestry is available since we, for the moment,
        # make use of some of it.
        require ["core/compat/tapestry"], ->
          # Make sure all libraries are loaded
          exports.loadLibraries partial?.libraries, ->

            # Libraries are loaded, update each zone:
            _(partial?.content).each ([id, content]) ->
              console.debug "Updating content for zone #{id}"

              zone = spi.wrap id

              if zone
                zone.trigger events.zone.update, content

            # Invoke the callback, if present.  The callback may do its own content updates.
            callback and callback.call null, response

            # Now that all content updates are, presumably, complete, it is time to
            # perform initializations.  Once those complete, use the onDomLoadedCallback()
            # to do some final changes and event registrations (hopefully, to be removed
            # soon).

            exports.initialize partial?.inits, Tapestry.onDomLoadedCallback

        return
