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

# ## t5/core/fields
#
# Module for logic relating to form input fields (input, select, textarea); specifically
# presenting validation errors and perfoming input validation when necessary.
define ["underscore", "./events", "./dom", "./builder", "./utils", "./forms"],
  (_, events, dom, builder, utils) ->

    ensureFieldId = (field) ->
      fieldId = field.attribute "id"

      unless fieldId
        fieldId = _.uniqueId "field"
        field.attribute "id", fieldId

      return fieldId

    # Finds a `p.help-block` used for presenting errors for the provided field.
    # Returns the found block as an ElementWrapper. May modify attributes of the field
    # or the block to make future
    #
    # * field - element wrapper for the field
    findHelpBlock = (field) ->
      fieldId = field.attribute "id"

      # When the field has an id (the normal case!), search the body for
      # the matching help block.
      if fieldId
        block = dom.body().findFirst "[data-error-block-for='#{fieldId}']"

        return block if block
      else
        # Assign a unique (hopefully!) client id for the field, which will be
        # used to link the field and the label together.
        fieldId = ensureFieldId field

      # Not found by id, but see if an empty placeholder was provided within
      # the same .controls or .control-group.

      group = field.findParent ".controls, .control-group"

      return null unless group

      block = group.findFirst "[data-presentation=error]"

      if block

        unless fieldId
          # Assign a unique (hopefully!) client id for the field, which will be
          # used to link the field and the label together.
          fieldId = _.uniqueId "field"
          field.attribute "id", fieldId

        block.attribute "data-error-block-for", fieldId

      return block

    createHelpBlock = (field) ->
      fieldId = ensureFieldId field

      # No containing group ... this is a problem, probably an old 5.3 application upgraded to 5.4
      # or beyond.  Place the block just after the field.

      container = field.parent()

      block = builder "p.help-block", "data-error-block-for": fieldId

      # The .input-append and .input-prepend are used to attach buttons or markers to the field.
      # In which case, the block can go
      if container.hasClass("input-append") or container.hasClass("input-prepend")
        container.insertAfter block
      else
        field.insertAfter block

      return block

    # This may be removed soon:
    showValidationError = (id, message) ->
      dom.wrap(id).trigger events.field.showValidationError, { message }

    collectOptionValues = (wrapper) ->
      _.pluck wrapper.element.options, "value"

    # Default registrations:

    dom.onDocument events.field.inputValidation, (event, formMemo) ->

      # When not visible to the user, ignore the input validation. Components
      # are generally configured so that they do not submit a value to the server
      # when not visible ... this is what the core/FormFragment component is responsible
      # for.
      return unless @deepVisible()

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

      group = @findParent ".control-group"

      group and group.removeClass "error"

      return

    dom.onDocument events.field.showValidationError, (event, memo) ->
      block = exports.findHelpBlock this

      unless block
        block = exports.createHelpBlock this

      block.show().update(memo.message)

      group = @findParent ".control-group"

      group and group.addClass "error"

    exports = {findHelpBlock, createHelpBlock, showValidationError}