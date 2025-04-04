# Copyright 2012-2014 The Apache Software Foundation
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

# ## t5/core/events
#
# This module defines logical names for all events that Tapestry-controlled elements
# trigger or listener for. Prototype requires that all custom events have a namespace prefix; jQuery appears to
# allow it without issue.
define

  # Defines events related to the validation and submission of forms. See module `t5/core/forms` for further details.
  # All events are triggered on a specific HTML `<form>` element, and top-level handlers take it from there.
  form:
    # Triggered after fields have been validated, when there are no field validation exceptions, to allow for
    # cross-form validation. Passed a memo object: the handler should set the `error` property of the memo
    # to true to indicate a validation exception occured, and the form submission should be prevented.
    validate: "t5:form:validate"

    # Triggered after fields and form have been validated, when there are field or form validation exceptions.
    validateInError: "t5:form:validateInError"

    # Triggered after `validate` (when there are no prior validation exceptions), to allow certain elements
    # to configure themselves immediately before the form is submitted. This exists primarily for components such
    # as FormFragment, which will enable or disable a hidden field to match the visibility of the fragment.
    # There is no event memo.
    prepareForSubmit: "t5:form:prepare-for-submit"

  # Events releated to form input fields. Primarily, these events are related to form input validation.
  # Validating a field involves three major steps:
  #
  # * optional - check for a required field that has no value
  # * translate - translate a string to another representation, such as `Date`, or a number
  # * validate - validate the field against any number of other constraints (such as ranges)
  #
  # A field that is blank but not required is considered valid: the translate and validate steps are skipped.
  #
  # When a validation error occurs, the event handler should present the validation error (see below), but also
  # return `false`. This will prevent the event from propogating to other event handlers (Tapestry only supports
  # a single validation exception per field).
  #
  # Presenting validation error: The event handler has two options for indicating a validation failure
  # at any of the three steps:
  #
  # * set the `error` property of the memo to true, and trigger the `showValidationError` event (or otherwise
  #   make the validation error visible)
  # * set the `error` property of the memo to the message to display; this will indicate a failure, and the
  #   `showValidationError` event will be triggered automatically.
  # * In addition, return `false` to prevent the event bubbling (see note above).
  field:

    # Perform the optionality check. The event memo includes a `value` property. If the field is required
    # but the value is blank, then a validation error should be presented (as described above). The `value`
    # property of the memo is as described for the `translate` event.
    optional: "t5:field:optional"

    # Trigged by the field if there is a field value (a non-empty string, or a non-empty array in the case
    # of a select element). The event memo includes the field's value as the `value` property.
    # For text fields, the value is the text inside the field. For select elements, it is an array of the values
    # of selected options. If the element has the attribute `data-value-mode` set to 'options', then the
    # value will be the array of all options (selected or not; this is provided for the core/Palette Tapestry
    # component).
    #
    # An event handler may update the event, setting the `translated` property to an alternate formatting, or
    # alternate representation (e.g., `Date`, or a number) for the input value. If the input can not be translated,
    # then a validation error should be presented (as described above).
    translate: "t5:field:translate"

    # Triggered by the field if there is a field value, and the `translate` event succeeded. The event memo
    # includes a `value' property, and a `translated` property. If any constraints on the field are invalid,
    # then the event handler should be presented (as described above).
    validate: "t5:field:validate"

    # Triggered by the form on all enclosed elements with the `data-validation` attribute (indicating they are
    # interested in participating with user input validation). The default implementation fires a series of
    # events: `optional`, `translate`, `validate`. The latter two are always skipped if the input is blank, or if
    # a preceding event set the memo's `error` property to true.  If all events complete without setting an error,
    # then the `clearValidationError` event is triggered, to remove any validation errors from previous
    # validation cycles.
    #
    # This event is passed a memo object; it should set the memo's `error` property to true if validation failed
    # for the field.
    inputValidation: "t5:field:input-validation"

    # Clears and hides the element used to display validation error messages. There is no memo for
    # this event. The p.help-block for the field is located (if it exists) and emptied and hidden.
    # The containing .form-group element (if it exists) has its "has-error" class name removed.
    clearValidationError: "t5:field:clear-validation-error"

    # Presents a validation error for a field. The event memo should have a `message` key; the message to present
    # (as a string, or even as a detached DOM element). The help block for the field will be located or created,
    # made visible, and have its content updated to `memo.message`.  If a containing element has the class ".form-group",
    # then the class "has-error" will be added; otherwise, the immediately containing element will have class "has-error"
    # added. The latter handles the case where, for layout reasons, the error container can not be inside the same
    # .form-group as the form control (this often happens when constructing horizontal forms).
    #
    # The rules for locating the help block:
    #
    # * Search for element with attribute `data-error-block-for` set to the field's `id` attribute
    # * If not found, find the enclosing .form-group element
    # * Search the form group for an element with attribute `data-presentation="error"`
    # * Otherwise, it is not found (but may be created dynamically)
    # * If found, set the `data-error-block-for` attribute to the field's `id` (assigning a unique id to the field
    #   if not already present)
    #
    # The rules for creating the help block:
    #
    # * The element is created as `p.help-block` with `data-error-block-for` attribute set to the
    #   field's id.  The field will be assigned an id if necesary.
    # * Normally, the block is inserted immediately after the field
    # * If the field's immediate container has class "input-group", then the block is inserted after the container
    showValidationError: "t5:field:show-validation-error"

  # Events triggered by the Palette component.
  palette:
    # Event triggered when the selection is about to change.
    #
    # * memo.selectedOptions - array of selected options (e.g., HTMLOptionElement) representing which options
    #   will be selected in the Palette, should the change be allowed.
    # * memo.reorder - if true, then the event represents changing the order of the selections only
    # * memo.cancel - function to invoke to prevent the change to the Palette from occurring
    # * memo.defer - like cancel, but returns a no-arguments function that will perform the update at a later date (e.g.,
    #   after a confirmation panel)
    willChange: "t5:palette:willChange"
    # Event triggered after the Palette selection has changed.
    #
    # * memo.selectedOptions - array of selected options (e.g., HTMLOptionElement)
    # * memo.reorder - if true, the event represents a change in the order of selections only
    didChange: "t5:palette:didChange"

  # Defines a number of event names specific to Tapestry Zones. Zones are Tapestry components that are structured
  # to correctly support dynamic updates from the server via an Ajax request, and a standard response
  # (the partial page render reponse). More details are available in the `t5/core/zone` module.
  zone:
    # Invoked on a zone element to force an update to its content. The event memo should contain a `content` key (an
    # Element, or a `t5/core/dom:ElementWrapper`, or more typically, a string containing HTML markup). A standard top-level
    # handler is defined by module `t5/core/zone`, and is responsible for the actual update; it triggers the
    # `events.zone.willUpdate` and `events.zone.didUpdate` events just before and just after changing the element's
    # content.
    update: "t5:zone:update"

    # Triggered (by the standard `events.zone.update` event handler) just before the content in a Zone will be updated.
    willUpdate: "t5:zone:will-update"

    # Triggered (by the standard `events.zone.update` event handle) just after the content in a Zone has updated.
    # If the zone was not visible, it is made visible after its content is changed, and before this event is triggered.
    # Some number of other components that also perform Ajax updates of the page also trigger this event.
    #
    # Certain components bind this event to scan new additions to the page to see if certain structures exist and
    # create client-side support in the form of controllers and event handlers. DateField is one such example
    # (see `t5/core/datefield` module).
    didUpdate: "t5:zone:did-update"

    # Triggered on (or within) a zone element, the default handler will peform an Ajax request and, when the response is available,
    # update the zone (via `events.zone.update`). The request should provide a partial page render response. If the
    # response includes a `content` key, its value will be the markup to replace the zone element's body.
    #
    # * memo.url - URL to use for the Ajax request
    # * memo.parameters - (optional) additional query parameters for the request
    refresh: "t5:zone:refresh"

  # Event names for arbitrary elements. These notifications exist primarily to allow for customizations in how
  # certain behaviors are presented, for example, to add animation when certain elements are hidden or revealed.
  element:
    # Triggered when a hidden element has just been displayed.
    didShow: "t5:element:did-show"
    # Trigered when a visible element has just been hidden.
    didHide: "t5:element:did-hide"
  # Event names specific to client-side element associated with the FormFragment component. These events exist to allow
  # client code to cleanly adjust the visibility of the fragment, or remove it.
  formfragment:
    # Requests that the fragment change its visibility. The event memo is an object with a single key, visible, a
    # boolean. The fragment will show or hide itself if necessary (triggering the `element.didShow` or
    # `element.didHide` event).
    changeVisibility: "t5:fragment:change-visibility"
  # Event to scan inserted DOM content for components to initialize (see t5/core/dom:scanner)
  initializeComponents : "t5:initializeComponents"
