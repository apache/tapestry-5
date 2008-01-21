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

package org.apache.tapestry.dom;

import org.apache.tapestry.ioc.internal.util.Defense;

import java.io.PrintWriter;

/**
 * The root node of a DOM.
 */
public final class Document extends Node
{
    private Element _rootElement;

    private DTD _dtd;

    private final MarkupModel _model;

    private final String _encoding;

    public Document(MarkupModel model)
    {
        this(model, null);
    }

    public Document(MarkupModel model, String encoding)
    {
        super(null);

        _model = model;
        _encoding = encoding;
    }

    Document getDocument()
    {
        return this;
    }

    /**
     * Finds an element based on a path of element names.
     *
     * @param path slash separated series of element names
     * @return the matching element, or null if not found
     * @see Element#find(String)
     */
    public Element find(String path)
    {
        Defense.notBlank(path, "path");

        if (_rootElement == null) return null;

        int slashx = path.indexOf("/");

        String rootElementName = slashx < 0 ? path : path.substring(0, slashx);

        if (!_rootElement.getName().equals(rootElementName)) return null;

        return slashx < 0 ? _rootElement : _rootElement.find(path.substring(slashx + 1));
    }

    /**
     * Builds with an instance of {@link DefaultMarkupModel}.
     */
    public Document()
    {
        this(new DefaultMarkupModel());
    }

    public MarkupModel getMarkupModel()
    {
        return _model;
    }

    /**
     * Creates the root element for this document, replacing any previous root element.
     */
    public Element newRootElement(String name)
    {
        _rootElement = new Element(this, null, name);

        return _rootElement;
    }

    /**
     * Creates a new root element within a namespace.
     *
     * @param namespace URI of namespace containing the element
     * @param name      name of element with namespace
     * @return the root element
     */
    public Element newRootElement(String namespace, String name)
    {
        _rootElement = new Element(this, namespace, name);

        return _rootElement;
    }

    @Override
    public void toMarkup(PrintWriter writer)
    {
        if (_rootElement == null) throw new IllegalStateException(DomMessages.noRootElement());


        if (_model.isXML())
        {
            writer.print("<?xml version=\"1.0\"");

            if (_encoding != null) writer.printf(" encoding=\"%s\"", _encoding);

            writer.print("?>\n");
        }

        // TODO: lead-in comments, directives.
        if (_dtd != null)
        {
            _dtd.toMarkup(writer);
        }

        _rootElement.toMarkup(writer);
    }

    @Override
    public String toString()
    {
        if (_rootElement == null) return "[empty Document]";

        return super.toString();
    }

    public Element getRootElement()
    {
        return _rootElement;
    }

    /**
     * Tries to find an element in this document whose id is specified.
     *
     * @param id the value of the id attribute of the element being looked for
     * @return the element if found. null if not found.
     */
    public Element getElementById(String id)
    {
        return _rootElement.getElementById(id);
    }

    public void dtd(String name, String publicId, String systemId)
    {
        _dtd = new DTD(name, publicId, systemId);
    }
}
