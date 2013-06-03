# Copyright 2012, 2013 The Apache Software Foundation
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

# ## t5/core/datefield
#
# Provides support for the `core/DateField` component.
define ["./dom", "./events", "./messages", "./builder", "./ajax",
  "underscore", "./fields"],
  (dom, events, messages, builder, ajax, _) ->


    # Translate from the provided order (SUNDAY = 0, MONDAY = 1), to
    # the order needed by the DatePicker component (MONDAY = 0 ... SUNDAY = 6)
    serverFirstDay = parseInt messages "date-symbols.first-day"
    datePickerFirstDay = if serverFirstDay is 0 then 6 else serverFirstDay - 1

    # Loalize a few other things.
    DatePicker.months = (messages "date-symbols.months").split ","
    days = (messages "date-symbols.days").split ","

    # Shuffle sunday to the end, so that monday is first.

    days.push days.shift()

    DatePicker.days = _.map days, (name) -> name.substr(0, 1).toLowerCase()

    DatePicker.TODAY = messages "core-datefield-today"
    DatePicker.NONE = messages "core-datefield-none"

    # Track the active popup; only one allowed at a time. May look to rework this
    # later so that there's just one popup and it is moved around the viewport, or
    # around the DOM.
    activePopup = null

    class Controller
      constructor: (@container) ->
        @field = @container.findFirst "input"
        @trigger = @container.findFirst "button"

        @trigger.on "click", =>
          @doTogglePopup()
          false

      showPopup: ->
        if activePopup and activePopup isnt @popup
          activePopup.hide()

        @popup.show()
        activePopup = @popup

      hidePopup: ->
        @popup.hide()
        activePopup = null

      doTogglePopup: ->
        return if @field.element.disabled

        unless @popup
          @createPopup()
          activePopup?.hide()
        else if @popup.visible()
          @hidePopup()
          return

        value = @field.value()

        if value is ""
          @datePicker.setDate null
          @showPopup()
          return

        @field.addClass "ajax-wait"

        ajax (@container.attribute "data-parse-url"),
          parameters:
            input: value
          onerror: (message) =>
            @field.removeClass "ajax-wait"
            @fieldError message

          success: (response) =>
            @field.removeClass "ajax-wait"
            reply = response.json

            if reply.result
              @clearFieldError()

              date = new Date()
              date.setTime reply.result
              @datePicker.setDate date
              @showPopup()
              return

            @fieldError (dom.escapeHTML reply.error)
            @hidePopup()
            return

      fieldError: (message) ->
        @field.focus().trigger events.field.showValidationError, { message }

      clearFieldError: ->
        @field.trigger events.field.clearValidationError

      createPopup: ->
        @datePicker = new DatePicker()
        @datePicker.setFirstWeekDay datePickerFirstDay
        @popup = builder "div.t-datefield-popup"
        @popup.append dom @datePicker.create()
        @container.append @popup

        @datePicker.onselect = _.bind @onSelect, this

      onSelect: ->
        date = @datePicker.getDate()

        if date is null
          @hidePopup()
          @clearFieldError()
          @field.value ""
          return

        @field.addClass "ajax-wait"


        ajax (@container.attribute "data-format-url"),
          parameters:
            input: date.getTime()
          failure: (response, message) =>
            @field.removeClass "ajax-wait"
            @fieldError message
          success: (response) =>
            @field.removeClass "ajax-wait"
            @clearFieldError()
            @field.value response.json.result
            @hidePopup()


    scan = (root) ->
      for container in root.find "[data-component-type='core/DateField']"
        # Hide it from later scans
        container.attribute "data-component-type", null

        new Controller(container)

    # Initialization:

    scan dom.body()

    # And scan any newly added content:

    dom.onDocument events.zone.didUpdate, -> scan this

    # Exports nothing.
    return null