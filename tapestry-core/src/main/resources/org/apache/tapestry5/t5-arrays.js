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
T5.define("arrays", function() {

	function isNonEmpty(array) {
		if (array === null || array === undefined)
			return false;

		return array.length > 0;
	}

	function isEmpty(array) {
		return !isNonEmpty(array);
	}

	var concat = Array.prototype.concat;

	/**
	 * Iterates over an array, invoking a function for each array element.
	 * 
	 * @param fn
	 *            passed each element in array as first parameter, element index
	 *            as second parameter
	 * @param array
	 *            to iterate over (possibly null or undefined)
	 */
	function each(fn, array) {
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
	 * @param mapperfn
	 *            function passed each object from the array, and the index for
	 *            each object from the array
	 * @param array
	 *            object to iterate over (may be null or undefined)
	 * @returns result array (possibly empty)
	 */
	function map(mapperfn, array) {
		var result = [];

		each(function(element, index) {
			result[index] = mapperfn(element, index);
		}, array);

		return result;
	}

	/**
	 * Reduces an array by passing the initial value and the first element to
	 * the reducer function. The result (the accumulator) is passed to the
	 * reducer function with the second element, and so on. The final result is
	 * the accumulator after all elements have been passed.
	 * 
	 * @param reducerfn
	 *            passed the accumulator, an element, and an index and returns
	 *            the new accumulator
	 * @param initial
	 *            the initial value for the accumulator
	 * @param array
	 *            array (may be null or undefined)
	 * @returns the accumulator
	 */
	function reduce(reducerfn, initial, array) {
		var accumulator = initial;

		each(function(element, index) {
			accumulator = reducerfn(accumulator, element, index);
		}, array);

		return accumulator;
	}

	/**
	 * A variation of map, where the mapperfn is expected to return an array of
	 * values (not a single value). The result arrays are concatenated, to
	 * return a single flattened result.
	 * 
	 * @param mapperfn
	 *            passed each element and index, returns an array of results
	 * @param array
	 *            to iterate over
	 * @returns the concatenation of the result arrays
	 */
	function mapcat(mapperfn, array) {
		var results = map(mapperfn, array);

		return concat.apply([], results);
	}

	/**
	 * Removes an element from an array, returning a modified version of the
	 * array with all instances of the element eliminated. Uses === for
	 * comparison. May return the original array unchanged if the element is not
	 * present.
	 * 
	 * @param element
	 *            to remove from array
	 * @param array
	 *            a non-null array
	 * @returns the array, or the array with any references to element removed
	 */
	function without(element, array) {
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

	/**
	 * Filters the array, returning a new array containing just those elements
	 * for which the filterfn returns true.
	 * 
	 * @param filterfn
	 *            function of one or two parameters: element and index. Returns
	 *            true to include element in result.
	 * @param array
	 *            to filter
	 */
	function filter(filterfn, array) {
		var result = [];

		each(function(element, index) {
			if (filterfn(element, index)) {
				result.push(element);
			}
		}, array);

		return result;
	}

	/**
	 * Filters the array, returning a new array containing just those elements
	 * for which the filterfn returns false.
	 * 
	 * @param filterfn
	 *            function of one or two parameters: element and index. Returns
	 *            true to include element in result.
	 * @param array
	 *            to filter
	 */
	function remove(filterfn, array) {
		return filter(function(element, index) {
			return !filterfn(element, index);
		}, array);
	}

	/**
	 * Returns the first element that passes the filterfn, or null if not found.
	 * 
	 * @param filterfn
	 *            function of one or two parameters: element and index. Returns
	 *            true if the element is a match.
	 * @param arary
	 *            to scan
	 * @return first element for which the function indicates a match, or null
	 * 
	 */
	function first(filterfn, array) {
		if (isNonEmpty(array)) {
			for ( var index = 0; index < array.length; index++) {
				if (filterfn(array[index], index)) {
					return array[index];
				}
			}
		}

		return null;
	}

	/**
	 * Returns a function for use with map() or filter(), that extracts a
	 * specific property, by name, from the elements.
	 */
	function extractProperty(name) {
		return function(element) {
			return element[name];
		}
	}

	/**
	 * Extracts the named property from each element in the input array.
	 */
	function extract(propertyName, array) {
		return map(extractProperty(propertyName), array);
	}

	return {
		each : each,
		extract : extract,
		extractProperty : extractProperty,
		filter : filter,
		first : first,
		isEmpty : isEmpty,
		isNonEmpty : isNonEmpty,
		map : map,
		mapcat : mapcat,
		reduce : reduce,
		remove : remove,
		without : without
	};
});