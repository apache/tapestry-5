/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
require(["t5/core/messages", "underscore"], function(messages, _) {

  module("t5/core/messages");

  const missing = key => (_.indexOf(messages.keys(), key)) === -1;

  test("access known key", () => equal(messages("client-accessible"), "Client Accessible"));

  test("unknown messages key", () => equal(messages("gnip-gnop"), "[[Missing Key: 'gnip-gnop']]"));

  test("messages values with '%' are not client accessible", () => ok(missing("not-visible")));

  return test("messages prefixed with 'private-' are not client accessible", () => ok(missing("private-is-not-visible")));
});

