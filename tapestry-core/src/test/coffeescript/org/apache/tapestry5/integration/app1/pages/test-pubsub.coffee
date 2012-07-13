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

  test "off match by specific responder", ->
    count = 0

    responder = -> count++

    pubsub.on "stim", responder
    pubsub.fire "stim"

    equal count, 1, "responder invoked on first fire"

    pubsub.off null, responder
    pubsub.fire "stim"

    equal count, 1, "responder not invoked after removal"

