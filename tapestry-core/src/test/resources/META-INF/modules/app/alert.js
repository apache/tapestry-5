define(["core/console"], function (console) {

    console.DURATION = 30

    return {
        alert: function (message) {
            console.info("app/alert (module): " + message);
            T5.console.info("app/alert (T5): " + message);
        }
    };
});