// Copyright 2012-2025 The Apache Software Foundation
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
 * ## t5/core/messages
 *
 * Stub so other modules this one can compile. The ones actually used will
 * be messages-amd.js and messages-es-module.js depending on whether
 * ES modules or AMD/Require.js ones are used.
 * 
 * @packageDocumentation
 */

import _ from "underscore";

let messages: {[key: string]: string} = {};

const get = function(key: string): string {
  return messages[key];
};

// Returns all keys that are defined by the underlying catalog, in no specific order.
get.keys = () => _.keys(messages);


// Export get as the main function.
export default get;
