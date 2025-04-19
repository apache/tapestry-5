define ["t5/core/dom", "t5/core/console"],
  (dom, console) ->

    for name in ["debug", "info", "warn", "error"]
      do (name) ->
        (dom name).on "change", -> console[name](@value())

    return