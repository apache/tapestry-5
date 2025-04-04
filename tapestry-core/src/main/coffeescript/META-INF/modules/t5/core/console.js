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

# ## t5/core/console
#
# A wrapper around the native console, when it exists.
define ["t5/core/dom", "underscore", "t5/core/bootstrap"],
  (dom, _, { glyph }) ->

    nativeConsole = null

    try
      # FireFox will throw an exception if you even access the console object and it does
      # not exist. Wow!
      nativeConsole = window.console or {}
    catch e

    floatingConsole = null
    messages = null

    noFilter = -> true

    filter = noFilter

    updateFilter = (text) ->
      if text is ""
        filter = noFilter
        return

      words = text.toLowerCase().split /\s+/

      filter = (e) ->
        content = e.text().toLowerCase()

        for word in words
          return false if content.indexOf(word) < 0

        return true

      return

    consoleAttribute = dom.body.attr "data-floating-console"

    forceFloating = (consoleAttribute is "enabled") or (consoleAttribute is "invisible")

    button = (action, icon, label, disabled = false) -> """
        <button data-action="#{action}" class="btn btn-default btn-mini">
          #{glyph icon} #{label}
        </button>
      """

    # _internal_: displays the message inside the floating console, creating the floating
    # console as needed.
    display = (className, message) ->

      unless floatingConsole
        floatingConsole = dom.create
          class: "tapestry-console",
          """
            <div class="message-container"></div>
            <div class="row">
              <div class="btn-group btn-group-sm col-md-4">
                #{button "clear", "remove", "Clear Console"}
                #{button "enable", "play", "Enable Console"}
                #{button "disable", "pause", "Disable Console"}
              </div>
              <div class="col-md-8">
                <input class="form-control" size="40" placeholder="Filter console content">
              </div>
            </div>
            """

        dom.body.prepend floatingConsole

        # Basically, any non-blank value will enable the floating console. In addition, the special
        # value "invisible" will enable it but then hide it ... this is useful in tests, since
        # the console output is captured in the markup, but the visible console can have unwanted interactions
        # (such as obscuring elements that make them unclickable).
        if consoleAttribute is "invisible"
          floatingConsole.hide()

      messages = floatingConsole.findFirst ".message-container"

      floatingConsole.findFirst("[data-action=enable]").attr "disabled", true

      floatingConsole.on "click", "[data-action=clear]", ->
        floatingConsole.hide()
        messages.update ""

      floatingConsole.on "click", "[data-action=disable]", ->

        @attr "disabled", true
        floatingConsole.findFirst("[data-action=enable]").attr "disabled", false

        messages.hide()

        return false

      floatingConsole.on "click", "[data-action=enable]", ->

        @attr "disabled", true
        floatingConsole.findFirst("[data-action=disable]").attr "disabled", false

        messages.show()

        return false

      floatingConsole.on "change keyup", "input", ->
        updateFilter @value()

        for e in messages.children()
          visible = filter e

          e[if visible then "show" else "hide"]()

        return false

      div = dom.create
        class: className,
        _.escape message

        # Should really filter on original message, not escaped.

      unless filter div
        div.hide()

      messages.append div

      # A slightly clumsy way to ensure that the container is scrolled to the bottom.
      _.delay -> messages.element.scrollTop = messages.element.scrollHeight

    level = (className, consolefn) ->
      (message) ->
        # consolefn may be null if there's no console; under IE it may be non-null, but not a function.
        # For some testing, it is nice to force the floating console to always display.

        if forceFloating or (not consolefn)
          # Display it floating. If there's a real problem, such as a failed Ajax request, then the
          # client-side code should be alerting the user in some other way, and not rely on them
          # being able to see the logged console output.
          display className, message

          return unless forceFloating

        if window.console and (_.isFunction consolefn)
          # Use the available native console, calling it like an instance method
          consolefn.call window.console, message
          return

        # And IE just has to be different. The properties of console are callable, like functions,
        # but aren't proper functions that work with `call()` either.
        # On IE8, the console object is undefined unless debugging tools are enabled.
        # In that case, nativeConsole will be an empty object.
        if consolefn
          consolefn message

        return

    exports =
      info: level "info", nativeConsole.info
      warn: level "warn", nativeConsole.warn
      error: level "error", nativeConsole.error

      # Determine whether debug is enabled by checking for the necessary attribute (which is missing
      # in production mode).
      debugEnabled: (document.documentElement.getAttribute "data-debug-enabled")?

    noop = ->

    # When debugging is not enabled, then the debug function becomes a no-op.
    exports.debug =
      if exports.debugEnabled
        # If native console available, go for it.  IE doesn't have debug, so we use log instead.
        # Add a special noop case for IE8, since IE8 is just crazy.
        level "debug", (nativeConsole.debug or nativeConsole.log or noop)
      else
        noop

    # This is also an aid to debugging; it allows arbitrary scripts to present on the console; when using Geb
    # and/or Selenium, it is very useful to present debugging data right on the page.
    window.t5console = exports

    requirejs.onError = (err) ->

      message = "RequireJS error: #{err?.requireType or 'unknown'}"

      if err.message
        message += """: #{err.message}"""

      if err.requireType
        modules = err?.requireModules
        if modules and modules.length > 0
          message += """, modules #{modules.join(", ")}"""

      if err.fileName
        message += """, #{err.fileName}"""

      if err.lineNumber
        message += """, line #{err.lineNumber}"""

      if err.columnNumber
        message += """, line #{err.columnNumber}"""

      exports.error message


    # Return the exports; we keep a reference to it, so we can see exports.DURATION, even
    # if some other module imports this one and modifies that property.
    return exports