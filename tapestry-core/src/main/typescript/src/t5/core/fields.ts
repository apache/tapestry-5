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

/**
 * ## t5/core/fields
 *
 * Module for logic relating to form input fields (input, select, textarea); specifically
 * presenting validation errors and perfoming input validation when necessary.
 * @packageDocumentation
 */

import _ from "underscore"
import events from "t5/core/events";
import dom from "t5/core/dom";
import utils from  "t5/core/utils";
import forms from "t5/core/forms";
import { ElementWrapper, EventWrapper } from "t5/core/types";

let exports_;
const ensureFieldId = function(field: ElementWrapper): string {
  let fieldId = field.attr("id");

  if (!fieldId) {
    fieldId = _.uniqueId("field");
    field.attr("id", fieldId);
  }

  return fieldId as string;
};

// Finds any `.help-block` used for presenting errors for the provided field.
// Returns the found block(s) as an array of ElementWrapper. Returns null
// if no blocks can be found.
//
// Normally, you would expect just a single help block for a field, but in some cases,
// such as to support responsive layout, there will be multiple help blocks for a single field.
//
// * field - element wrapper for the field
const findHelpBlocks = function(field: ElementWrapper) {
  let fieldId = field.attr("id");

  // When the field has an id (the normal case!), search the body for
  // the matching help blocks.
  if (fieldId) {
    const blocks = dom.body.find(`[data-error-block-for='${fieldId}']`);

    if (blocks.length > 0) { return blocks; }
  } else {
    // Assign a unique (hopefully!) client id for the field, which will be
    // used to link the field and the new help-block together.
    fieldId = ensureFieldId(field);
  }

  // Not found by id, but see if an empty placeholder was provided within
  // the same .form-group.

  const group = field.findParent(".form-group");

  if (!group) { return null; }

  // This happens less often, now that the Errors component ensures (at render time)
  // a fieldId and a data-error-block-for element. Even so, sometimes a template
  // will just contain a div.help-block[data-presentation=error]
  const block = group.findFirst("[data-presentation=error]");

  if (block) {
    block.attr("data-error-block-for", fieldId as string);
    return [block];
  }

  // Not found, so perhaps it will be created dynamically.
  return null;
};

const createHelpBlock = function(field: ElementWrapper) {
  const fieldId = ensureFieldId(field);

  // No containing group ... this is a problem, probably an old 5.3 application upgraded to 5.4
  // or beyond.  Place the block just after the field.

  const container = field.parent();

  const block = dom.create("p", {
    class: "help-block form-text text-danger",
    "data-error-block-for": fieldId,
    "id": fieldId + "-help-block"
  }
  );

  // The .input-group selectors are used to attach buttons or markers to the field.
  // In which case, the help block can go after the group instead.
  if (container!.hasClass("input-group")) {
    container!.insertAfter(block);
  } else {
    field.insertAfter(block);
  }

  return block;
};

const showValidationError = (id: string, message: string) => dom.wrap(id)!.trigger(events.field.showValidationError, { message });

// @ts-ignore
const collectOptionValues = (wrapper: ElementWrapper) => _.pluck(wrapper.element.options, "value");

// Default registrations:

dom.onDocument(events.field.inputValidation, null, function(element: ElementWrapper, event: EventWrapper, formMemo: any) {

  // Fields that are disabled, or not visible to the user are not subject to
  // validation. Typically, a field will only be invisible due to the
  // core/FormFragment component.  
  // @ts-ignore
  if (element.element.disabled || (!element.deepVisible())) { return; }

  let failure = false;

  const fieldValue =
    (element.attr("data-value-mode")) === "options" ?
      collectOptionValues(element)
      // @ts-ignore
    : element.element.type === "checkbox" ?
      element.checked()
    :
      element.value();

  const memo = {value: fieldValue};
  
  const postEventTrigger = () => {
    // @ts-ignore
    if (memo.error) {
      // Assume the event handler displayed the message.
      failure = true;

      // @ts-ignore
      if (_.isString(memo.error)) {

        // @ts-ignore
        return element.trigger(events.field.showValidationError, { message: memo.error });
      }
    }
  };

  element.trigger(events.field.optional, memo);

  postEventTrigger();

  if (!failure && (!utils.isBlank(memo.value))) {

    element.trigger(events.field.translate, memo);

    postEventTrigger();

    if (!failure) {
        // @ts-ignore
        if (_.isUndefined(memo.translated)) {
          // @ts-ignore
          memo.translated = memo.value;
        }

        element.trigger(events.field.validate, memo);

        postEventTrigger();
      }
  }

  if (failure) {
    formMemo.error = true;
    element.attr('aria-invalid', 'true');
    element.attr('aria-describedby', element.attr('id') + "-help-block");
  } else {
    element.attr('aria-invalid', 'false');
    element.attr('aria-describedby ', null);
    element.trigger(events.field.clearValidationError);
  }

});

dom.onDocument(events.field.clearValidationError, null, function(element: ElementWrapper) {
  const blocks = findHelpBlocks(element);

  for (var block of Array.from(blocks || [])) {
    block.hide().update("");
    block.parent()!.removeClass("has-error");
    block.attr("role", null);
  }

  const group = element.findParent(".form-group");

  group && group.removeClass("has-error");

});

dom.onDocument(events.field.showValidationError, null, function(element: ElementWrapper, event: EventWrapper, memo: any) {
  let blocks = findHelpBlocks(element);

  if (!blocks) {
    blocks = [createHelpBlock(element)];
  }

  for (var block of Array.from(blocks)) {
    block.removeClass("invisible").show().update(memo.message);
    // Add "has-error" to the help-block's immediate container; this assist with some layout issues
    // where the help block can't be under the same .form-group element as the field (more common
    // with a horizontal form layout).
    block.parent()!.addClass("has-error");
    block.attr("role", "alert");
  }

  const group = element.findParent(".form-group");

  const container = group || element.parent()!.closest(":not(.input-group)");

  container!.addClass("has-error");

});

export default {findHelpBlocks, createHelpBlock, showValidationError};