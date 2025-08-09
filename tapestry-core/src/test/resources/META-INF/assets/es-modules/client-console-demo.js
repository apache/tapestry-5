import dom from "t5/core/dom";
import console from  "t5/core/console";

for (var name of ["debug", "info", "warn", "error"]) {
  ((name => (dom(name)).on("change", function() { return console[name](this.value()); })))(name);
}
