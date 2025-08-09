import dom from "t5/core/dom"
import events from "t5/core/events";
import _ from "underscore"
import console from "t5/core/console";

dom.onDocument(events.palette.willChange, function(event, memo) {

  console.info("palette-demo, palette willChange");

  const values = _.map(memo.selectedOptions, o => o.value);

  (dom("event-selection")).update(JSON.stringify(values));
  return (dom("event-reorder")).update(memo.reorder.toString());
});
