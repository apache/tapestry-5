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

# ## t5/core/exceptionframe
#
# Manages a special element used to present a HTML exception report from an Ajax request (where a non-markup response
# was expected, including a partial page render response).
define ["./dom", "./builder", "underscore"],
  (dom, builder, _) ->
    container = null
    iframe = null
    iframeDocument = null

    write = (content) ->
      # Clear current content:
      iframeDocument.open()
      # Write new content:
      iframeDocument.write content
      iframeDocument.close()

    clear = ->
      write ""
      container.hide()
      return false

    create = ->
      return if container

      container = builder ".t-exception-container",
        ["iframe"],
        ["div > button.pull-right.btn.btn-primary", "Close"]

      dom.body().append container.hide()

      iframe = (container.findFirst "iframe").element

      # See http://xkr.us/articles/dom/iframe-document/

      iframeDocument = iframe.contentWindow or iframe.contentDocument
      if iframeDocument.document
        iframeDocument = iframeDocument.document

      container.on "click", "button", clear

    # Export single function:

    (exceptionContent) ->
      create()
      write exceptionContent
      container.show()