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

package org.apache.tapestry.integration.app1;

import org.apache.tapestry.Field;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.dom.Element;

public class ChattyValidationDecorator implements ValidationDecorator
{
    private final MarkupWriter _writer;

    private final ValidationDecorator _delegate;

    public ChattyValidationDecorator(MarkupWriter writer, ValidationDecorator delegate)
    {
        _writer = writer;
        _delegate = delegate;
    }

    public void beforeLabel(Field field)
    {
        _writer.writef("[Before label for %s]", field.getLabel());
    }

    public void afterLabel(Field field)
    {
        _writer.writef("[After label for %s]", field.getLabel());
    }

    public void beforeField(Field field)
    {
        _writer.writef("[Before field %s]", field.getLabel());

        _delegate.beforeField(field);
    }

    public void insideField(Field field)
    {
        _delegate.insideField(field);
    }

    public void afterField(Field field)
    {
        _delegate.afterField(field);

        _writer.writef("[After field %s]", field.getLabel());
    }

    public void insideLabel(Field field, Element labelElement)
    {
        _delegate.insideLabel(field, labelElement);
    }
}
