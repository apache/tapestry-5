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

# ## t5/core/autocomplete
#
# Support for the core/Autocomplete Tapestry mixin.
define ["./dom", "./ajax", "jquery", "bootstrap"],
  (dom, ajax, $) ->

    doLookup = ($field, url, query, process) ->
      $field.addClass "ajax-wait"

      ajax url,
        parameters:
          "t:input": query
        onsuccess: (response) ->
          $field.removeClass "ajax-wait"

          process response.json.matches

    init = (spec) ->
      $field = $ document.getElementById spec.id

      $field.typeahead
        minLength: spec.minChars
        source: (query, process) -> doLookup $field, spec.url, query, process


    exports = init