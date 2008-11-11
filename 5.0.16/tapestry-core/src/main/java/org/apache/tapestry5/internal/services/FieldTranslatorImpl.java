//  Copyright 2008 The Apache Software Foundation
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

public class FieldTranslatorImpl<T> implements FieldTranslator<T>
{
    private final Field field;
    private final Translator<T> translator;
    private final MessageFormatter formatter;
    private final FormSupport formSupport;

    public FieldTranslatorImpl(Field field, Translator<T> translator, MessageFormatter formatter,
                               FormSupport formSupport)
    {
        this.field = field;
        this.translator = translator;
        this.formatter = formatter;
        this.formSupport = formSupport;
    }

    public T parse(String input) throws ValidationException
    {
        return translator.parseClient(field, input, formatMessage());
    }

    private String formatMessage()
    {
        return formatter.format(field.getLabel());
    }

    public void render(MarkupWriter writer)
    {
        translator.render(field, formatMessage(), writer, formSupport);
    }

    public String toClient(T value)
    {
        return translator.toClient(value);
    }

    public Class<T> getType()
    {
        return translator.getType();
    }
}
