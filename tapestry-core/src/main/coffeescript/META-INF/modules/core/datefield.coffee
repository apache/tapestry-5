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

# ##core/datefield
#
# Provides support for the `core/DateField` component.
define ["core/dom", "core/events", "core/messages", "core/builder", "core/ajax",
  "core/alert", "_"],
  (dom, events, messages, builder, ajax, alert, _) ->

    # Translate from the provided order (SUNDAY = 0, MONDAY = 1), to
    # the order needed by the DatePicker component (MONDAY = 0 ... SUNDAY = 6)
    serverFirstDay = parseInt messages "date-symbols.first-day"
    datePickerFirstDay = if serverFirstDay is 0 then 6 else serverFirstDay - 1

    # Loalize a few other things.
    DatePicker.months = (messages "date-symbols.months").split ","
    days = (messages "date-symbols.days").split ","
    DatePicker.days = _.map days, (name) -> name.substr(0, 1).toLowerCase()

    class Controller
      constructor: (@container) ->
        @field = @container.findFirst "input"
        @trigger = @container.findFirst "button"

        @trigger.on "click", =>
          @doTogglePopup()
          false

      doTogglePopup: ->
        return if @field.element.disabled

        unless @popup
          @createPopup()
        else if @popup.visible()
          @popup.hide()
          return

        value = @field.value()

        if value is ""
          @datePicker.setDate null
          @popup.show()
          return

        @field.addClass "ajax-wait"

        ajax (@container.attribute "data-parse-url"),
          parameters:
            input: value
          onerror: =>
            @field.removeClass "ajax-wait"
            @field.focus()

          onsuccess: (response) =>
            @field.removeClass "ajax-wait"
            reply = response.responseJSON

            if reply.result
              date = new Date()
              date.setTime reply.result
              @datePicker.setDate date
              @popup.show()
              return

            @field.focus()

            @fieldError reply.error

      fieldError: (message) ->
        alert { message }

      createPopup: ->
        @datePicker = new DatePicker()
        @popup = builder "div.t-datefield-popup"
        @popup.append dom @datePicker.create()
        @trigger.insertAfter @popup

        @datePicker.onselect = _.bind @onSelect, this

      onSelect: ->
        @field.addClass "t-ajax-wait"

        date = @datePicker.getDate()

        if date is null
          @popup.hide()
          @field.value ""
          return

        ajax (@container.attribute "data-format-url"),
          parameters:
            input: date.getTime()
          onerror: (message) =>
            @field.removeClass "t-ajax-wait"
            @fieldError message
            @popup.hide()
          onsuccess: (response) =>
            @field.removeClass "t-ajax-wait"
            @field.value response.responseJSON.result
            @popup.hide()



    scan = (root) ->
      for container in root.find "[data-component-type=core/DateField]"
        # Hide it from later scans
        container.attribute "data-component-type", null

        new Controller(container)

    # Initialization:

    scan dom.body()

    # And scan any newly added content:

    dom.onDocument events.zone.didUpdate, -> scan this

    # Exports nothing.
    return null