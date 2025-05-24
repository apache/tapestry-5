/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
define(["t5/core/dom", "t5/core/events", "underscore", "t5/core/console"],
  (dom, events, _, console) => dom = dom.default; dom.default.on(events.palette.willChange, function(event, memo) {

    console.info("palette-demo, palette willChange");

    const values = _.map(memo.selectedOptions, o => o.value);

    (dom("event-selection")).update(JSON.stringify(values));
    return (dom("event-reorder")).update(memo.reorder.toString());
  }));