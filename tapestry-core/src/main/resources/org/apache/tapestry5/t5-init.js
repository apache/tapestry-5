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

/** Extends T5 with new functions related to page initialization. */
T5.extend(T5, function() {

	return {
		/**
		 * The T5.Initializer namespace, which contains functions used to
		 * perform page load initializations.
		 */
		initializers : {},

		/**
		 * A convenience method for extending the T5.Initializer namespace.
		 * 
		 * @param source
		 *            object or function used to extend T5.initializers
		 */
		extendInitializers : function(source) {
			T5.extend(T5.initializers, source);
		}
	};
});