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


# ##core/form-fragment
#
define ["core/spi", "core/events", "core/compat/tapestry"],
  (spi, events) ->

    # Initializes a FormFragment element
    #
    # * spec.element - id of fragment's element
    # * spec.bound - (optional) reference to function that determines bound
    #   (used with `core/spi:EventWrapper.deepVisible()`)
    # * spec.alwaysSubmit - (optional) if true, then fields inside the fragment submit their values
    #   even when the fragment is not visible. If false (default), then field data is not submitted.
    initFragment = (spec) ->
      element = spi spec.element
      hidden = spi "#{spec.element}-hidden"
      form = spi hidden.element.form

      opts = spec.bound and { bound: spec.bound} or null

      unless spec.alwaysSubmit
        hidden.element.disabled = ! element.deepVisible opts

      updateUI = (makeVisible) ->
        unless spec.alwaysSubmit
          hidden.element.disabled = ! (makeVisible and element.container().deepVisible opts)

        element[if makeVisible then "show" else "hide"]()

        element.trigger events.element[if makeVisible then "didShow" else "didHide"]

      element.on Tapestry.CHANGE_VISIBILITY_EVENT, (event) ->
        event.stop()

        makeVisible = event.memo.visible

        unless makeVisible is element.visible()
          updateUI makeVisible

      element.on Tapestry.HIDE_AND_REMOVE_EVENT, (event) ->
        event.stop()
        element.remove()

    # Initializes a trigger for a FormFragment
    #
    # * spec.triggerId - id of checkbox or radio button
    # * spec.fragmentId - id of FormFragment element
    # * spec.invert - (optional) if true, then checked trigger hides (not shows) the fragment
    linkTrigger = (spec) ->
      trigger = spi spec.triggerId
      invert = spec.invert or false

      update = ->
        checked = trigger.element.checked
        makeVisible = checked isnt invert

        (spi spec.fragmentId).trigger Tapestry.CHANGE_VISIBILITY_EVENT,  visible: makeVisible

      if trigger.element.type is "radio"
        spi.on trigger.element.form, "click", update
      else
        trigger.on "click", update

    { initFragment, linkTrigger }
