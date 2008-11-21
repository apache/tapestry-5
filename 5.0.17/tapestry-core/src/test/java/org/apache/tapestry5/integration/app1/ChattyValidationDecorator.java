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

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.dom.Element;

public class ChattyValidationDecorator implements ValidationDecorator
{
    private final MarkupWriter writer;

    private final ValidationDecorator delegate;

    public ChattyValidationDecorator(MarkupWriter writer, ValidationDecorator delegate)
    {
        this.writer = writer;
        this.delegate = delegate;
    }

    public void beforeLabel(Field field)
    {
        writer.writef("[Before label for %s]", field.getLabel());
    }

    public void afterLabel(Field field)
    {
        writer.writef("[After label for %s]", field.getLabel());
    }

    public void beforeField(Field field)
    {
        writer.writef("[Before field %s]", field.getLabel());

        delegate.beforeField(field);
    }

    public void insideField(Field field)
    {
        delegate.insideField(field);
    }

    public void afterField(Field field)
    {
        delegate.afterField(field);

        writer.writef("[After field %s (%s)]", field.getLabel(),
                      field.isRequired() ? "required" : "optional"
        );
    }

    public void insideLabel(Field field, Element labelElement)
    {
        delegate.insideLabel(field, labelElement);
    }
}
