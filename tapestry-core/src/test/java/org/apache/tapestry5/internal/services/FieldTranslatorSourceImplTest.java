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
import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.beaneditor.Translate;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.root.FieldComponent;
import org.apache.tapestry5.services.FieldTranslatorSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.TranslatorSource;
import org.testng.annotations.Test;

/**
 * Fills in some gaps that are not currently tested by the integration tests.
 */
@SuppressWarnings("unchecked")
public class FieldTranslatorSourceImplTest extends InternalBaseTestCase
{
    @Test
    public void create_default_property_type_null()
    {
        Field field = mockField();
        Messages messages = mockMessages();
        Locale locale = Locale.ENGLISH;

        replay();

        FieldTranslatorSource source = new FieldTranslatorSourceImpl(null, null, null);

        assertNull(source.createDefaultTranslator(field, "override", messages, locale, null, null));

        verify();
    }

    @Test
    public void create_default_translator_not_found_for_type()
    {
        Field field = mockField();
        Messages messages = mockMessages();
        Locale locale = Locale.ENGLISH;
        Class propertyType = Map.class;
        TranslatorSource ts = mockTranslatorSource();
        AnnotationProvider ap = mockAnnotationProvider(null);

        train_findByType(ts, propertyType, null);

        replay();

        FieldTranslatorSource source = new FieldTranslatorSourceImpl(ts, null, null);

        assertNull(source.createDefaultTranslator(field, "override", messages, locale, propertyType, ap));

        verify();
    }

    @Test
    public void create_default_translator_with_annotation()
    {
        TranslatorSource ts = mockTranslatorSource();
        AnnotationProvider ap = mockAnnotationProvider("fred");
        Translator t = mockTranslator();

        expect(ts.get("fred")).andReturn(t);

        replay();

        FieldTranslatorSourceImpl source = new FieldTranslatorSourceImpl(ts, null, null);

        assertSame(source.findTranslator(Map.class, ap), t);

        verify();
    }

    private AnnotationProvider mockAnnotationProvider(String translatorName)
    {
        AnnotationProvider ap = mockAnnotationProvider();

        if (translatorName == null)
        {
            train_getAnnotation(ap, Translate.class, null);
        }
        else
        {
            Translate t = newMock(Translate.class);
            expect(t.value()).andReturn(translatorName).atLeastOnce();
            train_getAnnotation(ap, Translate.class, t);
        }

        return ap;
    }

    @Test
    public void create_default_translator_with_name_and_null_key()
    {
        Field field = mockField();
        Messages messages = mockMessages();
        Locale locale = Locale.ENGLISH;
        Class propertyType = Map.class;
        TranslatorSource ts = mockTranslatorSource();
        FormSupport fs = mockFormSupport();
        Translator translator = mockTranslator("maptrans", Map.class);
        Messages globalMessages = mockMessages();
        MessageFormatter formatter = mockMessageFormatter();
        MarkupWriter writer = mockMarkupWriter();
        String label = "Field Label";
        String message = "Woops, did it again.";
        AnnotationProvider ap = mockAnnotationProvider(null);

        train_findByType(ts, propertyType, translator);

        train_getFormValidationId(fs, "myform");

        train_contains(messages, "myform-myfield-maptrans-message", false);
        train_contains(messages, "myfield-maptrans-message", false);
        train_getMessageKey(translator, null);

        train_getMessageFormatter(globalMessages, "maptrans-message", formatter);
        train_getLabel(field, label);
        train_format(formatter, message, label);

        translator.render(field, message, writer, fs);

        replay();

        FieldTranslatorSource source = new FieldTranslatorSourceImpl(ts, globalMessages, fs);

        FieldTranslator ft = source.createDefaultTranslator(field, "myfield", messages, locale, propertyType, ap);

        assertEquals(ft.getType(), Map.class);

        ft.render(writer);

        verify();
    }

    @Test
    public void create_default_translator_with_name()
    {
        Field field = mockField();
        Messages messages = mockMessages();
        Locale locale = Locale.ENGLISH;
        Class propertyType = Map.class;
        TranslatorSource ts = mockTranslatorSource();
        FormSupport fs = mockFormSupport();
        Translator translator = mockTranslator("maptrans", Map.class);
        Messages globalMessages = mockMessages();
        MessageFormatter formatter = mockMessageFormatter();
        MarkupWriter writer = mockMarkupWriter();
        String label = "Field Label";
        String message = "Woops, did it again.";
        AnnotationProvider ap = mockAnnotationProvider(null);

        train_findByType(ts, propertyType, translator);

        train_getFormValidationId(fs, "myform");

        train_contains(messages, "myform-myfield-maptrans-message", false);
        train_contains(messages, "myfield-maptrans-message", false);
        train_getMessageKey(translator, "mykey");
        train_getMessageFormatter(globalMessages, "mykey", formatter);
        train_getLabel(field, label);
        train_format(formatter, message, label);

        translator.render(field, message, writer, fs);

        replay();

        FieldTranslatorSource source = new FieldTranslatorSourceImpl(ts, globalMessages, fs);

        FieldTranslator ft = source.createDefaultTranslator(field, "myfield", messages, locale, propertyType, ap);

        assertEquals(ft.getType(), Map.class);

        ft.render(writer);

        verify();
    }

    @Test
    public void create_default_translator_with_override_message()
    {
        Field field = mockField();
        Messages messages = mockMessages();
        Locale locale = Locale.ENGLISH;
        Class propertyType = Map.class;
        TranslatorSource ts = mockTranslatorSource();
        FormSupport fs = mockFormSupport();
        Translator translator = mockTranslator("maptrans", Map.class);
        MessageFormatter formatter = mockMessageFormatter();
        MarkupWriter writer = mockMarkupWriter();
        String label = "My Label";
        String message = "Formatted Message";
        AnnotationProvider ap = mockAnnotationProvider(null);

        train_findByType(ts, propertyType, translator);

        train_getFormValidationId(fs, "myform");

        train_contains(messages, "myform-myfield-maptrans-message", false);
        train_contains(messages, "myfield-maptrans-message", true);
        train_getMessageFormatter(messages, "myfield-maptrans-message", formatter);

        train_getLabel(field, label);
        train_format(formatter, message, label);

        translator.render(field, message, writer, fs);

        replay();

        FieldTranslatorSource source = new FieldTranslatorSourceImpl(ts, null, fs);

        FieldTranslator ft = source.createDefaultTranslator(field, "myfield", messages, locale, propertyType, ap);

        assertEquals(ft.getType(), Map.class);

        ft.render(writer);

        verify();
    }

    @Test
    public void create_default_translator_with_per_form_override_message()
    {
        Field field = mockField();
        Messages messages = mockMessages();
        Locale locale = Locale.ENGLISH;
        Class propertyType = Map.class;
        TranslatorSource ts = mockTranslatorSource();
        FormSupport fs = mockFormSupport();
        Translator translator = mockTranslator("maptrans", Map.class);
        MessageFormatter formatter = mockMessageFormatter();
        MarkupWriter writer = mockMarkupWriter();
        String label = "My Label";
        String message = "Formatted Message";
        AnnotationProvider ap = mockAnnotationProvider(null);

        train_findByType(ts, propertyType, translator);

        train_getFormValidationId(fs, "myform");

        train_contains(messages, "myform-myfield-maptrans-message", true);
        train_getMessageFormatter(messages, "myform-myfield-maptrans-message", formatter);

        train_getLabel(field, label);
        train_format(formatter, message, label);

        translator.render(field, message, writer, fs);

        replay();

        FieldTranslatorSource source = new FieldTranslatorSourceImpl(ts, null, fs);

        FieldTranslator ft = source.createDefaultTranslator(field, "myfield", messages, locale, propertyType, ap);

        assertEquals(ft.getType(), Map.class);

        ft.render(writer);

        verify();
    }

    @Test
    public void create_translator_from_translator_name()
    {
        ComponentResources resources = mockComponentResources();
        FieldComponent field = mockFieldComponent();
        Messages messages = mockMessages();
        TranslatorSource ts = mockTranslatorSource();
        FormSupport fs = mockFormSupport();
        Translator translator = mockTranslator("map", Map.class);
        Messages globalMessages = mockMessages();
        MessageFormatter formatter = mockMessageFormatter();
        MarkupWriter writer = mockMarkupWriter();
        String label = "My Label";
        String message = "Formatted Message";

        train_getComponent(resources, field);
        train_getId(resources, "myfield");
        train_getContainerMessages(resources, messages);

        train_get(ts, "map", translator);

        train_getFormValidationId(fs, "myform");

        train_contains(messages, "myform-myfield-map-message", false);
        train_contains(messages, "myfield-map-message", false);
        train_getMessageKey(translator, "mykey");
        train_getMessageFormatter(globalMessages, "mykey", formatter);

        train_getLabel(field, label);
        train_format(formatter, message, label);

        translator.render(field, message, writer, fs);

        replay();

        FieldTranslatorSource source = new FieldTranslatorSourceImpl(ts, globalMessages, fs);

        FieldTranslator ft = source.createTranslator(resources, "map");

        assertEquals(ft.getType(), Map.class);

        ft.render(writer);

        verify();
    }
}
