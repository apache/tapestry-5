/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
require(["t5/core/dom", "t5/core/events"],
  (dom, events) => dom.default.onDocument(events.default.zone.didUpdate, () => (dom.default("zone-update-message")).update("Zone updated.")));
