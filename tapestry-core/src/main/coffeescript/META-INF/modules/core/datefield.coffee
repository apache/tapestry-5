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
define ["core/dom", "core/events", "core/messages", "core/builder", "_"],
  (dom, events, messages, builder, _) ->

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
          return

        if @popup.visible()
          @popup.hide()
        else
          @popup.show()

      createPopup: ->
        @datePicker = new DatePicker()
        @popup = builder "div.t-datefield-popup"
        @popup.append dom @datePicker.create()
        @trigger.insertAfter @popup

      # @popup.absolutize().hide()

      positionPopup: ->
        reference = @container.findFirst "input[type=text], button"

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