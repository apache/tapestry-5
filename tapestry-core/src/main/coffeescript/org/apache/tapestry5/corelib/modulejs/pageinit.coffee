# Copyright 2012 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Module that defines functions used for page initialization.
# The initialize function is passed an array of initializers; each initializer is itself
# an array. The first value in the initializer defines the name of the module to invoke.
# The module name may also indicate the function exported by the module, as a suffix following a colon:
# e.g., "my/module:myfunc".
# Any additional values in the initializer are passed to the function. The context of the function (this) is null.
define ["_", "core/console"], (_, console) ->
  invokeInitializer = (qualifiedName, initArguments...) ->
    [moduleName, functionName] = qualifiedName.split ':'

    require [moduleName], (moduleLib) ->
      fn = if functionName? then moduleLib[functionName] else moduleLib
      fn.apply null, initArguments

  exports =
    # Passed a list of initializers, executes each initializer in order. Due to asynchronous loading
    # of modules, the exact order in which initializer functions are invoked is not predictable.
    initialize: (inits) ->
      # apply will split the first value from the rest for us, implicitly.
      invokeInitializer.apply null, init for init in inits

    # Pre-loads a number of scripts in order. When the last script is loaded,
    # invokes the callback (with no parameters).
    loadScripts: (scripts, callback) ->
      reducer = (callback, script) -> ->
        console.debug "Loading script #{script}"
        require [script], callback

      finalCallback = _.reduceRight scripts, reducer, callback

      finalCallback.call(null)

    # Loads all the scripts, in order. It then executes the immediate initializations.
    # After that, it waits for the DOM to be ready and executes the other initializations.
    loadScriptsAndInitialize: (scripts, immediateInits, otherInits) ->
      exports.loadScripts scripts, ->
        exports.initialize immediateInits
        require ["core/domReady!"], -> exports.initialize otherInits

    evalJavaScript: (js) ->
      console.debug "Evaluating: #{js}"
      eval js




