// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.dom.*;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

import java.io.PrintWriter;

public class MarkupWriterImpl implements MarkupWriter
{
    private final Document _document;

    private Element _current;

    private Text _currentText;

    public MarkupWriterImpl()
    {
        this(new DefaultMarkupModel());
    }

    public MarkupWriterImpl(MarkupModel model)
    {
        _document = new Document(model);
    }

    public void toMarkup(PrintWriter writer)
    {
        _document.toMarkup(writer);
    }

    @Override
    public String toString()
    {
        return _document.toString();
    }

    public Document getDocument()
    {
        return _document;
    }

    public Element getElement()
    {
        return _current;
    }

    public void write(String text)
    {
        // Whitespace before and after the root element is quietly ignored.
        if (_current == null && InternalUtils.isBlank(text)) return;

        ensureCurrentElement();

        if (text == null) return;

        if (_currentText == null)
        {
            _currentText = _current.text(text);
            return;
        }

        _currentText.write(text);
    }

    public void writef(String format, Object... args)
    {
        // A bit of a cheat:

        write("");
        _currentText.writef(format, args);
    }

    public void attributes(Object... namesAndValues)
    {
        ensureCurrentElement();

        int i = 0;

        while (i < namesAndValues.length)
        {
            // name should never be null.

            String name = namesAndValues[i++].toString();
            Object value = namesAndValues[i++];

            if (value == null) continue;

            _current.attribute(name, value.toString());
        }

    }

    private void ensureCurrentElement()
    {
        if (_current == null) throw new IllegalStateException(ServicesMessages.markupWriterNoCurrentElement());
    }

    public Element element(String name, Object... namesAndValues)
    {
        if (_current == null) _current = _document.newRootElement(name);
        else _current = _current.element(name);

        attributes(namesAndValues);

        _currentText = null;

        return _current;
    }

    public void writeRaw(String text)
    {
        ensureCurrentElement();

        _currentText = null;

        _current.raw(text);
    }

    public Element end()
    {
        ensureCurrentElement();

        _current = _current.getParent();

        _currentText = null;

        return _current;
    }

    public void comment(String text)
    {
        ensureCurrentElement();

        _current.comment(text);

        _currentText = null;
    }

}
