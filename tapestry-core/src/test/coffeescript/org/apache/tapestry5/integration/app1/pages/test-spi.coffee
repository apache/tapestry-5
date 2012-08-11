require ["core/spi"], (spi) ->
  module "core/spi"

  test "get wrapped element by id", ->
    e = spi.wrap "spi-test1"

    ok e != null, "element found and wrapped"

  test "get wrapped element by unknown id is null", ->
    e = spi.wrap "spi-does-not-exist-element"

    ok e == null, "element not found and null"