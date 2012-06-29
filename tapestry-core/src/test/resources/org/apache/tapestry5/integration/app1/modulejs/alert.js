define(["core/console"], function (cc) {

    cc.DURATION = 30

    return {
        alert: function (message) {
            cc.info("app/alert (module): " + message);
            T5.console.info("app/alert (T5): " + message);
        }
    };
});