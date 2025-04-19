/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
require(["t5/core/utils"], function(utils) {

  module("t5/core/utils");

  test("startsWith, positive case", function() {
  	ok(utils.startsWith('foobar', 'foo'));
  	return ok(utils.startsWith('foobarfoo', 'foo'));
  }); // TAP5-2370

  return test("startsWith, negative case", () => equal((utils.startsWith('barfoo', 'foo')), false));
});
