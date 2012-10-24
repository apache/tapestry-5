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

# For all of these modules, we've turned off CoffeeScript's normal outer function
# wrapper, as each module is just a call to `define()` with a function that fulfills
# the same purpose. This one is different, as it is necessary to compute one of the dependencies.
# On the server `core/messages/<locale>` is actually generated dynamically, as is a simple
# mapping of message keys to message values, from the global application message catalog.
do ->
  # In the unexpected case that the data-locale attribute is missing, assume English
  locale = (document.documentElement.getAttribute "data-locale") or "en"

  define ["core/messages/#{locale}"],
    (messages) ->

      # Returns the application message catalog message for the given key. Returns
      # a placeholder if the key is not found.
      get = (key) ->
        return messages[key] || "[[Missing Key: '#{key}']]"

      # Export get as the main function; perhaps later we'll add a "format"
      # or something similar as a property of get.
      return get
