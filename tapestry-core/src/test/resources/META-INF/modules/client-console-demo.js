/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
define(["t5/core/dom", "t5/core/console"],
  function(dom, console) {

    for (var name of ["debug", "info", "warn", "error"]) {
      ((name => (dom(name)).on("change", function() { return console[name](this.value()); })))(name);
    }

});