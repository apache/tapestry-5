Tapestry.onDOMLoaded(function() {

    var _ = T5._;

    function execute(level) {
        T5.console[level]($(level).value);
        $(level).select();
    }

    function wire(level) {
        $(level).observe("change", function() {
            execute(level);
        });

    }

    _.each(["debug", "info", "warn", "error"], wire);
});
