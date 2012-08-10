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

# core/exceptionframe
#
# Manages a special element used to present a HTML exception report from an Ajax request (where a non-markup response
# was expected, including a partial page render response).

define ["core/spi", "core/builder", "_"],
  (spi, builder, _) ->
    container = null
    iframe = null
    iframeDocument = null

    write = (content) ->
      # Clear current content:
      iframeDocument.open()
      # Write new content:
      iframeDocument.write content
      iframeDocument.close()

    clear = (event) ->
      event.stop()
      write ""
      container.hide()

    # Called after the window has resized to adjust the size of the iframe.
    resize = ->
      dims = spi.viewportDimensions()

      iframe.width = dims.width - 100
      iframe.height = dims.height - 120

    create = ->
      return if container

      container = builder ".t-exception-container",
        ["iframe.t-exception-frame", width: "100%"],
        [".t-exception-controls > span.t-exception-close", "Close"]

      spi.body().append container.hide()

      iframe = (container.find "iframe").element

      # See http://xkr.us/articles/dom/iframe-document/

      iframeDocument = iframe.contentWindow or iframe.contentDocument
      if iframeDocument.document
        iframeDocument = iframeDocument.document

      container.on "click", ".t-exception-close", clear

      spi.on window, "resize", (_.debounce resize, 20)

    # Export single function:

    (exceptionContent) ->
      create()
      write exceptionContent
      resize()
      container.show()