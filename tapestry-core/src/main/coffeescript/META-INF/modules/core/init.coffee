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

# ## core/init
#
# Tapestry 5.3 compatibility module, invokes functions on the T5.initializers namespace.
#
# Provides a small amount of backwards compatibility to the Tapestry 5.3 approach.
# This provides placeholders for the following:
#
# * `T5` namespace, including `extend`, `define`, and `initializers`, `extendInitializers`, and `_` properties
# * `Tapestry` namespace: just the `Initializer` property, as an alias of `T5.initializers`
#
# Introduced in 5.4, to be removed in the next release.
define ["core/console", "_"],

  (console, _) ->

    extend = (destination, source) ->
      if _.isFunction source
        source = source()

      _.extend destination, source

    T5 =
      _: _.noConflict()
      extend: extend

      define: (name, source) ->

        namespace = extend {}, source

        T5[name] = namespace

      initializers: {}

      extendInitializers: (source) ->

        extend T5.initializers, source

    Tapestry =
      Initializer: T5.initializers

    window.T5 = T5
    window.Tapestry = Tapestry

    # Exports a single function that finds an initializer in `T5.initializers` and invokes it.
    (initName, args...) ->
      fn = T5.initializers[initName]
      if not fn
        console.error "Initialization function '#{initName}' not found in T5.initializers namespace."
      else
        fn.apply null, args
