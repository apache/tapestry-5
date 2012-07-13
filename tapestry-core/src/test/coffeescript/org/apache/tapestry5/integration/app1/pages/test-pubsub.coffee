module "PubSub"

require ["core/pubsub"], (pubsub) ->

  test "simple on/fire", ->

    memoValue = null
    expectedMemo = "expected"

    pubsub.on "stim", (memo) -> memoValue = memo
    pubsub.fire "stim", expectedMemo

    ok memoValue is expectedMemo, "responder function was invoked"