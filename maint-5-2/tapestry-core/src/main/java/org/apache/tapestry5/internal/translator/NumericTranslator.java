// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.translator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.services.FormSupport;

import java.text.ParseException;

/**
 * Uses a {@link org.apache.tapestry5.internal.translator.NumericTranslatorSupport} to provide proper locale-aware
 * support for all the built-in numeric types.
 *
 * @since 5.1.0.1
 */
public class NumericTranslator<T extends Number> extends AbstractTranslator<T>
{
    private final NumericTranslatorSupport support;

    public NumericTranslator(String name, Class<T> type, NumericTranslatorSupport support)
    {
        super(name, type, support.getMessageKey(type));

        this.support = support;
    }

    public void render(Field field, String message, MarkupWriter writer, FormSupport formSupport)
    {
        if (formSupport.isClientValidationEnabled())
            support.addValidation(getType(), field, message);
    }

    public T parseClient(Field field, String clientValue, String message) throws ValidationException
    {
        try
        {
            return support.parseClient(getType(), clientValue);
        }
        catch (ParseException ex)
        {
            throw new ValidationException(message);
        }
    }

    public String toClient(T value)
    {
        return support.toClient(getType(), value);
    }
}
