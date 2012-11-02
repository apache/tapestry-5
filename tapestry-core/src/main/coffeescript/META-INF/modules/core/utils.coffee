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

## core/utils
#
# A few handy functions.
define [], ->

  trim = (input) ->
    if String.prototype.trim
      input.trim()
    else
      input.replace(/^\s+/, '').replace(/\s+$/, '')

  exports =
    # Trims leading and trailing whitespace from a string. Delegates to String.prototype.trim if present.
    trim: trim
    isBlank: (input) -> input is null or (exports.trim input).length == 0