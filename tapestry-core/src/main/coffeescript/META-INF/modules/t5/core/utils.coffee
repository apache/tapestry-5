# Copyright 2012, 2013 The Apache Software Foundation
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

## t5/core/utils
#
# A few handy functions.
define ["underscore"], (_) ->

  trim = (input) ->
    if String.prototype.trim
      input.trim()
    else
      input.replace(/^\s+/, '').replace(/\s+$/, '')

  exports =
    startsWith: (string, pattern) -> (string.lastIndexOf pattern) is 0
    # Trims leading and trailing whitespace from a string. Delegates to String.prototype.trim if present.
    trim: trim
    # Determines if the input is a blank string, or null, or an empty array.
    isBlank: (input) ->

        return true if input is null

        if _.isArray input
          return input.length is 0

        return (exports.trim input).length is 0

    # Splits the input string into words separated by whitespace
    split: (str) -> _(str.split " ").reject((s) -> s is "")
