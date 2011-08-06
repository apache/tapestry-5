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
 * Defines Tapestry Ajax support, which includes sending requests and receiving
 * replies, but also includes default handlers for errors and failures, and
 * processing of Tapestry's partial page render response (a common response for
 * many types of Ajax requests).             `
 */
T5.define("ajax", function() {

	var $ = T5.$;
	var spi = T5.spi;

    function noop() { }

	function defaultFailure(transport) {
	}
	
	function defaultException(exception) {
	}
	
	/**
	 * Performs an AJAX request. The options object is used to identify
	 * additional parameters to be encoded into the request, and to identify the
	 * handlers for success and failure.
	 * <p>
	 * Option keys:
	 * <dl>
	 * <dt>parameters
	 * <dd>object with string keys and string values, defines additional query
	 * parameters
	 * <dt>failure
	 * <dd>A function invoked if the Ajax request fails; the function is passed
	 * the transport
	 * <dt>exception
	 * <dd>A function invoked if there's an exception processing the Ajax
	 * request, the function is passed the exception
	 * <dt>success
	 * <dd>A function invoked when the Ajax response is returned successfully.
	 * The function is passed the transport object.
	 * <dt>method
	 * <dd>The type of request, 'get' or 'post'. 'post' is the default.
	 * </dl>
	 * 
	 * @param url
	 *            the URL for the request
	 * @param options
	 *            an optional object that provides additional options.
	 * @return not defined
	 * 
	 */
	function request(url, options) {
		
  throw "not yet implemented";
	}

	return {
		defaultFailure : defaultFailure,
		defaultException : defaultException,
		defaultSuccess : noop,
		request : request
	};
});