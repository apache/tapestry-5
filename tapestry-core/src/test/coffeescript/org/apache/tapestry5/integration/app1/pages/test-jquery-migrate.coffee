require ["jquery-migrate"], () ->

  module "jquery-migrate"

  test "Migration warnings are issued", ->
  
    # Clear any warnings that may have been caused in other places
    jQuery.migrateReset()
    equal jQuery.migrateWarnings.length, 0, "No warnings issued"

    # Call a function deprecated in jQuery 3.0
    obj = jQuery.parseJSON( '{ "foo": "bar" }' )
    
    equal jQuery.migrateWarnings.length, 1, "One warning issued"
    equal jQuery.migrateWarnings[0], "jQuery.parseJSON is deprecated; use JSON.parse [parseJSON]", "Warning is the deprecation notice about parseJSON"
