# Copyright 2012-2014 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# ## t5/core/ajax
#
# Exports a single function, that invokes `t5/core/dom:ajaxRequest()` with the provided `url` and a modified version of the
# `options`.
#
# * options.method - "post", "get", etc., default: "post".
# * options.element - if provided, the URL will be treated as a server-side event name
#    and the actual URL to be used will be obtained from dom.getEventUrl(url, element)
# * options.contentType - request content, defaults to "application/x-www-form-urlencoded"
# * options.data - optional, additional key/value pairs (for the default content type)
# * options.success - handler to invoke on success. Passed the ResponseWrapper object.
#   Default does nothing.
# * options.failure - handler to invoke on failure (server responds with a non-2xx code).
#   Passed the response. Default will throw the exception
# * options.exception - handler to invoke when an exception occurs (often means the server is unavailable).
#   Passed the exception. Default will generate an exception message and throw an `Error`.
#   Note: not really supported under jQuery, a hold-over from Prototype.
# * options.complete - handler to invoke after success, falure, or exception. The handler is passed no
#   parameters.
#
# It wraps (or provides) `success`, `exception`, and `failure` handlers, extended to handle a partial page render
# response (for success), or properly log a server-side failure or client-side exception, including using the
# `t5/core/exception-frame` module to display a server-side processing exception.
define ["t5/core/pageinit", "t5/core/dom", "t5/core/exception-frame", "t5/core/console", "underscore"],
  (pageinit, dom, exceptionframe, console, _) ->
    (url, options) ->

      complete = ->
        if options.complete
          options.complete()

        return
        
      if options.hasOwnProperty 'element'
        url = dom.getEventUrl(url, options.element)

      newOptions = _.extend {}, options,

        # Logs the exception to the console before passing it to the
        # provided exception handler or throwing the exception.
        exception: (exception) ->
          console.error "Request to #{url} failed with #{exception}"

          if options.exception
            options.exception exception
          else
            throw exception

          complete()

          return

        failure: (response, failureMessage) ->
          raw = response.header "X-Tapestry-ErrorMessage"
          unless _.isEmpty raw
            message = window.unescape raw
            console.error "Request to #{url} failed with '#{message}'."

            contentType = response.header "content-type"

            isHTML = contentType and (contentType.split(';')[0] is "text/html")

            if isHTML
              exceptionframe response.text
          else
            console.error failureMessage

          options.failure and options.failure(response)

          complete()

          return

        success: (response) ->
          pageinit.handlePartialPageRenderResponse response, options.success

          complete()

          return

      dom.ajaxRequest url, newOptions