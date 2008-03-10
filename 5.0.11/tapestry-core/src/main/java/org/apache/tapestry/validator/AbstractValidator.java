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

package org.apache.tapestry.validator;

import org.apache.tapestry.Validator;

public abstract class AbstractValidator<C, T> implements Validator<C, T>
{
    private final Class<C> _constraintType;

    private final Class<T> _valueType;

    private final String _messageKey;

    protected AbstractValidator(Class<C> constraintType, Class<T> valueType, String messageKey)
    {
        _constraintType = constraintType;
        _valueType = valueType;
        _messageKey = messageKey;
    }

    public final Class<C> getConstraintType()
    {
        return _constraintType;
    }

    public final Class<T> getValueType()
    {
        return _valueType;
    }

    public String getMessageKey()
    {
        return _messageKey;
    }

    public boolean isRequired()
    {
        return false;
    }
}
