require ["core/spi"], (spi) ->
  module "core/spi"

  test "get wrapped element by id", ->
    e = spi.wrap "spi-test1"

    ok e isnt null, "element found and wrapped"

  test "get wrapped element by unknown id is null", ->
    e = spi.wrap "spi-does-not-exist-element"

    ok e is null, "element not found and null"

  test "pause and resume events", ->

    clicks = 0
    container = spi.wrap "spi-test2"
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