// Copyright 2007 The Apache Software Foundation
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
     * A convienience for invoking {@link ValidationTracker#recordError(String)}.
     */
    public abstract void recordError(String errorMessage);

    /**
     * A convienience for invoking {@link ValidationTracker#recordError(Field, String)}.
     */
    public abstract void recordError(Field field, String errorMessage);

    /**
     * Returns true if the form's {@link ValidationTracker} contains any {@link ValidationTracker#getHasErrors()
     * errors}.
     */
    public abstract boolean getHasErrors();

    /**
     * Returns true if the form's {@link ValidationTracker} does not contain any {@link ValidationTracker#getHasErrors()
     * errors}.
     */
    public abstract boolean isValid();

    /**
     * Invokes {@link ValidationTracker#clear()}.
     */
    public abstract void clearErrors();

}
