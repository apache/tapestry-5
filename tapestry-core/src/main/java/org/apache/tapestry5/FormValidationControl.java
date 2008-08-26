// Copyright 2007, 2008 The Apache Software Foundation
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
 * Allows control over validation concerns of a Form component.
 */
public interface FormValidationControl
{
    /**
     * A convienience for invoking {@link org.apache.tapestry5.ValidationTracker#recordError(String)}.
     */
    void recordError(String errorMessage);

    /**
     * A convienience for invoking {@link org.apache.tapestry5.ValidationTracker#recordError(Field, String)}.
     */
    void recordError(Field field, String errorMessage);

    /**
     * Returns true if the form's {@link ValidationTracker} contains any {@link org.apache.tapestry5.ValidationTracker#getHasErrors()
     * errors}.
     */
    boolean getHasErrors();

    /**
     * Returns true if the form's {@link org.apache.tapestry5.ValidationTracker} does not contain any {@link
     * org.apache.tapestry5.ValidationTracker#getHasErrors() errors}.
     */
    boolean isValid();

    /**
     * Invokes {@link org.apache.tapestry5.ValidationTracker#clear()}.
     */
    void clearErrors();
}
