/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
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

// ## t5/core/exception-frame
//
// Manages a special element used to present an HTML exception report from an Ajax request (where a non-markup response
// was expected, including a partial page render response).
import dom from "t5/core/dom";

const write = function(container, content) {
  const iframe = (container.findFirst("iframe")).element;

  // See http://xkr.us/articles/dom/iframe-document/

  let iframeDocument = iframe.contentWindow || iframe.contentDocument;
  if (iframeDocument.document) {
    iframeDocument = iframeDocument.document;
  }

  // Clear current content:
  iframeDocument.open();
  // Write new content:
  iframeDocument.write(content);
  return iframeDocument.close();
};

const clear = function() {
  const container = this.closest('.exception-container');
  container.remove();
  return false;
};

const create = function() {

  const container = dom.create(
    {class: "exception-container"},
    `\
<iframe> </iframe>
<div>
  <button class="pull-right btn btn-primary">
    <i class="icon-remove icon-white"></i>
    Close
  </button>
</div>\
`
  );

  dom.body.append(container.hide());

  container.on("click", "button", clear);
  return container;
};

// Export single function:

export default function(exceptionContent) {
  const container = create();
  write(container, exceptionContent);
  container.show();
};