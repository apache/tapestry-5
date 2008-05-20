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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.*;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.services.FormSupport;

public class FieldValidatorImpl implements FieldValidator
{
    private final Field field;

    private final Object constraintValue;

    private final MessageFormatter messageFormatter;

    private final Validator validator;

    private final FormSupport formSupport;

    public FieldValidatorImpl(Field field, Object constraintValue, MessageFormatter messageFormatter,
                              Validator validator, FormSupport formSupport)
    {
        this.field = field;
        this.constraintValue = constraintValue;
        this.messageFormatter = messageFormatter;
        this.validator = validator;
        this.formSupport = formSupport;
    }

    @SuppressWarnings("unchecked")
    public void validate(Object value) throws ValidationException
    {
        if (!validator.isRequired() && isBlank(value)) return;

        if (value != null && !validator.getValueType().isInstance(value)) return;

        validator.validate(field, constraintValue, messageFormatter, value);
    }

    @SuppressWarnings("unchecked")
    public void render(MarkupWriter writer)
    {
        validator.render(field, constraintValue, messageFormatter, writer, formSupport);
    }

    public boolean isRequired()
    {
        return validator.isRequired();
    }

    private boolean isBlank(Object value)
    {
        return value == null || value.equals("");
    }

}
