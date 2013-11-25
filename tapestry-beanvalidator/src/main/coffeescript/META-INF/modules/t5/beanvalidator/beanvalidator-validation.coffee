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

# ## t5/beanvalidator/beanvalidator-validation
#
# The awkward name is to accomidate the "docco" documentation tool; it doesn't understand
# having the same named file in multiple folders. See https://github.com/jashkenas/docco/issues/201.
#
# Supports extra validations related to the beanvalidator module.
define ["underscore", "t5/core/dom", "t5/core/events", "t5/core/utils", "t5/core/validation"],
  (_, dom, events, utils) ->

    rangeValue = (element, attribute, defaultValue) ->
      v = element.attr attribute
      if v is null
        defaultValue
      else
        parseInt v

    countOptions = (e) ->
      # A select that is used as part of a palette is different; the validation attributes
      # are attached to the selected (right side) <select>, and anything there counts as part
      # of the selection.
      if e.findParent ".palette"
        e.element.options.length
      else
        # An ordinary <select> may have multiple options (the clumsy control-click way)
        _.filter(e.element.options, (o) -> o.selected).length

    doRangeValidate = (element, value, memo) ->
      min = rangeValue element, "data-range-min", 0
      max = rangeValue element, "data-range-max", Number.MAX_VALUE

      # If the translated value is still a string, and not a number, then the
      # size refers to the length of the string, not its numeric value.
      if _.isString value
        value = value.length

      unless min <= value <= max
        memo.error = (element.attr "data-range-message") or "RANGE ERROR"
        return false

      return true

    dom.onDocument events.field.optional, "[data-optionality=prohibited]", (event, memo) ->

      unless utils.isBlank memo.value
        memo.error = (@attr "data-prohibited-message") or "PROHIBITED"
        return false

      return true

    dom.onDocument events.field.validate, "input[data-range-min], input[data-range-max], textarea[data-range-min], textarea[data-range-max]", (event, memo) ->
      doRangeValidate this, memo.translated, memo

    dom.onDocument events.field.validate, "select[data-range-min], select[data-range-max]", (event, memo) ->
      doRangeValidate this, (countOptions this), memo

    return
