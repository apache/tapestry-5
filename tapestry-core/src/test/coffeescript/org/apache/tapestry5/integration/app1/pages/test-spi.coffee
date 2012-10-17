require ["core/spi"], (spi) ->
  module "core/spi"

  test "get wrapped element by id", ->
    e = spi "spi-eventelement"

    ok e isnt null, "element found and wrapped"

  test "get wrapped element by unknown id is null", ->
    e = spi "spi-does-not-exist-element"

    ok e is null, "element not found and null"

  test "pause and resume events", ->

    clicks = 0
    container = spi "spi-eventelement"
    button = container.findFirst "a"

    # Remember that Prototype will never trigger a native event, just a
    # custom event, so we create a custom event here.
    # NOTE: support for native events was added later.
    eh = container.on "x:click", "a", (event) ->
      event.stop()
      clicks++

    button.trigger "x:click"

    equal clicks, 1, "first click registered"

    eh.stop()

    button.trigger "x:click"

    equal clicks, 1, "no notification when EventHandler stopped"

    eh.start()

    button.trigger "x:click"

    equal clicks, 2, "notifications resume after EventHandler started"

    eh.stop()

  test "trigger native events", ->

    clicks = 0
    container = spi "spi-eventelement"
    button = container.findFirst "a"

    eh = container.on "click", "a", (event) ->
      event.stop()
      clicks++

    button.trigger "click"

    equal clicks, 1, "native event was triggered"

    eh.stop()

  test "selector used with events filters", ->

    clicks = 0
    container = spi "spi-eventelement"
    primary = container.findFirst "a.btn-primary"
    secondary = container.findFirst "a[data-use=secondary]"

    eh = container.on "x:click", "a.btn-primary", (event) ->
      event.stop()
      clicks++

    primary.trigger "x:click"

    equal clicks, 1, "click on selected element invokes handler"

    secondary.trigger "x:click"

    equal clicks, 1, "click on non-selected element does not invoke handler"

    eh.stop()

  test "this is matched element in handler", ->

    container = spi "spi-eventelement"
    primary = container.findFirst "a.btn-primary"

    eh = container.on "x:click", "a.btn-primary", (event) ->
      event.stop()

      strictEqual this.element, primary.element, "this should be the wrapper for element that was matched"

    primary.trigger "x:click"

    eh.stop()

  test "visibility, hide(), and show()", ->

    e = (spi "spi-visibility").findFirst "span"

    equal e.visible(), true, "element is initially visible"

    e.hide()

    equal e.visible(), false, "element is not visible once hidden"

    e.show()

    equal e.visible(), true, "element is visible against once shown"
