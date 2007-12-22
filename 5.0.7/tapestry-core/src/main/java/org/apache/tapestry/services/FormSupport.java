// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import org.apache.tapestry.ClientElement;
import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.Field;

/**
 * Services provided by an enclosing Form control component to the various form element components
 * it encloses. Implement {@link ClientElement}, to share the id of the enclosing form.
 */
public interface FormSupport extends ClientElement
{
    /**
     * Allocates a unique (within the form) element name for some component enclosed component,
     * based on the component's id.
     *
     * @param id the component's id
     * @return a unique string, usually the component's id, but sometime extended with a unique
     *         number or string
     */
    String allocateElementName(String id);

    /**
     * Stores an action for execution during a later request.
     */
    <T> void store(T component, ComponentAction<T> action);

    /**
     * As with {@link #store(Object, org.apache.tapestry.ComponentAction)}}, but the
     * action is also invoked immediately. This is useful for defining an action that
     * should occur symmetrically in both the render request and the form submission's
     * action request.
     *
     * @param component component against which to trigger the action
     * @param action    the action that will be triggered (and passed the component)
     */
    <T> void storeAndExecute(T component, ComponentAction<T> action);

    /**
     * Defers a command until the end of the form submission. The command will be executed after the
     * Form's validate notification, but before the Form's submit, success or failure notifications.
     * During a form render, runnables are executed after the body of the form has rendered.
     *
     * @param command
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
     * Collects field validation information.
     *
     * @param field          for which validation is being generated
     * @param validationName name of validation method (see Tapestry.Validation in tapestry.js)
     * @param message        the error message to display if the field is invalid
     * @param constraint     additional constraint value, or null for validations that don't require a constraint
     */
    void addValidation(Field field, String validationName, String message, Object constraint);

}
