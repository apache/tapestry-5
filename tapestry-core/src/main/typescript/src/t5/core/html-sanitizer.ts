// Copyright 2025 The Apache Software Foundation
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

/** 
 * ## t5/core/html-sanitizer
 *
 * Provides a function that sanitizes HTML.
 * @packageDocumentation
 */
export default function(html: string) {

  if (html === null || html === undefined) {
    return "";
  }

  // No elements, no problems
  if (html.indexOf("<") < 0) {
    return html;
  }

  // Mostly copied from
  // https://gomakethings.com/how-to-sanitize-html-strings-with-vanilla-js-to-reduce-your-risk-of-xss-attacks/

  let parser = new DOMParser();
  let document = parser.parseFromString(html, "text/html");

  // Remove <script> and <iframe> elements

  let scripts = document.querySelectorAll("script, iframe");
  for (let script of scripts) {
    script.remove();
  }

  let root = document.body;

  clean(root);

  return root.innerHTML;

}

// Recursively cleans the elements
function clean(element: HTMLElement) {
  removeDangerousAttributes(element);
  for (let child of element.children) {
    clean(child as HTMLElement);
  }
}

// Remove on[something] attributes
function removeDangerousAttributes(element: HTMLElement): void {

  let attributes = element.attributes;
  if (attributes != null) {
    for (let {name, value} of attributes) {
      if (isDangerousAttribute(name, value)) {
        element.removeAttribute(name);
      }
    }
  }

}

const DANGEROUS_ATTRIBUTE_NAMES = ['src', 'href', 'xlink:href'];

function isDangerousAttribute(name: string, value: string) {
  value = value.replace(/\s+/g, '').toLowerCase();
  return name.toLocaleLowerCase().startsWith("on") ||
    (DANGEROUS_ATTRIBUTE_NAMES.includes(name) && (
      value.includes("javascript:") ||
      value.includes("data:text/html")
    ));
}
