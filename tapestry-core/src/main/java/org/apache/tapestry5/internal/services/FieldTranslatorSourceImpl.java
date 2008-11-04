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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.FieldTranslatorSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.TranslatorSource;
import org.apache.tapestry5.services.ValidationMessagesSource;

import java.util.Locale;

public class FieldTranslatorSourceImpl implements FieldTranslatorSource
{
    private final TranslatorSource translatorSource;

    private final ValidationMessagesSource validationMessagesSource;

    private final FormSupport formSupport;

    public FieldTranslatorSourceImpl(TranslatorSource translatorSource,
                                     ValidationMessagesSource validationMessagesSource, FormSupport formSupport)
    {
        this.translatorSource = translatorSource;
        this.validationMessagesSource = validationMessagesSource;
        this.formSupport = formSupport;
    }

    public FieldTranslator createDefaultTranslator(ComponentResources resources, String parameterName)
    {
        Defense.notNull(resources, "resources");
        Defense.notBlank(parameterName, "parameterName");

        Field field = (Field) resources.getComponent();
        Class propertyType = resources.getBoundType(parameterName);

        return createDefaultTranslator(field, resources.getId(), resources.getContainerMessages(),
                                       resources.getLocale(), propertyType,
                                       resources.getAnnotationProvider(parameterName));
    }

    public FieldTranslator createDefaultTranslator(Field field, String overrideId, Messages overrideMessages,
                                                   Locale locale, Class propertyType,
                                                   AnnotationProvider propertyAnnotations)
    {
        Defense.notNull(field, "field");
        Defense.notBlank(overrideId, "overrideId");
        Defense.notNull(overrideMessages, "overrideMessages");
        Defense.notNull(locale, "locale");


        if (propertyType == null) return null;

        Translator translator = translatorSource.findByType(propertyType);

        if (translator == null) return null;

        return createTranslator(field, overrideId, overrideMessages, locale, translator);
    }

    public FieldTranslator createTranslator(Field field, String overrideId, Messages overrideMessages, Locale locale,
                                            Translator translator)
    {
        MessageFormatter formatter = findFormatter(overrideId, overrideMessages, locale, translator);

        return new FieldTranslatorImpl(field, translator, formatter, formSupport);
    }

    public FieldTranslator createTranslator(ComponentResources resources, String translatorName)
    {
        Defense.notNull(resources, "resources");
        Defense.notBlank(translatorName, "translatorName");

        Field field = (Field) resources.getComponent();

        Translator translator = translatorSource.get(translatorName);

        return createTranslator(field, resources.getId(), resources.getContainerMessages(), resources.getLocale(),
                                translator);
    }


    private MessageFormatter findFormatter(String overrideId, Messages overrideMessages, Locale locale,
                                           Translator translator)
    {
        // TAP5-228: Try to distinguish message overrides by form id and overrideId (i.e., property name) first.

        String translatorName = translator.getName();

        String overrideKey = formSupport.getFormValidationId() + "-" + overrideId + "-" + translatorName + "-message";

        if (overrideMessages.contains(overrideKey))
            return overrideMessages.getFormatter(overrideKey);

        // Ok, look for a simpler name that omits the formId prefix.

        overrideKey = overrideId + "-" + translatorName + "-message";

        if (overrideMessages.contains(overrideKey))
            return overrideMessages.getFormatter(overrideKey);

        // Otherwise, use the built-in validation message appropriate to this validator.

        Messages validationMessages = validationMessagesSource.getValidationMessages(locale);

        return validationMessages.getFormatter(translator.getMessageKey());
    }
}
