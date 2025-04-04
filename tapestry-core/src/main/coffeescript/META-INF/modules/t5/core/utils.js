/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
// Copyright 2012, 2013 The Apache Software Foundation
//
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

//# t5/core/utils
//
// A few handy functions.
define(["underscore"],

  function(_) {

    let exports;
    const trim = function(input) {
      if (String.prototype.trim) {
        return input.trim();
      } else {
        return input.replace(/^\s+/, '').replace(/\s+$/, '');
      }
    };

    // Extends a URL, adding parameters and values from the params object. The values must already
    // be URI encoded.
    const extendURL = function(url, params) {

      let sep = url.indexOf("?") >= 0 ? "&" : "?";

      for (var name in params) {
        var value = params[name];
        url = url + sep + name + "=" + value;
        sep = "&";
      }

      return url;
    };

    return exports = {
      trim,
      extendURL,

      startsWith(string, pattern) { return (string.indexOf(pattern)) === 0; },
      // Trims leading and trailing whitespace from a string. Delegates to String.prototype.trim if present.
      // Determines if the input is a blank string, or null, or an empty array.
      isBlank(input) {

          if (input === null) { return true; }

          if (_.isArray(input)) {
            return input.length === 0;
          }
            
          if (typeof input === "boolean") { return false; }

          return (exports.trim(input)).length === 0;
        },

      // Splits the input string into words separated by whitespace
      split(str) { return _(str.split(" ")).reject(s => s === ""); }
    };
});
