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
 * Extends T5 with new utility functions.
 */
T5.extend(T5, function() {

	function isNonEmpty(array) {
		if (array === null || array === undefined)
			return false;

		return array.length > 0;
	}

	/**
	 * Iterates over an array, invoking a function for each array element.
	 * 
	 * @param array
	 *            to iterate over (possibly null or undefined)
	 * @param fn
	 *            passed each element in array as first parameter, element index
	 *            as second parameter
	 */
	function each(array, fn) {
		if (isNonEmpty(array)) {
			for ( var index = 0; index < array.length; index++) {
				fn(array[index], index);
			}
		}
	}

	/**
	 * Maps over a JavaScript array, passing each value to the mapper function.
	 * Returns the array of return values from the mapper.
	 * 
	 * @param array
	 *            object to iterate over (may be null or undefined)
	 * @param mapperfn
	 *            function passed each object from the array, and the index for
	 *            each object from the array
	 * @returns result array (possibly empty)
	 */
	function map(array, mapperfn) {
		var result = [];

		each(array, function(element, index) {
			result[index] = mapperfn(element, index);
		});

		return result;
	}

	/**
	 * Reduces an array by passing the initial value and the first element to
	 * the reducer function. The result (the accumulator) is passed to the
	 * reducer function with the second element, and so on. The final result is
	 * the accumulator after all elements have been passed.
	 * 
	 * @param array
	 *            array (may be null or undefined)
	 * @param initial
	 *            the initial value for the accumulator
	 * @param reducerfn
	 *            passed the accumulator, an element, and an index and returns
	 *            the new accumulator
	 * @returns the accumulator
	 */
	function reduce(array, initial, reducerfn) {
		var accumulator = initial;

		each(array, function(element, index) {
			accumulator = reducerfn(accumulator, element, index);
		});

		return accumulator;
	}

	var concat = Array.prototype.concat;
	
	/**
	 * A variation of map, where the mapperfn is expected to return an array of
	 * values (not a single value). The result arrays are concatenated, to
	 * return a single flattened result.
	 * 
	 * @param array
	 *            to iterate over
	 * @param mapperfn
	 *            passed each element and index, returns an array of results
	 * @returns the concatination of the result arrays
	 */
	function mapcat(array, mapperfn) {
		var results = map(array, mapperfn);

		return concat.apply([], results);
	}

	/**
	 * Removes an element from an array, returning a modified version of the
	 * array with all instances of the element eliminated. Uses === for
	 * comparison. May return the original array unchanged if the element is not
	 * present.
	 * 
	 * @param array
	 *            a non-null array
	 * @param element
	 *            to remove from array
	 * @returns the array, or the array with any references to element removed
	 */
	function without(array, element) {
		var index;
		for (index = array.length - 1; index >= 0; index--) {
			if (array[index] === element) {
				// TODO: This could be made more efficient when the element is
				// the first or last index in the array.

				array = array.slice(0, index).concat(array.slice(index + 1));
			}
		}

		return array;
	}

	return {
		each : each,
		map : map,
		mapcat : mapcat,
		reduce : reduce,
		without : without
	};
});