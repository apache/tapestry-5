require ["core/dom", "core/events"],
  (dom, events) ->

    dom.onDocument events.zone.didUpdate, ->
      (dom "zone-update-message").update "Zone updated."
