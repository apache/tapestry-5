define ["t5/core/dom", "t5/core/events"],
  (dom, events) ->

    dom.onDocument events.palette.willChange, (event, memo) ->
      (dom "event-selection").update JSON.stringify memo.selectedValues