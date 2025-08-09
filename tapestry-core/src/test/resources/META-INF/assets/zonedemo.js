/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */

function execute(dom, events) {
	dom.default.onDocument(events.default.zone.didUpdate, function() {
		dom.default("zone-update-message").update("Zone updated.");
	});
}

if (typeof require !== 'undefined') {
	require(["t5/core/dom", "t5/core/events"], function(dom, events) {
		execute(dom, events);
	});
	  
}
else {
	import("t5/core/dom").then((dom) => { 
		import("t5/core/events").then((events) => {
			execute(dom, events);
		});
	});
}