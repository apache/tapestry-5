Tapestry.onDOMLoaded(function() {

    function execute(level) {
        T5.console[level]($(level).value);
        $(level).select();
    }

    function wire(level) {
        $(level).observe("change", function() { execute(level); });

    }

    _.each(["debug", "info", "warn", "error"], wire);
});
