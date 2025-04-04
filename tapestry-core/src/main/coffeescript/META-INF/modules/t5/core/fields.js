/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
// Copyright 2012-2013 The Apache Software Foundation
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

// ## t5/core/fields
//
// Module for logic relating to form input fields (input, select, textarea); specifically
// presenting validation errors and perfoming input validation when necessary.
define(["underscore", "t5/core/events", "t5/core/dom", "t5/core/utils", "t5/core/forms"],
  function(_, events, dom, utils) {

    let exports;
    const ensureFieldId = function(field) {
      let fieldId = field.attr("id");

      if (!fieldId) {
        fieldId = _.uniqueId("field");
        field.attr("id", fieldId);
      }

      return fieldId;
    };

    // Finds any `.help-block` used for presenting errors for the provided field.
    // Returns the found block(s) as an array of ElementWrapper. Returns null
    // if no blocks can be found.
    //
    // Normally, you would expect just a single help block for a field, but in some cases,
    // such as to support responsive layout, there will be multiple help blocks for a single field.
    //
    // * field - element wrapper for the field
    const findHelpBlocks = function(field) {
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
        block.attr("data-error-block-for", fieldId);
        return [block];
      }

      // Not found, so perhaps it will be created dynamically.
      return null;
    };

    const createHelpBlock = function(field) {
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
      if (container.hasClass("input-group")) {
        container.insertAfter(block);
      } else {
        field.insertAfter(block);
      }

      return block;
    };

    const showValidationError = (id, message) => dom.wrap(id).trigger(events.field.showValidationError, { message });

    const collectOptionValues = wrapper => _.pluck(wrapper.element.options, "value");

    // Default registrations:

    dom.onDocument(events.field.inputValidation, function(event, formMemo) {

      // Fields that are disabled, or not visible to the user are not subject to
      // validation. Typically, a field will only be invisible due to the
      // core/FormFragment component.
      if (this.element.disabled || (!this.deepVisible())) { return; }

      let failure = false;

      const fieldValue =
        (this.attr("data-value-mode")) === "options" ?
          collectOptionValues(this)
        : this.element.type === "checkbox" ?
          this.checked()
        :
          this.value();

      const memo = {value: fieldValue};
      
      const postEventTrigger = () => {
        if (memo.error) {
          // Assume the event handler displayed the message.
          failure = true;

          if (_.isString(memo.error)) {

            return this.trigger(events.field.showValidationError, { message: memo.error });
          }
        }
      };

      this.trigger(events.field.optional, memo);

      postEventTrigger();

      if (!failure && (!utils.isBlank(memo.value))) {

        this.trigger(events.field.translate, memo);

        postEventTrigger();

        if (!failure) {
            if (_.isUndefined(memo.translated)) {
              memo.translated = memo.value;
            }

            this.trigger(events.field.validate, memo);

            postEventTrigger();
          }
      }

      if (failure) {
        formMemo.error = true;
        this.attr('aria-invalid', 'true');
        this.attr('aria-describedby', this.attr('id') + "-help-block");
      } else {
        this.attr('aria-invalid', 'false');
        this.attr('aria-describedby ', null);
        this.trigger(events.field.clearValidationError);
      }

    });

    dom.onDocument(events.field.clearValidationError, function() {
      const blocks = exports.findHelpBlocks(this);

      for (var block of Array.from(blocks || [])) {
        block.hide().update("");
        block.parent().removeClass("has-error");
        block.attr("role", null);
      }

      const group = this.findParent(".form-group");

      group && group.removeClass("has-error");

    });

    dom.onDocument(events.field.showValidationError, function(event, memo) {
      let blocks = exports.findHelpBlocks(this);

      if (!blocks) {
        blocks = [exports.createHelpBlock(this)];
      }

      for (var block of Array.from(blocks)) {
        block.removeClass("invisible").show().update(memo.message);
        // Add "has-error" to the help-block's immediate container; this assist with some layout issues
        // where the help block can't be under the same .form-group element as the field (more common
        // with a horizontal form layout).
        block.parent().addClass("has-error");
        block.attr("role", "alert");
      }

      const group = this.findParent(".form-group");

      const container = group || this.parent().closest(":not(.input-group)");

      container.addClass("has-error");

    });

    return exports = {findHelpBlocks, createHelpBlock, showValidationError};
});