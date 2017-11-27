define(["require", "exports"], function (require, exports) {
    "use strict";
    function greeter(person) {
        return "Hello, " + person;
    }
    var user = "Jane User";
    document.body.innerHTML = greeter(user);
});
