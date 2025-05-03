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
 * ## t5/core/form-fragment
 * 
 * JavaScript code for the `core/FormFragment` component.
 */
import _ from "underscore";
import dom from "t5/core/dom";
import events from "t5/core/events";
import forms from  "t5/core/forms";
import { ElementWrapper, EventWrapper }from "t5/core/types";

const SELECTOR = "[data-component-type='core/FormFragment']";

const REENABLE = "data-re-enable-when-fragment-visible";

const disableInputFields = (fragment: ElementWrapper) => // This is an example of where the t5/core/dom abstraction label is problematic,
// as this is very inefficient vs. the native jQuery approach.
(() => {
  const result = [];
  for (var field of Array.from(fragment.find("input:not(:disabled)"))) {
    field.attr("disabled", true);
    result.push(field.attr(REENABLE, true));
  }
  return result;
})();

const renableInputFields = (fragment: ElementWrapper) => (() => {
  const result = [];
  for (var field of Array.from(fragment.find(`input[${REENABLE}]`))) {
    field.attr("disabled", null);
    result.push(field.attr(REENABLE, null));
  }
  return result;
})();

const updateFields = function(fragment: ElementWrapper, makeVisible: boolean) {

  // This is a server side option that says the content of the fragment should always be submitted,
  // even if the fragment is not currently visible.
  if (fragment.attr("data-always-submit")) { return; }

  const f = makeVisible ? renableInputFields : disableInputFields;

  return f(fragment);
};

// Again, a DOM event to make the FormFragment visible or invisible; this is useful
// because of the didShow/didHide events ... but we're really just seeing the evolution
// from the old style (the FormFragment class as controller) to the new style (DOM events and
// top-level event handlers).
dom.onDocument(events.formfragment.changeVisibility, SELECTOR, function(element: ElementWrapper, event: EventWrapper) {
    const makeVisible = event.memo.visible;

    element[makeVisible ? "show" : "hide"]();

    updateFields(element, makeVisible);

    element.trigger(events.element[makeVisible ? "didShow" : "didHide"]);

    return false;
});

// When a FormFragment is initially rendered as hidden, then we need to do some
// book-keeping on the client side.
const hide = function(id: string) {
  const field = dom(id);

  return updateFields(field!, false);
};

// Initializes a trigger for a FormFragment
//
// * spec.triggerId - id of checkbox or radio button
// * spec.fragmentId - id of FormFragment element
// * spec.invert - (optional) if true, then checked trigger hides (not shows) the fragment
const linkTrigger = function(spec: { triggerId: string | HTMLElement | null; fragmentId: string | HTMLElement | null; invert: boolean; }) {
  if (spec.triggerId == null) { throw new Error("Incomplete parameters, triggerId is null"); }
  if (spec.fragmentId == null) { throw new Error("Incomplete parameters, fragmentId is null"); }
  const trigger = dom(spec.triggerId);
  const fragment = dom(spec.fragmentId);
  const element = trigger!.element as HTMLInputElement;
  if (fragment === null) {
    throw new Error(`Invalid configuration, fragment with id ${spec.fragmentId} not found`);
  }

  const invert = spec.invert || false;

  const update = function() {
    const checked = element.checked;
    const makeVisible = checked !== invert;

    fragment.trigger(events.formfragment.changeVisibility,  {visible: makeVisible});

  };

  if (element.type === "radio") {
    return dom.on(element.form!, "click", null, update);
  } else {
    return trigger!.on("click", null, update);
  }
};

// Module exports:
export default { linkTrigger, hide };
