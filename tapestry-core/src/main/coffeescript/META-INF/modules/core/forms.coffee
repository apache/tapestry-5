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

# ## core/forms
#
# Defines handlers for HTML forms and HTML field elements, specifically to control input validation.

define ["core/events", "core/spi", "core/builder", "core/compat/tapestry"],
  (events, spi, builder) ->

    SKIP_VALIDATION = "data-skip-validation"

    isPreventSubmission = (element) ->
      (element.hasClass Tapestry.PREVENT_SUBMISSION) or
      (element.getAttribute "data-prevent-submission")

    clearSubmittingHidden = (form) ->
      hidden = form.find "[name='t:submit']"

      hidden.setValue null if hidden

      return

    setSubmittingHidden = (form, wrapper) ->
      hidden = form.find "[name='t:submit']"

      unless hidden
        firstHidden = form.find "input[type=hidden]"
        hidden = builder "input", type:"hidden", name:"t:submit"
        firstHidden.insertBefore hidden

      # TODO: Research why we need id and name and get rid of one if possible.
      value = Object.toJSON [ wrapper.element.id, wrapper.element.name ]

      hidden.setValue value

    defaultValidateAndSubmit = (event) ->

      if ((this.getAttribute "data-validate") is "submit") and
         (not this.getAttribute SKIP_VALIDATION)

        this.removeAttribute SKIP_VALIDATION

        memo = error: false

        for field in this.findAll "[data-validation]"
           field.trigger events.field.validate, memo

        # Only do form validation if all individual field validation
        # was successful.
        this.trigger events.form.validateForm, memo unless memo.error

        if memo.error
          clearSubmittingHidden this
          event.stop()
          return

      # Allow certain types of elements to do last-moment set up. Basically, this is for
      # FormFragment, or similar, to make their t:hidden field enabled or disabled to match
      # their UI's visible/hidden status. This is assumed to work.
      this.trigger events.form.prepareForSubmit, this

      # Sometimes we want to submit the form normally, for a full-page render.
      # Othertimes we want to stop here and let the `events.form.processSubmit`
      # handler take it from here.
      if isPreventSubmission this
        event.stop()
        this.trigger events.form.processSubmit, this

      # Otherwise, the event is good, there are no validation problems, let the normal processing commence.
      return

    spi.domReady ->
      # TODO: May want to define a data attribute to control whether Tapestry gets
      # involved at all?
      spi.body().on "submit", "form", defaultValidateAndSubmit

      # On any click on a submit or image, update the containing form to indicate that the element
      # was responsible for the eventual submit; this is very important to Ajax updates, otherwise the
      # information about which control triggered the submit gets lost.
      spi.body().on "click", "input[type=submit], input[type=image]", ->
        setSubmittingHidden (spi this.element.form), this

    exports =
      setSubmittingElement: (form, element) ->
        setSubmittingHidden form, element

      skipValidation: (formWrapper) ->
        formWrapper.setAttribute SKIP_VALIDATION, true