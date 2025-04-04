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
define ["t5/core/dom", "t5/core/events", "t5/core/messages", "t5/core/ajax", "underscore", "t5/core/datepicker", "t5/core/fields"],
  (dom, events, messages, ajax, _, DatePicker) ->


    # Translate from the provided order (SUNDAY = 0, MONDAY = 1), to
    # the order needed by the DatePicker component (MONDAY = 0 ... SUNDAY = 6)
    serverFirstDay = parseInt messages "date-symbols.first-day"
    datePickerFirstDay = if serverFirstDay is 0 then 6 else serverFirstDay - 1

    # Localize a few other things.
    days = (messages "date-symbols.days").split ","

    # Shuffle sunday to the end, so that monday is first.

    days.push days.shift()

    monthsLabels = (messages "date-symbols.months").split ","
    abbreviateWeekDay = (name) -> name.substr(0, 1).toLowerCase()
    locale = (document.documentElement.getAttribute("data-locale")) || "en"
    if (locale.indexOf 'zh') is 0
      # TAP5-1886, Chinese weekdays cannot be abbreviated using the first character
      abbreviateWeekDay = (name) -> name.substr(name.length-1)
    daysLabels = (abbreviateWeekDay name for name in days)
    todayLabel = messages "core-datefield-today"
    noneLabel = messages "core-datefield-none"


    # Track the active popup; only one allowed at a time. May look to rework this
    # later so that there's just one popup and it is moved around the viewport, or
    # around the DOM.
    activePopup = null


    isPartOfPopup = (element) ->
      element.findParent(".labelPopup")? or element.findParent(".datefield-popup")?

    dom.body.on "click", ->
      if activePopup and not isPartOfPopup @
        activePopup.hide()
        activePopup = null
      return


    class Controller
      constructor: (@container) ->
        @field = @container.findFirst 'input:not([name="t:formdata"])'
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

        ajax (@container.attr "data-parse-url"),
          data:
            input: value
          onerror: (message) =>
            @field.removeClass "ajax-wait"
            @fieldError message

            @showPopup()
            return

          success: (response) =>
            @field.removeClass "ajax-wait"
            reply = response.json

            if reply.result
              @clearFieldError()
              [year, month, day] = reply.result.split '-'

              date = new Date year, month-1, day
              @datePicker.setDate date

            if reply.error
              @fieldError (_.escape reply.error)

              @datePicker.setDate null

            @showPopup()
            return

      fieldError: (message) ->
        @field.focus().trigger events.field.showValidationError, { message }

      clearFieldError: ->
        @field.trigger events.field.clearValidationError

      createPopup: ->
        @datePicker = new DatePicker()
        @datePicker.setFirstWeekDay datePickerFirstDay
        
        @datePicker.setLocalizations monthsLabels, daysLabels, todayLabel, noneLabel
        
        @popup = dom.create("div", { class: "datefield-popup well"}).append @datePicker.create()
        @container.insertAfter @popup

        @datePicker.onselect = _.bind @onSelect, this

      onSelect: ->
        date = @datePicker.getDate()

        if date is null
          @hidePopup()
          @clearFieldError()
          @field.value ""
          return

        @field.addClass "ajax-wait"

        normalizedFormat = "#{date.getFullYear()}-#{date.getMonth()+1}-#{date.getDate()}"
        ajax (@container.attr "data-format-url"),
          data:
            input: normalizedFormat
          failure: (response, message) =>
            @field.removeClass "ajax-wait"
            @fieldError message
          success: (response) =>
            @field.removeClass "ajax-wait"
            @clearFieldError()
            @field.value response.json.result
            @hidePopup()


    # Initialization:

    dom.scanner "[data-component-type='core/DateField']", (container) ->
      # Hide it from later scans
      container.attr "data-component-type", null

      new Controller(container)

    # Exports nothing.
    return null
