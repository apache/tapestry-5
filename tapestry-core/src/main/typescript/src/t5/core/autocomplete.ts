// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// @ts-nocheck

/**
 * ## t5/core/autocomplete
 * 
 * Support for the core/Autocomplete Tapestry mixin, a wrapper around
 * the Twitter autocomplete.js library.
 */
import dom from "t5/core/dom";
import _ from "underscore";
import $ from "jquery"
import utils from "t5/core/utils";
import typeahead from "t5/core/typeahead";

// Line below is used to force the TypeScript compiler to actually import t5/core/typeahead
// as it's not used directly here. 
let workaround = typeahead;

export default function(spec) {
  const $field = $(document.getElementById(spec.id));

  const engine = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.whitespace,
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    limit: spec.limit,
    remote: {
      url: spec.url,
      replace(uri, query) { return utils.extendURL(uri, {"t:input": query}); },
      filter(response) { return response.matches; }
    }
  });

  engine.initialize();

  const dataset = {
    name: spec.id,
    displayKey: _.identity,
    source: engine.ttAdapter()
  };

  $field.typeahead(
    {minLength: spec.minChars},
    dataset);

  // don't validate the "tt-hint" input field created by Typeahead (fix for TAP5-2440)
  $field.prev(".tt-hint").removeAttr("data-validation data-optionality data-required-message");

};