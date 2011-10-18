var JST = (function() {

    var _ = T5._;

    var resultElement;

    var $fail = {};

    function fail(text) {
        resultElement.insert({
            top : "FAIL - ",
            after : "<hr>" + text
        }).up("div").addClassName("fail").scrollTo();

        throw $fail;
    }

    function toString(value) {

        // Prototype:
        return Object.toJSON(value);
    }

    function failNotEqual(actual, expected) {
        fail(toString(actual) + " !== " + toString(expected));
    }

    function assertSame(actual, expected) {
        if (actual !== expected) {
            failNotEqual(actual, expected);
        }
    }

    function assertEqual(actual, expected) {

        if (!_.isEqual(actual, expected)) {
            failNotEqual(actual, expected);
        }
    }

    function doRunTests(elementId) {

        var passCount = 0;
        var failCount = 0;

        $(elementId).addClassName("js-results").select("div:odd").each(
            function(e) {
                e.addClassName("odd");
            });

        $(elementId).select("div").each(
            function(test) {

                test.addClassName("active");

                resultElement = test.down("p");
                resultNoted = false;

                var testCode = test.down("pre").textContent;

                try {
                    eval(testCode);

                    passCount++;

                    resultElement.insert({
                        top : "PASS - "
                    });

                    test.addClassName("pass");

                } catch (e) {

                    Tapestry.error(e)

                    failCount++;

                    if (e !== $fail) {
                        resultElement.next().insert(
                            {
                                top : "EXCEPTION - ",
                                after : "<hr><div class='exception'>"
                                    + toString(e) + "</div>"
                            });

                        test.addClassName("fail").scrollTo();
                    }
                }

                test.removeClassName("active");
            });

        $(elementId)
            .insert(
            {
                top : "<p class='caption #{class}'>Results: #{pass} passed / #{fail} failed</p>"
                    .interpolate({
                    class : failCount == 0 ? "pass"
                        : "fail",
                    pass : passCount,
                    fail : failCount
                })
            });

        if (failCount == 0) {
            $(elementId).down("p").scrollTo();
        }
    }

    function runTestSuite(elementId) {
        Tapestry.onDOMLoaded(function() {
            doRunTests(elementId);
        });
    }

    return {
        fail : fail,
        assertEqual : assertEqual,
        assertSame : assertSame,
        runTestSuite : runTestSuite
    };
})();