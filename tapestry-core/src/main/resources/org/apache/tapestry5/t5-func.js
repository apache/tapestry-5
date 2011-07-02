/* Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Define functions used to create or manipulate other functions, plus some
 * extra utilities.
 */
T5.define("func", function() {

	return {
		/**
		 * The empty function does nothing.
		 */
		empty : function() {
		},

		/**
		 * The identity function returns its first argument.
		 */
		identity : function(x) {
			return x;
		}
	};
});