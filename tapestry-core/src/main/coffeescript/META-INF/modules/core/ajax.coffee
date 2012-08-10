# Copyright 2012 The Apache Software Foundation
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

# ##core/ajax
#
# Exports a single function, that invokes `core/spi:ajaxRequest()` with the provided `url` and a modified version of the
# `options`.
#
# It wraps (or provides) `onsuccess` and `onfailure` elements, extended to handle a partial page render reponse (for
# success), or properly log a server-side failure, including using `core/exceptionframe` module to display a server-side
# processing exception.
define ["core/pageinit", "core/spi", "core/exceptionframe", "core/console", "_"],
  (pageinit, spi, exceptionframe, console, _) ->
    (url, options = {}) ->
      newOptions = _.extend {}, options,
        onfailure: (response) ->
          raw = response.getHeader "X-Tapestry-ErrorMessage"
          unless _.isEmpty raw
            message = window.unescape raw
            console.error "Request to #{url} failed with '#{message}'."

            contentType = response.getHeader "content-type"

            isHTML = contentType and (contentType.split(';')[0] is "text/html")

            if isHTML
              exceptionframe response.responseText
          else
            message = "Request to #{url} failed with status #{response.getStatus()}"
            text = response.getStatusText()
            if not _.isEmpty text
              message += " -- #{text}"

            console.error message + "."

          options.onfailure and options.onfailure(response)

          return null

        onsuccess: (response) ->
          pageinit.handlePartialPageRenderResponse response, options.onsuccess

      spi.ajaxRequest url, newOptions