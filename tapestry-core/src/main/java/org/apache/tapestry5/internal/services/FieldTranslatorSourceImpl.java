// Copyright 2008, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import java.util.Locale;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.beaneditor.Translate;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.FieldTranslatorSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.TranslatorSource;

@SuppressWarnings("all")
public class FieldTranslatorSourceImpl implements FieldTranslatorSource
{
    private final TranslatorSource translatorSource;

    private final Messages globalMessages;

    private final FormSupport formSupport;

    public FieldTranslatorSourceImpl(TranslatorSource translatorSource, Messages globalMessages,
            FormSupport formSupport)
    {
        this.translatorSource = translatorSource;
        this.globalMessages = globalMessages;
        this.formSupport = formSupport;
    }

    public FieldTranslator createDefaultTranslator(ComponentResources resources, String parameterName)
    {
        assert resources != null;
        assert InternalUtils.isNonBlank(parameterName);
        Field field = (Field) resources.getComponent();
        Class propertyType = resources.getBoundType(parameterName);

        return createDefaultTranslator(field, resources.getId(), resources.getContainerMessages(),
                null, propertyType, resources.getAnnotationProvider(parameterName));
    }

    public FieldTranslator createDefaultTranslator(Field field, String overrideId, Messages overrideMessages,
            Locale locale, Class propertyType, AnnotationProvider propertyAnnotations)
    {
        assert field != null;
        assert overrideMessages != null;
        assert InternalUtils.isNonBlank(overrideId);
        if (propertyType == null)
            return null;

        Translator translator = findTranslator(propertyType, propertyAnnotations);

        if (translator == null)
            return null;

        return createTranslator(field, overrideId, overrideMessages, locale, translator);
    }

    Translator findTranslator(Class propertyType, AnnotationProvider propertyAnnotations)
    {
        Translate annotation = propertyAnnotations.getAnnotation(Translate.class);

        if (annotation != null)
            return translatorSource.get(annotation.value());

        if (propertyType == null)
            return null;

        return translatorSource.findByType(propertyType);
    }

    public FieldTranslator createTranslator(Field field, String overrideId, Messages overrideMessages, Locale locale,
            Translator translator)
    {
        MessageFormatter formatter = findFormatter(overrideId, overrideMessages, translator);

        return new FieldTranslatorImpl(field, translator, formatter, formSupport);
    }

    public FieldTranslator createTranslator(ComponentResources resources, String translatorName)
    {
        assert resources != null;
        assert InternalUtils.isNonBlank(translatorName);
        Field field = (Field) resources.getComponent();

        Translator translator = translatorSource.get(translatorName);

        return createTranslator(field, resources.getId(), resources.getContainerMessages(), null, translator);
    }

    private MessageFormatter findFormatter(String overrideId, Messages overrideMessages, Translator translator)
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
        String messageKey = translator.getMessageKey();

        // If no key has been specified, use translator name to create a key
        if(messageKey == null)
        {
            messageKey = translatorName + "-message";
        }

        return globalMessages.getFormatter(messageKey);
    }
}
