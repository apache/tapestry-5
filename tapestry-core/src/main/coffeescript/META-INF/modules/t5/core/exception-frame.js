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

# ## t5/core/exception-frame
#
# Manages a special element used to present an HTML exception report from an Ajax request (where a non-markup response
# was expected, including a partial page render response).
define ["t5/core/dom"], (dom) ->

  write = (container, content) ->
    iframe = (container.findFirst "iframe").element

    # See http://xkr.us/articles/dom/iframe-document/

    iframeDocument = iframe.contentWindow or iframe.contentDocument
    if iframeDocument.document
      iframeDocument = iframeDocument.document

    # Clear current content:
    iframeDocument.open()
    # Write new content:
    iframeDocument.write content
    iframeDocument.close()

  clear = ->
    container = @closest '.exception-container'
    container.remove()
    return false

  create = ->

    container = dom.create
      class: "exception-container"
      """
        <iframe> </iframe>
        <div>
          <button class="pull-right btn btn-primary">
            <i class="icon-remove icon-white"></i>
            Close
          </button>
        </div>
      """

    dom.body.append container.hide()

    container.on "click", "button", clear
    container

  # Export single function:

  (exceptionContent) ->
    container = create()
    write container, exceptionContent
    container.show()
    return