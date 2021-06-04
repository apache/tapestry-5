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

# ## t5/core/pageinit
#
# Module that defines functions used for page initialization.
# The initialize function is passed an array of initializers; each initializer is itself
# an array. The first value in the initializer defines the name of the module to invoke.
# The module name may also indicate the function exported by the module, as a suffix following a colon:
# e.g., "my/module:myfunc".
# Any additional values in the initializer are passed to the function. The context of the function (this) is null.
define ["underscore", "t5/core/console", "t5/core/dom", "t5/core/events"],
  (_, console, dom, events) ->
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

      insertionPoint = _.find(document.styleSheets, (ss) ->
        parent = ss.ownerNode || ss.owningElement
        parent.rel is "stylesheet ajax-insertion-point")

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
      
        try
          # Some modules export nothing but do some full-page initialization, such as adding
          # event handlers to the body.
          if not functionName and
            initArguments.length is 0 and
            not _.isFunction moduleLib
              console.debug "Loaded module #{moduleName}"
              return

          unless moduleLib
            throw new Error "require('#{moduleName}') returned #{moduleLib} when not expected"

          fn = if functionName? then moduleLib[functionName] else moduleLib

          unless fn?
            if functionName
              console.error "Could not locate function `#{qualifiedName}' in #{JSON.stringify(moduleLib)}"
              console.error moduleLib
            throw new Error "Could not locate function `#{qualifiedName}'."

          if console.debugEnabled
            argsString = (JSON.stringify arg for arg in initArguments).join(", ")
            console.debug "Invoking #{qualifiedName}(#{argsString})"

          fn.apply null, initArguments
        finally
          tracker()

    # Loads all specified libraries in order (this includes the core stack, other stacks, and
    # any free-standing libraries). It then executes the initializations. Once all initializations have
    # completed (which is usually an asynchronous operation, as initializations may require the loading
    # of further modules), then the `data-page-initialized` attribute of the `<body>` element is set to
    # 'true'.
    #
    # This is the main export of the module; other functions are attached as properties.
    loadLibrariesAndInitialize = (libraries, inits) ->
      console.debug "Loading #{libraries?.length or 0} libraries"
      exports.loadLibraries libraries,
        -> exports.initialize inits,
          ->
            # At this point, all libraries have been loaded, and all inits should have executed. Unless some of
            # the inits triggered Ajax updates (such as a core/ProgressiveDisplay component), then the page should
            # be ready to go. We set a flag, mostly used by test suites, to ensure that all is ready.
            # Later Ajax requests will cause the data-ajax-active attribute to be incremented (from 0)
            # and decremented (when the requests complete).

            dom.body.attr "data-page-initialized", "true"

            for mask in dom.body.find ".pageloading-mask"
              mask.remove()

    exports = _.extend loadLibrariesAndInitialize,
      # Passed a list of initializers, executes each initializer in order. Due to asynchronous loading
      # of modules, the exact order in which initializer functions are invoked is not predictable.
      initialize: (inits = [], callback) ->
        console.debug "Executing #{inits.length} inits"
        callbackCountdown = inits.length + 1

        # tracker gets invoked once after each require/callback, plus once extra
        # (to handle the case where there are no inits). When the count hits zero,
        # it invokes the callback (if there is one).
        tracker = ->
          callbackCountdown--

          if callbackCountdown is 0
            console.debug "All inits executed"
            callback() if callback

        # First value in each init is the qualified module name; anything after
        # that are arguments to be passed to the identified function. A string
        # is the name of a module to load, or function to invoke, that
        # takes no parameters.
        for init in inits
          if _.isString init
            invokeInitializer tracker, init, []
          else
            [qualifiedName, initArguments...] = init
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

      evalJavaScript: (js) ->
        console.debug "Evaluating: #{js}"
        eval js

      # Triggers the focus event on the field, if the field exist. Focus occurs delayed 1/8th of a
      # second, which helps ensure that other initializions on the page are in place.
      #
      # * fieldId - element id of field to focus on
      focus: (fieldId) ->
        field = dom fieldId

        if field
          _.delay (-> field.focus()), 125

      # Passed the response from an Ajax request, when the request is successful.
      # This is used for any request that attaches partial-page-render data to the main JSON object
      # response.  If no such data is attached, the callback is simply invoked immediately.
      # Otherwise, Tapestry processes the partial-page-render data. This may involve loading some number
      # of JavaScript libraries and CSS style sheets, and a number of direct updates to the DOM. After DOM updates,
      # the callback is invoked, passed the response (with any Tapestry-specific data removed).
      # After the callback is invoked, page initializations occur.  This method returns null.

      # * response - the Ajax response object
      # * callback - invoked after scripts are loaded, but before page initializations occur (may be null)
      handlePartialPageRenderResponse: (response, callback) ->

        # Capture the partial page response portion of the overall response, and
        # then remove it so it doesn't interfere elsewhere.
        responseJSON = response.json or {}
        partial = responseJSON._tapestry
        delete responseJSON._tapestry

        # Extreme case: the data has a redirectURL which forces an immediate redirect to the URL.
        # No other initialization or callback invocation occurs.
        if partial?.redirectURL
          if window.location.href is partial.redirectURL
            window.location.reload true
          else
            window.location.href = partial.redirectURL
          return

        addStylesheets partial?.stylesheets

        # Make sure all libraries are loaded
        exports.loadLibraries partial?.libraries, ->

          # After libraries are loaded, update each zone:
          _(partial?.content).each ([id, content]) ->
            console.debug "Updating content for zone #{id}"

            zone = dom.wrap id

            if zone
              zone.trigger events.zone.update, { content }

          # Invoke the callback, if present.  The callback may do its own content updates.
          callback and callback(response)

          # Lastly, perform initializations from the partial page render response.
          exports.initialize partial?.inits

        return
