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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.Field;

/**
 * Services provided by an enclosing Form control component to the various form element components it encloses.
 * Implements {@link org.apache.tapestry5.ClientElement}, to share the id of the enclosing form.
 *
 * @see org.apache.tapestry5.Field
 */
public interface FormSupport extends ClientElement
{
    /**
     * Allocates a unique (within the form) control name for some enclosed component, based on the component's id.
     *
     * @param id the component's id
     * @return a unique string, usually the component's id, but sometime extended with a unique number or string
     */
    String allocateControlName(String id);

    /**
     * Stores an action for execution during a later request.  If the action contains any mutable state, it should be in
     * its final state before invoking this method and its internal state should not be changed subsequently.
     */
    <T> void store(T component, ComponentAction<T> action);

    /**
     * As with {@link #store(Object, org.apache.tapestry5.ComponentAction)}}, but the action is also invoked
     * immediately. This is useful for defining an action that should occur symmetrically in both the render request and
     * the form submission's action request.
     *
     * @param component component against which to trigger the action
     * @param action    the action that will be triggered (and passed the component)
     */
    <T> void storeAndExecute(T component, ComponentAction<T> action);

    /**
     * Defers a command until the end of the form submission. The command will be executed <em>before</em> the Form's
     * validate notification, but after all other submit actions for the form have been processed. This is used,
     * primarily, to coordinate validations or other operations that involve multiple components, when the order of the
     * components can not be determined. During a form render, runnables are executed after the body of the form has
     * rendered.
     *
     * @param command to be executed
     */
    void defer(Runnable command);

    /**
     * Sets the encoding type for the Form. This should only be set once, and if
     *
     * @param encodingType MIME type indicating type of encoding for the form
     * @throws IllegalStateException if the encoding type has already been set to a value different than the supplied
     */
    void setEncodingType(String encodingType);

    /**
     * Collects field validation information. A Form may turn off client-side validation, in which case these calls will
     * be ignored.
     *
     * @param field          for which validation is being generated
     * @param validationName name of validation method (see Tapestry.Validation in tapestry.js)
     * @param message        the error message to display if the field is invalid
     * @param constraint     additional constraint value, or null for validations that don't require a constraint
     */
    void addValidation(Field field, String validationName, String message, Object constraint);

    /**
     * Return true if client validation is enabled for this form, false otherwise.
     */
    boolean isClientValidationEnabled();

    /**
     * Returns the complete id of the underlying Form component.  This is needed by {@link
     * org.apache.tapestry5.corelib.components.FormInjector}.
     */
    String getFormComponentId();

    /**
     * Id used as a prefix when searching {@link org.apache.tapestry5.ioc.Messages} for validation messages and
     * constraints. This is normally the simple id of the form.
     *
     * @return validation id string
     * @see org.apache.tapestry5.services.FieldTranslatorSource
     * @see org.apache.tapestry5.services.FieldValidatorSource
     */
    String getFormValidationId();
}
