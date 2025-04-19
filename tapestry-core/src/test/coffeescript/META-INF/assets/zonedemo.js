/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
require(["t5/core/dom", "t5/core/events"],
  (dom, events) => dom.onDocument(events.zone.didUpdate, () => (dom("zone-update-message")).update("Zone updated.")));
