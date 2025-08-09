define(["t5/core/dom", "t5/core/console"],
  function(dom, console) {

	dom = dom.default;
	console = console.default;
	
    for (var name of ["debug", "info", "warn", "error"]) {
      ((name => (dom(name)).on("change", function() { return console[name](this.value()); })))(name);
    }

});