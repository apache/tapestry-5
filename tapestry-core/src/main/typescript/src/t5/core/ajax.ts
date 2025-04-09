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

/** t5/core/ajax: exports a single function, that invokes `t5/core/dom:ajaxRequest()` with the provided `url` and a modified version of the
 * `options`.
 *
 * * options.method - "post", "get", etc., default: "post".
 * * options.element - if provided, the URL will be treated as a server-side event name
 *    and the actual URL to be used will be obtained from dom.getEventUrl(url, element)
 * * options.contentType - request content, defaults to "application/x-www-form-urlencoded"
 * * options.data - optional, additional key/value pairs (for the default content type)
 * * options.success - handler to invoke on success. Passed the ResponseWrapper object.
 *   Default does nothing.
 * * options.failure - handler to invoke on failure (server responds with a non-2xx code).
 *   Passed the response. Default will throw the exception
 * * options.exception - handler to invoke when an exception occurs (often means the server is unavailable).
 *   Passed the exception. Default will generate an exception message and throw an `Error`.
 *   Note: not really supported under jQuery, a hold-over from Prototype.
 * * options.complete - handler to invoke after success, falure, or exception. The handler is passed no
 *   parameters.
 * It wraps (or provides) `success`, `exception`, and `failure` handlers, extended to handle a partial page render
 * response (for success), or properly log a server-side failure or client-side exception, including using the
 * `t5/core/exception-frame` module to display a server-side processing exception.
 * @packageDocumentation
 */
import pageinit from "t5/core/pageinit.js";
import dom from "t5/core/dom.js";
import exceptionframe from "t5/core/exception-frame.js";
import console from "t5/core/console.js";
import _ from "underscore";

export default function(url: string, options?: any) {
  const complete = function() {
    if (options.complete) {
      options.complete();
    }

  };
    
  if (options.hasOwnProperty('element')) {
    const eventUrl = dom.getEventUrl(url, options.element);
    if (eventUrl == null) {
      throw new Error(`URL not found for event ${url}`);
    }
    url = eventUrl;
  }

  const newOptions = _.extend({}, options, {

    // Logs the exception to the console before passing it to the
    // provided exception handler or throwing the exception.
    exception(exception: any) {
      console.error(`Request to ${url} failed with ${exception}`);

      if (options.exception) {
        options.exception(exception);
      } else {
        throw exception;
      }

      complete();

    },

    failure(response: any, failureMessage: string) {
      const raw = response.header("X-Tapestry-ErrorMessage");
      if (!_.isEmpty(raw)) {
        const message = window.unescape(raw);
        console.error(`Request to ${url} failed with '${message}'.`);

        const contentType = response.header("content-type");

        const isHTML = contentType && (contentType.split(';')[0] === "text/html");

        if (isHTML) {
          exceptionframe(response.text);
        }
      } else {
        console.error(failureMessage);
      }

      options.failure && options.failure(response);

      complete();

    },

    success(response: any) {
      pageinit.handlePartialPageRenderResponse(response, options.success);

      complete();

    }
  }
  );

  return dom.ajaxRequest(url, newOptions);
};