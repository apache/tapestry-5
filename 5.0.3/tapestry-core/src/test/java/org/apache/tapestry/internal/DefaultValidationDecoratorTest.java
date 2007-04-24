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

package org.apache.tapestry.internal;

import org.apache.tapestry.Field;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.dom.XMLMarkupModel;
import org.apache.tapestry.internal.services.MarkupWriterImpl;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class DefaultValidationDecoratorTest extends TapestryTestCase
{
    @Test
    public void label_has_no_field()
    {
        Environment env = newEnvironment();

        replay();

        ValidationDecorator decorator = new DefaultValidationDecorator(env, null, null);

        decorator.insideLabel(null, null);

        verify();
    }

    @Test
    public void label_error_no_existing_class_attribute()
    {
        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);
        Environment env = newEnvironment();
        Field field = newField();
        ValidationTracker tracker = newValidationTracker();

        train_peekRequired(env, ValidationTracker.class, tracker);
        train_inError(tracker, field, true);

        replay();

        Element e = writer.element("label", "accesskey", "f");

        ValidationDecorator decorator = new DefaultValidationDecorator(env, null, null);

        decorator.insideLabel(field, e);

        assertEquals(writer.toString(), "<label accesskey=\"f\" class=\"t-error\"/>");

        verify();
    }

    @Test
    public void label_error_with_existing_class_attribute()
    {
        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);
        Environment env = newEnvironment();
        Field field = newField();
        ValidationTracker tracker = newValidationTracker();

        train_peekRequired(env, ValidationTracker.class, tracker);
        train_inError(tracker, field, true);

        replay();

        Element e = writer.element("label", "accesskey", "f", "class", "foo");

        ValidationDecorator decorator = new DefaultValidationDecorator(env, null, null);

        decorator.insideLabel(field, e);

        assertEquals(writer.toString(), "<label accesskey=\"f\" class=\"foo t-error\"/>");

        verify();
    }

    @Test
    public void field_error()
    {
        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel(), null);
        Environment env = newEnvironment();
        Field field = newField();
        ValidationTracker tracker = newValidationTracker();

        train_peekRequired(env, ValidationTracker.class, tracker);
        train_inError(tracker, field, true);
        train_peekRequired(env, MarkupWriter.class, writer);

        replay();

        writer.element(
                "input",
                "type",
                "text",
                "name",
                "ex",
                "class",
                "foo",
                "value",
                "freddy",
                "size",
                "30");

        ValidationDecorator decorator = new DefaultValidationDecorator(env, null, null);

        decorator.insideField(field);

        assertEquals(
                writer.toString(),
                "<input class=\"foo t-error\" name=\"ex\" size=\"30\" type=\"text\" value=\"freddy\"/>");

        verify();
    }

    @Test
    public void field_ok()
    {
        Environment env = newEnvironment();
        Field field = newField();
        ValidationTracker tracker = newValidationTracker();

        train_peekRequired(env, ValidationTracker.class, tracker);
        train_inError(tracker, field, false);

        replay();

        ValidationDecorator decorator = new DefaultValidationDecorator(env, null, null);

        decorator.insideField(field);

        verify();
    }

    @Test
    public void label_when_field_not_in_error()
    {
        Environment env = newEnvironment();
        Field field = newField();
        ValidationTracker tracker = newValidationTracker();

        train_peekRequired(env, ValidationTracker.class, tracker);
        train_inError(tracker, field, false);

        replay();

        ValidationDecorator decorator = new DefaultValidationDecorator(env, null, null);

        decorator.insideLabel(field, null);

        verify();
    }
}
