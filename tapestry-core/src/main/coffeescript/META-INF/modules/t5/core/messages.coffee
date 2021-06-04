# Copyright 2012-2013 The Apache Software Foundation
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

# ## t5/core/messages
#
# For all of these modules, we've turned off CoffeeScript's normal outer function
# wrapper, as each module is just a call to `define()` with a function that fulfills
# the same purpose. This one is different, as it is necessary to compute one of the dependencies.
# On the server `t5/core/messages/<locale>` is actually generated dynamically, as is a simple
# mapping of message keys to message values, from the global application message catalog.
#
# This module provides access to localized messages from the Tapestry applications' server-side
# application message catalog (which is, itself, built from multiple resources, some provided by
# the framework, others provided by the application, or third-party libraries).
#
# Messages in the catalog that contain Java-style format specifiers are not included, as there
# is no facility for formatting those on the client. This is actually done as a simple test for the
# presence of the `%` character.  In addition, any message key that begins with "private-" is
# assumed to contain sensitive data (such as database URLs or passwords) and will not be
# exposed to the client.

# In the unexpected case that the data-locale attribute is missing, assume English
locale = (document.documentElement.getAttribute "data-locale") or "en"

define ["t5/core/messages/#{locale}", "underscore", "t5/core/console"],
  (messages, _, console) ->

    # Returns the application message catalog message for the given key. Returns
    # a placeholder if the key is not found.
    get = (key) ->
      value = messages[key]

      if value
        return value
      else
        console.error "No value for message catalog key '#{key}' exists."
        return "[[Missing Key: '#{key}']]"

    # Returns all keys that are defined by the underlying catalog, in no specific order.
    get.keys = -> _.keys messages


    # Export get as the main function.
    return get
