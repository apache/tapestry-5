define(["t5/core/dom"], function (dom) {
    return function (id, message) {
        dom.default(id).update(message);
    };
});