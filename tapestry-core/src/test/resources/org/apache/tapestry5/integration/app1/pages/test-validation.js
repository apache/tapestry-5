/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
require(["t5/core/validation"], function(v) {

  const parse = v.parseNumber;

  module("t5/core/validation");

  // Tests assume, currently, that the active locale is "en". This affects
  // the decimal format symbols.

  test("basic numeric parsing", function() {

    strictEqual((parse("-1.23")), -1.23);
    strictEqual((parse("200", true)), 200);
    strictEqual((parse("1,000")), 1000);
    return strictEqual((parse(".23")), 0.23);
  });

  test("minus not allowed in middle", () => throws((() => parse("1-1")), /not numeric/));

  test("input is trimmed", () => strictEqual((parse(" 123,456.78 ")), 123456.78));

  test("no grouping seperator after decimal point", () => throws((() => parse(".2,3")), /not numeric/));

  test("consecutive grouping seperator not allowed", () => throws((() => parse("2,,3")), /not numeric/));

  return test("decimal not allowed for integer", () => throws((() => parse("3.14", true)), /not an integer/));
});
