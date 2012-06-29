define(["core/console"], function (cc) {
    return {
        alert: function (message) {
            cc.info("app/alert (module): " + message);
            T5.console.info("app/alert (T5): " + message);
        }
    };
});