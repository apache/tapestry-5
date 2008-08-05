// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.validator;

import org.apache.tapestry5.Validator;

/**
 * Base class for constructing a {@link org.apache.tapestry5.Validator}.
 */
public abstract class AbstractValidator<C, T> implements Validator<C, T>
{
    private final Class<C> constraintType;

    private final Class<T> valueType;

    private final String messageKey;

    protected AbstractValidator(Class<C> constraintType, Class<T> valueType, String messageKey)
    {
        this.constraintType = constraintType;
        this.valueType = valueType;
        this.messageKey = messageKey;
    }

    public final Class<C> getConstraintType()
    {
        return constraintType;
    }

    public final Class<T> getValueType()
    {
        return valueType;
    }

    public final String getMessageKey()
    {
        return messageKey;
    }

    /**
     * Return false, which is correct for the vast majority of validators. {@link org.apache.tapestry5.validator.Required}
     * overrides this to true.F
     */
    public boolean isRequired()
    {
        return false;
    }
}
