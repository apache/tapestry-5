# Copyright 2012-2013 The Apache Software Foundation
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

# ## t5/core/fields
#
# Module for logic relating to form input fields (input, select, textarea); specifically
# presenting validation errors and perfoming input validation when necessary.
define ["underscore", "t5/core/events", "t5/core/dom", "t5/core/utils", "t5/core/forms"],
  (_, events, dom, utils) ->

    ensureFieldId = (field) ->
      fieldId = field.attr "id"

      unless fieldId
        fieldId = _.uniqueId "field"
        field.attr "id", fieldId

      return fieldId

    # Finds any `.help-block` used for presenting errors for the provided field.
    # Returns the found block(s) as an array of ElementWrapper. Returns null
    # if no blocks can be found.
    #
    # Normally, you would expect just a single help block for a field, but in some cases,
    # such as to support responsive layout, there will be multiple help blocks for a single field.
    #
    # * field - element wrapper for the field
    findHelpBlocks = (field) ->
      fieldId = field.attr "id"

      # When the field has an id (the normal case!), search the body for
      # the matching help blocks.
      if fieldId
        blocks = dom.body.find "[data-error-block-for='#{fieldId}']"

        return blocks if blocks.length > 0
      else
        # Assign a unique (hopefully!) client id for the field, which will be
        # used to link the field and the new help-block together.
        fieldId = ensureFieldId field

      # Not found by id, but see if an empty placeholder was provided within
      # the same .form-group.

      group = field.findParent ".form-group"

      return null unless group

      # This happens less often, now that the Errors component ensures (at render time)
      # a fieldId and a data-error-block-for element. Even so, sometimes a template
      # will just contain a div.help-block[data-presentation=error]
      block = group.findFirst "[data-presentation=error]"

      if block
        block.attr "data-error-block-for", fieldId
        return [block]

      # Not found, so perhaps it will be created dynamically.
      return null

    createHelpBlock = (field) ->
      fieldId = ensureFieldId field

      # No containing group ... this is a problem, probably an old 5.3 application upgraded to 5.4
      # or beyond.  Place the block just after the field.

      container = field.parent()

      block = dom.create "p",
        class: "help-block"
        "data-error-block-for": fieldId
        "id": fieldId + "-help-block"

      # The .input-group selectors are used to attach buttons or markers to the field.
      # In which case, the help block can go after the group instead.
      if container.hasClass("input-group")
        container.insertAfter block
      else
        field.insertAfter block

      return block

    showValidationError = (id, message) ->
      dom.wrap(id).trigger events.field.showValidationError, { message }

    collectOptionValues = (wrapper) ->
      _.pluck wrapper.element.options, "value"

    # Default registrations:

    dom.onDocument events.field.inputValidation, (event, formMemo) ->

      # Fields that are disabled, or not visible to the user are not subject to
      # validation. Typically, a field will only be invisible due to the
      # core/FormFragment component.
      return if @element.disabled or (not @deepVisible())

      failure = false

      fieldValue =
        if (@attr "data-value-mode") is "options"
          collectOptionValues this
        else if @element.type is "checkbox"
          @checked()
        else
          @value()

      memo = value: fieldValue
      
      postEventTrigger = =>
        if memo.error
          # Assume the event handler displayed the message.
          failure = true

          if _.isString memo.error

            @trigger events.field.showValidationError, { message: memo.error }

      @trigger events.field.optional, memo

      postEventTrigger()

      unless failure or (utils.isBlank memo.value)

        @trigger events.field.translate, memo

        postEventTrigger()

        unless failure
            if _.isUndefined memo.translated
              memo.translated = memo.value

            @trigger events.field.validate, memo

            postEventTrigger()

      if failure
        formMemo.error = true
        this.attr('aria-invalid', 'true');
        this.attr('aria-describedby', this.attr('id') + "-help-block");
      else
        this.attr('aria-invalid', 'false');
        this.attr('aria-describedby ', null);
        @trigger events.field.clearValidationError

      return

    dom.onDocument events.field.clearValidationError, ->
      blocks = exports.findHelpBlocks this

      for block in blocks or []
        block.hide().update("")
        block.parent().removeClass "has-error"
        block.attr("role", null)

      group = @findParent ".form-group"

      group and group.removeClass "has-error"

      return

    dom.onDocument events.field.showValidationError, (event, memo) ->
      blocks = exports.findHelpBlocks this

      unless blocks
        blocks = [exports.createHelpBlock this]

      for block in blocks
        block.removeClass("invisible").show().update(memo.message)
        # Add "has-error" to the help-block's immediate container; this assist with some layout issues
        # where the help block can't be under the same .form-group element as the field (more common
        # with a horizontal form layout).
        block.parent().addClass("has-error")
        block.attr("role", "alert")

      group = @findParent ".form-group"

      container = group or @parent().closest(":not(.input-group)")

      container.addClass "has-error"

      return

    exports = {findHelpBlocks, createHelpBlock, showValidationError}