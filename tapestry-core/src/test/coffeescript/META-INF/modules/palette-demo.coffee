define ["t5/core/dom", "t5/core/events", "underscore", "t5/core/console"],
  (dom, events, _, console) ->

    dom.body.on events.palette.willChange, (event, memo) ->

      console.info "palette-demo, palette willChange"

      values = _.map memo.selectedOptions, (o) -> o.value

      (dom "event-selection").update JSON.stringify values
      (dom "event-reorder").update memo.reorder.toString()