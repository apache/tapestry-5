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

# ## t5/core/palette
#
# Support for the `core/Palette` component.
define ["t5/core/dom", "underscore", "t5/core/events"],
  (dom, _, events) ->

    isSelected = (option) -> option.selected

    class PaletteController

      constructor: (id) ->
        @selected = (dom id)
        @container = @selected.findParent ".palette"
        @available = @container.findFirst ".palette-available select"
        @hidden = @container.findFirst "input[type=hidden]"

        @select = @container.findFirst "[data-action=select]"
        @deselect = @container.findFirst "[data-action=deselect]"

        @moveUp = @container.findFirst "[data-action=move-up]"
        @moveDown = @container.findFirst "[data-action=move-down]"

        # Track where reorder is allowed based on whether the buttons actually exist
        @reorder = @moveUp isnt null

        @valueToOrderIndex = {}

        for option,i in @available.element.options
          @valueToOrderIndex[option.value] = i

        # This occurs even when the palette is disabled, to present the
        # values correctly. Otherwise it looks like nothing is selected.
        @initialTransfer()

        unless @selected.element.disabled
          @updateButtons()
          @bindEvents()

      initialTransfer: ->
        # Get the values for options that should move over
        values = JSON.parse @hidden.value()
        valueToPosition = {}

        for v, i in values
          valueToPosition[v] = i

        e = @available.element

        movers = []

        for i in [(e.options.length - 1)..0] by -1
          option = e.options[i]
          value = option.value
          pos = valueToPosition[value]
          unless pos is undefined
            movers[pos] = option
            e.remove i

        for option in movers
          @selected.element.add option

      # Invoked after any change to the selections list to update the hidden field as well as the
      # buttons' state.
      updateAfterChange: ->
        @updateHidden()
        @updateButtons()

      updateHidden: ->
        values = (option.value for option in @selected.element.options)
        @hidden.value JSON.stringify values

      bindEvents: ->
        @container.on "change", "select", =>
          @updateButtons()
          return false

        @select.on "click", =>
          @doSelect()
          return false

        @available.on "dblclick", =>
          @doSelect()
          return false

        @deselect.on "click", =>
          @doDeselect()
          return false

        @selected.on "dblclick", =>
          @doDeselect()
          return false

        if @reorder
          @moveUp.on "click", =>
            @doMoveUp()
            return false

          @moveDown.on "click", =>
            @doMoveDown()
            return false

      # Invoked whenever the selections in either list changes or after an updates; figures out which buttons
      # should be enabled and which disabled.
      updateButtons: ->
        @select.element.disabled = @available.element.selectedIndex < 0

        nothingSelected = @selected.element.selectedIndex < 0

        @deselect.element.disabled = nothingSelected

        if @reorder
          @moveUp.element.disabled = nothingSelected or @allSelectionsAtTop()
          @moveDown.element.disabled = nothingSelected or @allSelectionsAtBottom()

      doSelect: -> @transferOptions @available, @selected, @reorder

      doDeselect: -> @transferOptions @selected, @available, false

      doMoveUp: ->
        options = _.toArray @selected.element.options

        groups = _.partition options, isSelected

        movers = groups[0]

        # The element before the first selected element is the pivot; all the selected elements will
        # move before the pivot. If there is no pivot, the elements are shifted to the front of the list.
        firstMoverIndex = _.first(movers).index
        pivot = options[firstMoverIndex - 1]

        options = groups[1]

        splicePos = if pivot then _.indexOf options, pivot else 0

        movers.reverse()

        for o in movers
          options.splice splicePos, 0, o

        @reorderSelected options


      doMoveDown: ->
        options = _.toArray @selected.element.options

        groups = _.partition options, isSelected

        movers = groups[0]

        # The element after the last selected element is the pivot; all the selected elements will
        # move after the pivot. If there is no pivot, the elements are shifted to the end of the list.
        lastMoverIndex = movers[-1..-1][0].index
        pivot = options[lastMoverIndex + 1]

        options = groups[1]

        splicePos = if pivot then _.indexOf(options, pivot) + 1 else options.length

        movers.reverse()

        for o in movers
          options.splice splicePos, 0, o

        @reorderSelected options

      # Reorders the selected options to the provided list of options; handles triggering the willUpdate and
      # didUpdate events.
      reorderSelected: (options) ->

        @performUpdate true, options, =>

          @deleteOptions @selected

          for o in options
            @selected.element.add o, null

      # Performs the update, which includes the willChange and didChange events.
      performUpdate: (reorder, selectedOptions, updateCallback) ->

        canceled = false

        doUpdate = =>
          updateCallback()

          @selected.trigger events.palette.didChange, { selectedOptions, reorder }

          @updateAfterChange()

        memo =
          selectedOptions: selectedOptions
          reorder: reorder
          cancel: -> canceled = true
          defer: ->
            canceled = true
            return doUpdate

        @selected.trigger events.palette.willChange, memo

        doUpdate() unless canceled

      # Deletes all options from a select (an ElementWrapper), prior to new options being populated in.
      deleteOptions: (select) ->

        e = select.element

        for i in [(e.length - 1)..0] by -1
          e.remove i

      # Moves options between the available and selected lists, including event notifiations before and after.
      transferOptions: (from, to, atEnd) ->

        if from.element.selectedIndex is -1
          return

        # This could be done in a single pass, but:
        movers = _.filter from.element.options, isSelected
        fromOptions = _.reject from.element.options, isSelected

        toOptions = _.toArray to.element.options

        for o in movers
          @insertOption toOptions, o, atEnd

        selectedOptions = if to is @selected then toOptions else fromOptions

        @performUpdate false, selectedOptions, =>
          for i in [(from.element.length - 1)..0] by -1
            if from.element.options[i].selected
              from.element.remove i

          # A bit ugly: update the to select by removing all, then adding back in.

          for i in [(to.element.length - 1)..0] by -1
            to.element.options[i].selected = false
            to.element.remove i

          for o in toOptions
            to.element.add o, null

      insertOption: (options, option, atEnd) ->

        unless atEnd
          optionOrder = @valueToOrderIndex[option.value]
          before = _.find options, (o) => @valueToOrderIndex[o.value] > optionOrder

        if before
          i = _.indexOf options, before
          options.splice i, 0, option
        else
          options.push option


      indexOfLastSelection: (select) ->
        e = select.element
        if e.selectedIndex < 0
          return -1

        for i in [(e.options.length - 1)..(e.selectedIndex)] by -1
          if e.options[i].selected
            return i

        return -1

      allSelectionsAtTop: ->
        last = @indexOfLastSelection @selected
        options = _.toArray @selected.element.options

        _(options[0..last]).all (o) -> o.selected

      allSelectionsAtBottom: ->
        e = @selected.element
        last = e.selectedIndex
        options = _.toArray e.options

        _(options[last..]).all (o) -> o.selected


    # Export just the initializer function
    (id) -> new PaletteController(id)