require ["t5/core/utils"], (utils) ->

  module "t5/core/utils"

  test "startsWith, positive case", ->
  	ok utils.startsWith 'foobar', 'foo'
  	ok utils.startsWith 'foobarfoo', 'foo' # TAP5-2370

  test "startsWith, negative case", ->
    equal (utils.startsWith 'barfoo', 'foo'), false
