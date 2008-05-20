// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.MarkupWriterListener;
import org.apache.tapestry5.dom.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.io.PrintWriter;
import java.util.List;

public class MarkupWriterImpl implements MarkupWriter
{
    private final Document document;

    private Element current;

    private Text currentText;

    private final List<MarkupWriterListener> listeners = CollectionFactory.newList();

    public MarkupWriterImpl()
    {
        this(new DefaultMarkupModel());
    }

    public MarkupWriterImpl(MarkupModel model)
    {
        this(model, null);
    }

    public MarkupWriterImpl(MarkupModel model, String encoding)
    {
        document = new Document(model, encoding);
    }

    public void toMarkup(PrintWriter writer)
    {
        document.toMarkup(writer);
    }

    @Override
    public String toString()
    {
        return document.toString();
    }

    public Document getDocument()
    {
        return document;
    }

    public Element getElement()
    {
        return current;
    }

    public void cdata(String content)
    {
        ensureCurrentElement();

        current.cdata(content);

        currentText = null;
    }

    public void write(String text)
    {
        // Whitespace before and after the root element is quietly ignored.
        if (current == null && InternalUtils.isBlank(text)) return;

        ensureCurrentElement();

        if (text == null) return;

        if (currentText == null)
        {
            currentText = current.text(text);
            return;
        }

        currentText.write(text);
    }

    public void writef(String format, Object... args)
    {
        // A bit of a cheat:

        write("");
        currentText.writef(format, args);
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

            current.attribute(name, value.toString());
        }

    }

    private void ensureCurrentElement()
    {
        if (current == null) throw new IllegalStateException(ServicesMessages.markupWriterNoCurrentElement());
    }

    public Element element(String name, Object... namesAndValues)
    {
        if (current == null) current = document.newRootElement(name);
        else current = current.element(name);

        attributes(namesAndValues);

        currentText = null;

        fireElementDidStart();

        return current;
    }

    public void writeRaw(String text)
    {
        ensureCurrentElement();

        currentText = null;

        current.raw(text);
    }

    public Element end()
    {
        ensureCurrentElement();

        fireElementDidEnd();

        current = current.getParent();

        currentText = null;

        return current;
    }

    public void comment(String text)
    {
        ensureCurrentElement();

        current.comment(text);

        currentText = null;
    }

    public Element attributeNS(String namespace, String attributeName, String attributeValue)
    {
        ensureCurrentElement();

        current.attribute(namespace, attributeName, attributeValue);

        return current;
    }

    public Element defineNamespace(String namespace, String namespacePrefix)
    {
        ensureCurrentElement();

        current.defineNamespace(namespace, namespacePrefix);

        return current;
    }

    public Element elementNS(String namespace, String elementName)
    {
        if (current == null) current = document.newRootElement(namespace, elementName);
        else current = current.elementNS(namespace, elementName);

        currentText = null;


        fireElementDidStart();

        return current;
    }

    public void addListener(MarkupWriterListener listener)
    {
        Defense.notNull(listener, "listener");

        listeners.add(listener);
    }

    public void removeListener(MarkupWriterListener listener)
    {
        listeners.remove(listener);
    }

    private void fireElementDidStart()
    {
        for (MarkupWriterListener l : listeners)
        {
            l.elementDidStart(current);
        }
    }

    private void fireElementDidEnd()
    {
        for (MarkupWriterListener l : listeners)
        {
            l.elementDidEnd(current);
        }
    }
}

