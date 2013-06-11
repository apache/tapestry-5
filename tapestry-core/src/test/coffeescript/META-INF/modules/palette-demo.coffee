define ["t5/core/dom", "t5/core/events", "underscore"],
  (dom, events, _) ->

    dom.onDocument events.palette.willChange, (event, memo) ->

      values = _.map memo.selectedOptions, (o) -> o.value

      (dom "event-selection").update JSON.stringify values
      (dom "event-reorder").update memo.reorder.toString()