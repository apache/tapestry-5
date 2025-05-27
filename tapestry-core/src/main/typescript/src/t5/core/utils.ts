// Copyright 2012, 2013, 2025 The Apache Software Foundation
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

/** 
 * ## t5/core/utils
 *
 * A few handy functions.
 * @packageDocumentation
 */
import _ from "underscore";

let exports_;
const trim = function(input: string) {
  // @ts-ignore
  if (String.prototype.trim) {
    return input.trim();
  } else {
    return input.replace(/^\s+/, '').replace(/\s+$/, '');
  }
};

// Extends a URL, adding parameters and values from the params object. The values must already
// be URI encoded.
const extendURL = function(url: string, params: [key: string]) {

  let sep = url.indexOf("?") >= 0 ? "&" : "?";

  for (var name in params) {
    var value = params[name];
    url = url + sep + name + "=" + value;
    sep = "&";
  }

  return url;
};

export default {
      trim,
      extendURL,

      startsWith(string: string, pattern: string) { return (string.indexOf(pattern)) === 0; },
      // Trims leading and trailing whitespace from a string. Delegates to String.prototype.trim if present.
      // Determines if the input is a blank string, or null, or an empty array.
      isBlank(input: any) {

          if (input === null) { return true; }

          if (_.isArray(input)) {
            return input.length === 0;
          }
            
          if (typeof input === "boolean") { return false; }

          return (trim(input)).length === 0;
        },

      // Splits the input string into words separated by whitespace
      split(str: string) { 
        return _(str.split(" ")).reject(s => s === ""); 
      }
    };
