require ["t5/core/dom"], (dom) ->
  module "t5/core/dom"

  test "get wrapped element by id", ->
    e = dom "dom-eventelement-native"

    ok e isnt null, "element found and wrapped"

  test "get wrapped element by unknown id is null", ->
    e = dom "dom-does-not-exist-element"

    ok e is null, "element not found and null"

  test "trigger native events", ->

    clicks = 0
    container = dom "dom-eventelement-native"
    button = container.findFirst "a"

    container.on "click", "a", ->
      clicks++
      return false

    button.trigger "click"

    equal clicks, 1, "native event was triggered"

  test "selector used with events filters", ->

    clicks = 0
    container = dom "dom-eventelement-selector"
    primary = container.findFirst "a.btn-primary"
    secondary = container.findFirst "a[data-use=secondary]"

    container.on "x:click", "a.btn-primary", ->
      clicks++
      return false

    primary.trigger "x:click"

    equal clicks, 1, "click on selected element invokes handler"

    secondary.trigger "x:click"

    equal clicks, 1, "click on non-selected element does not invoke handler"

  test "this is matched element in handler", ->

    container = dom "dom-eventelement-matched"
    primary = container.findFirst "a.btn-primary"

    container.on "x:click", "a.btn-primary", ->

      strictEqual @element, primary.element, "this should be the wrapper for element that was matched"

      return false

    primary.trigger "x:click"

  test "visibility, hide(), and show()", ->

    e = (dom "dom-visibility").findFirst "span"

    equal e.visible(), true, "element is initially visible"

    e.hide()

    equal e.visible(), false, "element is not visible once hidden"

    e.show()

    equal e.visible(), true, "element is visible against once shown"
