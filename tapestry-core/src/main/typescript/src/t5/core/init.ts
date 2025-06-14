// Copyright 2012, 2025 The Apache Software Foundation
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
 * ## t5/core/init
 * 
 * Compatibility module, invokes functions on the T5.initializers namespace.
 * Introduced in 5.4, to be removed at some point in the future, when T5.initializers is itself no more.
 */
import console from "t5/core/console";

// Line below is used to force the TypeScript compiler to actually import t5/core/console */
// as the compiler doesn't seet to notice it's used directly here..
let workaround = console;

export default (console: { error: (arg0: string) => any; }) => // Exports a single function that finds an initializer in `T5.initializers` and invokes it.
function(initName: string | number, ...args: any) {
  // @ts-ignore
  const fn = T5.initializers[initName];
  if (!fn) {
    return console.error(`Initialization function '${initName}' not found in T5.initializers namespace.`);
  } else {
    return fn.apply(null, args);
  }
}