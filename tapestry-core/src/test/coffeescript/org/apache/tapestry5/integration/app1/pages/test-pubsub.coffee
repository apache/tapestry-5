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

  test "stopResponding, match by responder", ->
    count = 0

    responder = -> count++

    pubsub.on "stim", responder
    pubsub.fire "stim"

    equal count, 1, "responder invoked on first fire"

    pubsub.off null, responder
    pubsub.fire "stim"

    equal count, 1, "responder not invoked after removal"

  test "stopResponding, match by namespace name", ->
    log = []

    a = (memo) -> log.push "a:#{memo}"
    b = (memo) -> log.push "b:#{memo}"

    pubsub.on "stim.a", a
    pubsub.on "stim.b", b

    pubsub.fire "stim", "first"

    deepEqual log, ["a:first", "b:first"], "both responders invoked"

    log.length = 0

    pubsub.off ".a"
    pubsub.fire "stim", "second"

    deepEqual log, ["b:second"], "only second responder invoked after .a removal"

  test "stopResponding, match by stimulus name", ->
    log = []

    a = (memo, event) -> log.push "a:#{event.stimulus}-#{memo}"
    b = (memo, event) -> log.push "b:#{event.stimulus}-#{memo}"
    c = (memo, event) -> log.push "c:#{event.stimulus}-#{memo}"

    pubsub.on("alpha", a).on("beta", b).on("alpha", c)

    pubsub.fire "alpha", "one"
    pubsub.fire "beta", "first"

    deepEqual log, ["a:alpha-one", "c:alpha-one", "b:beta-first"], "all responders invoked"

    log.length = 0

    pubsub.off "alpha"

    pubsub.fire "alpha", "two"
    pubsub.fire "beta", "second"

    deepEqual log, ["b:beta-second"], "only 'beta' responder invoked after removal"




