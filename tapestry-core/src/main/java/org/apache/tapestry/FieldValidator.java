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

package org.apache.tapestry;

/**
 * Responsible for validation of a single field.
 * 
 * @param <T>
 * @see Validator
 */
public interface FieldValidator<T>
{
    /**
     * Invoked after the client-submitted value has been {@link Translator translated} to check that
     * the value conforms to expectations (often, in terms of minimum or maximum value). If and only
     * if the value is approved by all Validators is the value applied by the field.
     * 
     * @param value
     *            the translated value supplied by the user
     * @throws ValidationException
     *             if the value violates the constraint
     */
    void validate(T value) throws ValidationException;
}
