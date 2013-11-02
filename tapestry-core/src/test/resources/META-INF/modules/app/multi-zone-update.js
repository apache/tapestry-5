define(["t5/core/dom"], function (dom) {
    return function (id, message) {
        dom(id).update(message);
    };
});