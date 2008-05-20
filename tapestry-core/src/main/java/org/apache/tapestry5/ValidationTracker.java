// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.corelib.components.Loop;

import java.util.List;

/**
 * Tracks information related to user input validations. This information is: <ul> <li>The input values provided by the
 * user. <li>Any validation exceptions associated with input fields. </ul>
 * <p/>
 * The tracker must differentiate between components (which will implement the {@link Field} interfaces) and fields. It
 * is a one to many relationship, because each field may be called upon to render itself multiple times within a
 * request, because of {@link Loop} or other similar components.
 * <p/>
 * Internally, the tracker indexes its information in terms of the {@linkplain Field#getControlName() control name} for
 * each rendering of the component (the mechanics of Tapestry ensures that this is unique within the form).
 * <p/>
 * Validation trackers must be serializable, as they will almost always be stored into the HttpSession.
 * <p/>
 * Trackers are used by only a single form within a single page; they are not threadsafe.
 */
public interface ValidationTracker
{
    /**
     * Called by a field to record the exact input from the user, prior to any validation. If the form is redisplayed
     * (to present errors), the input value will be sent back to the user for correction.
     *
     * @param field the field recording the input
     * @param input the value obtained from the forms submission
     */
    void recordInput(Field field, String input);

    /**
     * Returns a previously recorded input value.
     */
    String getInput(Field field);

    /**
     * Records an error message for a field. The error message is primarily derived from a {@link ValidationException}
     * thrown by a {@link Validator} or {@link Translator}.
     *
     * @param field
     * @param errorMessage
     */
    void recordError(Field field, String errorMessage);

    /**
     * Records an error message that is not associated with any specific field. This often reflects some amount of
     * cross-form validation.
     *
     * @param errorMessage
     */
    void recordError(String errorMessage);

    /**
     * For a given field, determines if the field is "in error", meaning that an error message has been previously
     * recorded for the field.
     *
     * @param field
     * @return true if an error message is present
     */
    boolean inError(Field field);

    /**
     * Returns a previously recorded error message.
     */
    String getError(Field field);

    /**
     * Returns true if any field contains an error.
     */
    boolean getHasErrors();

    /**
     * Returns a list of all error messages. The messages are stored in the order that they were added to the tracker,
     * except that unassociated errors (unassociated with any field) are listed first.
     */
    List<String> getErrors();

    /**
     * Clears all information stored by the tracker.
     */
    void clear();
}
