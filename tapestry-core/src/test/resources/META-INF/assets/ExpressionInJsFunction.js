function runTest(dom) {
	
	dom = dom.default;

    window.test_func = function () {
        dom('target').value("test1");
    };

    window.test_func_with_map = function () {
        dom('target').value("{key=test2}");
    };
}

const requireJsEnabled = "true" == document.querySelector("html")?.dataset['requireJsEnabled'];
if (requireJsEnabled) {
	require(["t5/core/dom"], runTest);	
}
else {
	import("t5/core/dom").then(runTest);
}
