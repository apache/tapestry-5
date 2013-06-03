require ["t5/core/messages", "underscore"], (messages, _) ->

  module "t5/core/messages"

  missing = (key) ->
    (_.indexOf messages.keys(), key) is -1

  test "access known key", ->
    equal messages("client-accessible"), "Client Accessible"

  test "unknown messages key", ->

    equal messages("gnip-gnop"), "[[Missing Key: 'gnip-gnop']]"

  test "messages values with '%' are not client accessible", ->

    ok missing "not-visible"

  test "messages prefixed with 'private-' are not client accessible", ->

    ok missing "private-is-not-visible"

