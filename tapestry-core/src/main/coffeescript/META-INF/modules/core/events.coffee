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

# ## core/events
#
# This module defines logical names for all events that Tapestry-controlled elements
# trigger or listener for. Prototype requires that all custom events have a namespace prefix; jQuery appears to
# allow it without issue.
define
  # Defines events related to the validation and submission of forms. See module `core/forms` for further details.
  # All events are triggered on a specific HTML `<form>` element, and top-level handlers take it from there.
  form:

    # Triggered after `events.field.validate`, when there are no field validation exceptions, to allow for cross-form
    # validation.
    validateForm: "t5:form:validate"

    # Triggered after `validateForm` (when there are no prior validation exceptions), to allow certain elements
    # to configure themselves immediately before the form is submitted. This exists primarily for components such
    # as FormFragment, which will update a enable or disable a hidden field to match the visibility of the fragment.
    prepareForSubmit: "t5:form:prepare-for-submit"

    # Triggered last, when the form is configured to not submit normally (as a standard POST). Under 5.3, this
    # configuration was achieved by adding the `t-prevent-submission` CSS class; under 5.4 it is preferred to
    # set the `data-prevent-submission` attribute. In either case, the submit event is stopped, and this
    # event fired to replace it; in most cases, a handler will then handle submitting the form's data as part
    # of an Ajax request.
    processSubmit: "t5:form:process-submit"

  field:
    # Triggered by the Form on all enclosed elements with the `data-validation` attribute (indicating they are
    # interested in participating with user input validation). The memo object passed to the event has an error property
    # that can be set to true to indicate a validation error. Individual fields should determine if the field is in
    # error and remove or add/update decorations for the validation error (decorations will transition from 5.3 style
    # popups to Twitter Bootstrap in the near future).
    validate: "t5:field:validate"

  # Defines a number of event names specific to Tapestry Zones. Zones are Tapestry components that are structured
  # to correctly support dynamic updates from the server via an Ajax request, and a standard response
  # (the partial page render reponse). More details are available in the `core/zone` module.
  zone:
    # Invoked on a zone element to force an update to its content. The event memo should contain a `content` key (an
    # Element, or a `core/spi:ElementWrapper`, or more typically, a string containing HTML markup). A standard top-level
    # handler is defined by module `core/zone`, and is responsible for the actual update; it triggers the
    # `events.zone.willUpdate` and `events.zone.didUpdate` events just before and just after changing the element's
    # content.
    update: "t5:zone:update"

    # Triggered (by the standard handler) just before the content in a Zone will be updated.
    willUpdate: "t5:zone:will-update"

    # Triggered (by the standard hanndler) just after the content in a Zone has updated. If the zone was not visible, it
    # is made visible after its content is changed, and before this event is triggered.
    didUpdate: "t5:zone:did-update"