define ->

  invokeInitializer = (name, argument) ->
    require [name], (moduleLib) -> moduleLib(argument)

  # Handles the simple case, where the initializer name is just the name of a module, and
  # an argument is always present. Later, we'll support an init that is just a string,
  # and names that include a property within the module name.
  (inits) ->
    invokeInitializer name, argument for [name, argument] in inits

