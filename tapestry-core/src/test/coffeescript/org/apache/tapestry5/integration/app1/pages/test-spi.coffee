require ["core/spi"], (spi) ->
  module "core/spi"

  test "get wrapped element by id", ->
    e = spi.wrap "spi-eventelement"

    ok e isnt null, "element found and wrapped"

  test "get wrapped element by unknown id is null", ->
    e = spi.wrap "spi-does-not-exist-element"

    ok e is null, "element not found and null"

  test "pause and resume events", ->

    clicks = 0
    container = spi.wrap "spi-eventelement"
    button = container.find "a"

    # Remember that Prototype will never trigger a native event, just a
    # custom event, so we create a custom event here.
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

  test "selector used with events filters", ->

    clicks = 0
    container = spi.wrap "spi-eventelement"
    primary = container.find "a.btn-primary"
    secondary = container.find "a[data-use=secondary]"

    container.on "x:click", "a.btn-primary", (event) ->
      event.stop()
      clicks++

    primary.trigger "x:click"

    equal clicks, 1, "click on selected element invokes handler"

    secondary.trigger "x:click"

    equal clicks, 1, "click on non-selected element does not invoke handler"

  test "this is matched element in handler", ->

    container = spi.wrap "spi-eventelement"
    primary = container.find "a.btn-primary"

    container.on "x:click", "a.btn-primary", (event) ->
      event.stop()

      strictEqual this, primary.element, "this should be the element that was matched"

    primary.trigger "x:click"

  test "visibility, hide(), and show()", ->

    e = (spi.wrap "spi-visibility").find "span"

    equal e.visible(), true, "element is initially visible"

    e.hide()

    equal e.visible(), false, "element is not visible once hidden"

    e.show()

    equal e.visible(), true, "element is visible against once shown"
