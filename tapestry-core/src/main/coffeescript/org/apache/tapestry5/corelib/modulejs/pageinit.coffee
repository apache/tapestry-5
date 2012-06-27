define ->

  invokeInitializer = (qualifiedName, initArguments...) ->

    [moduleName, functionName] = qualifiedName.split ':'

    require [moduleName], (moduleLib) ->
      fn = if functionName? then moduleLib[functionName] else moduleLib
      fn.apply null, initArguments

  # Exports this single function:
  (inits) ->
    invokeInitializer.apply null, init for init in inits

