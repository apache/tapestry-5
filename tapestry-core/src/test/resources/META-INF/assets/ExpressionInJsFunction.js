require(["t5/core/dom"], function (dom) {

    window.test_func = function () {
        dom('target').value("test1");
    };

    window.test_func_with_map = function () {
        dom('target').value("{key=test2}");
    };
});

