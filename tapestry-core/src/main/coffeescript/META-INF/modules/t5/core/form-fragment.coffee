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


# ## t5/core/form-fragment
#
define ["underscore", "t5/core/dom", "t5/core/events", "t5/core/forms"],
  (_, dom, events) ->

    SELECTOR = "[data-component-type='core/FormFragment']"

    REENABLE = "data-re-enable-when-fragment-visible"

    disableInputFields = (fragment) ->

      # This is an example of where the t5/core/dom abstraction label is problematic,
      # as this is very inefficient vs. the native jQuery approach.
      for field in fragment.find "input:not(:disabled)"
        field.attr "disabled", true
        field.attr REENABLE, true

    renableInputFields = (fragment) ->

      for field in fragment.find "input[#{REENABLE}]"
        field.attr "disabled", null
        field.attr REENABLE, null

    updateFields = (fragment, makeVisible) ->

      # This is a server side option that says the content of the fragment should always be submitted,
      # even if the fragment is not currently visible.
      return if fragment.attr "data-always-submit"

      f = if makeVisible then renableInputFields else disableInputFields

      f fragment

    # Again, a DOM event to make the FormFragment visible or invisible; this is useful
    # because of the didShow/didHide events ... but we're really just seeing the evolution
    # from the old style (the FormFragment class as controller) to the new style (DOM events and
    # top-level event handlers).
    dom.onDocument events.formfragment.changeVisibility, SELECTOR, (event) ->
        makeVisible = event.memo.visible

        this[if makeVisible then "show" else "hide"]()

        updateFields this, makeVisible

        @trigger events.element[if makeVisible then "didShow" else "didHide"]

        return false

    # When a FormFragment is initially rendered as hidden, then we need to do some
    # book-keeping on the client side.
    hide = (id) ->
      field = dom(id)

      updateFields field, false

    # Initializes a trigger for a FormFragment
    #
    # * spec.triggerId - id of checkbox or radio button
    # * spec.fragmentId - id of FormFragment element
    # * spec.invert - (optional) if true, then checked trigger hides (not shows) the fragment
    linkTrigger = (spec) ->
      unless spec.triggerId? then throw new Error "Incomplete parameters, triggerId is null"
      unless spec.fragmentId? then throw new Error "Incomplete parameters, fragmentId is null"
      trigger = dom spec.triggerId
      fragment = dom spec.fragmentId
      if fragment is null
        throw new Error "Invalid configuration, fragment with id #{spec.fragmentId} not found"

      invert = spec.invert or false

      update = ->
        checked = trigger.element.checked
        makeVisible = checked isnt invert

        fragment.trigger events.formfragment.changeVisibility,  visible: makeVisible

        return

      if trigger.element.type is "radio"
        dom.on trigger.element.form, "click", update
      else
        trigger.on "click", update

    # Module exports:
    { linkTrigger, hide }
