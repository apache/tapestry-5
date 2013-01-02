# Copyright 2012, 2013 The Apache Software Foundation
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
# It wraps (or provides) `onsuccess`, `onexception`, and `onfailure` handlers, extended to handle a partial page render
# response (for success), or properly log a server-side failure or client-side exception, including using the
# `t5/core/exceptionframe` module to display a server-side processing exception.
define ["./pageinit", "./dom", "./exceptionframe", "./console", "_"],
  (pageinit, dom, exceptionframe, console, _) ->
    (url, options) ->
      newOptions = _.extend {}, options,

        # Logs the exception to the console before passing it to the
        # provided exception handler or throwing the exception.
        onexception: (exception) ->
          console.error "Request to #{url} failed with #{exception}"

          if options.onexception
            options.onexception exception
          else
            throw exception

        onfailure: (response, failureMessage) ->
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

          options.onfailure and options.onfailure(response)

          return null

        onsuccess: (response) ->
          pageinit.handlePartialPageRenderResponse response, options.onsuccess

      dom.ajaxRequest url, newOptions