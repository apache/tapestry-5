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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.Validator;
import org.apache.tapestry.ioc.MessageFormatter;

public class FieldValidatorImpl implements FieldValidator
{
    private final Field _field;

    private final Object _constraintValue;

    private final MessageFormatter _messageFormatter;

    private final Validator _validator;

    public FieldValidatorImpl(Field field, Object constraintValue,
            MessageFormatter messageFormatter, Validator validator)
    {
        _field = field;
        _constraintValue = constraintValue;
        _messageFormatter = messageFormatter;
        _validator = validator;
    }

    @SuppressWarnings("unchecked")
    public void validate(Object value) throws ValidationException
    {
        if (! _validator.invokeIfBlank() && isBlank(value))
            return;

        if (value != null && !_validator.getValueType().isInstance(value))
            return;

        _validator.validate(_field, _constraintValue, _messageFormatter, value);
    }

    private boolean isBlank(Object value)
    {
        return value == null || value.equals("");
    }

}
