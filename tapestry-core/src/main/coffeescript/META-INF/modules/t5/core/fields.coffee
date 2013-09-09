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
define ["underscore", "./events", "./dom", "./utils", "./forms"],
  (_, events, dom, utils) ->

    ensureFieldId = (field) ->
      fieldId = field.attribute "id"

      unless fieldId
        fieldId = _.uniqueId "field"
        field.attribute "id", fieldId

      return fieldId

    # Finds a `.help-block` used for presenting errors for the provided field.
    # Returns the found block as an ElementWrapper. May modify attributes of the field
    # or the block to make future
    #
    # * field - element wrapper for the field
    findHelpBlock = (field) ->
      fieldId = field.attribute "id"

      # When the field has an id (the normal case!), search the body for
      # the matching help block.
      if fieldId
        block = dom.body.findFirst "[data-error-block-for='#{fieldId}']"

        return block if block
      else
        # Assign a unique (hopefully!) client id for the field, which will be
        # used to link the field and the label together.
        fieldId = ensureFieldId field

      # Not found by id, but see if an empty placeholder was provided within
      # the same .form-group.

      group = field.findParent ".form-group"

      return null unless group

      block = group.findFirst "[data-presentation=error]"

      if block
        block.attribute "data-error-block-for", fieldId

      return block

    createHelpBlock = (field) ->
      fieldId = ensureFieldId field

      # No containing group ... this is a problem, probably an old 5.3 application upgraded to 5.4
      # or beyond.  Place the block just after the field.

      container = field.parent()

      block = dom.create "p",
        class: "help-block"
        "data-error-block-for": fieldId

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

      # Fields that are disbled, or not visible to the user are not subject to
      # validation. Typically, a field will only be invisible due to the
      # core/FormFragment component.
      return if @element.disabled or (not @deepVisible())

      failure = false

      fieldValue =
        if (@attribute "data-value-mode") is "options"
          collectOptionValues this
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
      else
        @trigger events.field.clearValidationError

      return

    dom.onDocument events.field.clearValidationError, ->
      block = exports.findHelpBlock this

      if block
        block.hide().update("")
        block.parent().removeClass "has-error"

      group = @findParent ".form-group"

      group and group.removeClass "has-error"

      return

    dom.onDocument events.field.showValidationError, (event, memo) ->
      block = exports.findHelpBlock this

      unless block
        block = exports.createHelpBlock this

      block.removeClass("invisible").show().update(memo.message)
      # Add "has-error" to the help-block's immediate container; this assist with some layout issues
      # where the help block can't be under the same .form-group element as the field (more common
      # with a horizontal form layout).
      block.parent().addClass("has-error")

      group = @findParent ".form-group"

      container = group or @parent().closest(":not(.input-group)")

      container.addClass "has-error"

      return

    exports = {findHelpBlock, createHelpBlock, showValidationError}