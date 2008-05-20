// Copyright 2006, 2007, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5;

/**
 * Defines a field within a form.  Fields have a <a href="http://www.w3.org/TR/html4/interact/forms.html#control-name">control
 * name</a> that is used when rendering and, later, when the form is submitted, to identify the query parameter.
 * <p/>
 * Timing is important, as components may render multiple times, due to looping and other factors. Generally, a
 * component's {@link #getControlName()} will only be accurate after it has rendered.  In some cases, when generating
 * JavaScript for example, it is necessary to {@linkplain org.apache.tapestry5.services.Heartbeat#defer(Runnable) wait
 * until the end of the current Heartbeat} to ensure that all components have had thier chance to render.
 */
public interface Field extends ClientElement
{
    /**
     * Returns the value used as the name attribute of the rendered element. This value will be unique within an
     * enclosing form, even if the same component renders multiple times.
     *
     * @see org.apache.tapestry5.services.FormSupport#allocateControlName(String)
     */
    String getControlName();

    /**
     * Returns a user presentable (localized) label for the field, which may be used inside &lt;label&gt; elements on
     * the client, and inside client or server-side validation error messages.
     *
     * @return the label
     * @see org.apache.tapestry5.corelib.components.Label
     */
    String getLabel();

    /**
     * Returns true if the field is disabled; A disabled field will render a disabled attribute so that it is
     * non-responsive on the client (at least, until its disabled status is changed on the client using JavaScript). A
     * disabled field will ignore any value passed up in a form submit request. Care must be taken if the disabled
     * status of a field can change between the time the field is rendered and the time the enclosing form is
     * submitted.
     */
    boolean isDisabled();

    /**
     * Returns true if this field required (as per {@link org.apache.tapestry5.FieldValidator#isRequired()}).
     *
     * @return true if a non-blank value is required for the field
     */
    boolean isRequired();
}
