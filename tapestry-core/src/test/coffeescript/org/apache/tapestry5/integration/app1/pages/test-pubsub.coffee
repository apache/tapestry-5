module "PubSub"

require ["core/pubsub"], (pubsub) ->

  test "export aliases", ->

    ok pubsub.on is pubsub.respondTo, "on and respondTo"
    ok pubsub.off is pubsub.stopResponding, "off and stopResponding"

  test "simple on/fire", ->

    memoValue = null
    expectedMemo = "expected"

    pubsub.on "stim", (memo) -> memoValue = memo
    pubsub.fire "stim", expectedMemo

    ok memoValue is expectedMemo, "responder function was invoked"

