require ["t5/core/dom", "t5/core/events"],
  (dom, events) ->

    dom.onDocument events.zone.didUpdate, ->
      (dom "zone-update-message").update "Zone updated."
