var JST = (function() {

	/*
	 * Original script title/version: Object.identical.js/1.11 Copyright (c)
	 * 2011, Chris O'Brien, prettycode.org
	 * http://github.com/prettycode/Object.identical.js
	 * 
	 * LICENSE: Permission is hereby granted for unrestricted use, modification,
	 * and redistribution of this script, ONLY under the condition that this
	 * code comment is kept wholly complete, appearing above the script's code
	 * body--in all original or modified implementations of this script, except
	 * those that are minified.
	 */

	/*
	 * Requires ECMAScript 5 functions: - Array.isArray() - Object.keys() -
	 * Array.prototype.forEach() - JSON.stringify()
	 */

	function identical(a, b, sortArrays) {

		function sort(o) {

			if (sortArrays === true && Array.isArray(o)) {
				return o.sort();
			} else if (typeof o !== "object" || o === null) {
				return o;
			}

			var result = {};

			Object.keys(o).sort().forEach(function(key) {
				result[key] = sort(o[key]);
			});

			return result;
		}

		return JSON.stringify(sort(a)) === JSON.stringify(sort(b));
	}

	var resultElement;

	var $fail = {};

	function fail(text) {
		resultElement.next().insert({
			top : "FAIL - ",
			bottom : "<hr>" + text
		}).up().addClassName("fail");

		throw $fail;
	}

	function toString(value) {

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

		if (!identical(actual, expected)) {
			failNotEqual(actual, expected);
		}
	}

	function doRunTests(elementId) {

		var passCount = 0;
		var failCount = 0;

		$(elementId).insert({
			top : "<thead><th>Test</th><th>Description</th></tr></thead>"
		});

		$(elementId).addClassName("js-results").select("tbody tr:odd").each(
				function(e) {
					e.addClassName("odd");
				});

		$(elementId).select("tbody tr").each(function(row) {

			row.addClassName("active");

			row.scrollTo();

			resultElement = $(row).select("td")[0];
			resultNoted = false;

			try {
				eval(resultElement.textContent);

				passCount++;

				resultElement.next().insert({
					top : "PASS - "
				}).up().addClassName("pass");

			} catch (e) {
				failCount++;

				if (e !== $fail) {
					resultElement.next().insert({
						top : "EXCEPTION - ",
						bottom : "<hr>" + toString(e)
					}).up().addClassName("exception");
				}
			}

			row.removeClassName("active");
		});

		$(elementId)
				.insert(
						{
							bottom : "<caption class='#{class}'>Results: #{pass} pass / #{fail} fail</caption>"
									.interpolate({
										class : failCount == 0 ? "success"
												: "failures",
										pass : passCount,
										fail : failCount
									})
						});
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