require ["core/messages"], (messages) ->

  module "core/messages"

  test "access known key", ->
    equal messages("client-accessible"), "Client Accessible"

  test "unknown messages key", ->

    equal messages("gnip-gnop"), "[[Missing Key: 'gnip-gnop']]"
