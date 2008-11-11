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

package org.apache.tapestry5.dom;

import org.apache.tapestry5.ioc.internal.util.Defense;

import java.io.PrintWriter;

/**
 * The root node of a DOM.
 */
public final class Document extends Node
{
    private Element rootElement;

    private DTD dtd;

    private final MarkupModel model;

    private final String encoding;

    public Document(MarkupModel model)
    {
        this(model, null);
    }

    public Document(MarkupModel model, String encoding)
    {
        super(null);

        Defense.notNull(model, "model");

        this.model = model;
        this.encoding = encoding;
    }

    @Override
    public Document getDocument()
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

        if (rootElement == null) return null;

        int slashx = path.indexOf("/");

        String rootElementName = slashx < 0 ? path : path.substring(0, slashx);

        if (!rootElement.getName().equals(rootElementName)) return null;

        return slashx < 0 ? rootElement : rootElement.find(path.substring(slashx + 1));
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
        return model;
    }

    /**
     * Creates the root element for this document, replacing any previous root element.
     */
    public Element newRootElement(String name)
    {
        rootElement = new Element(this, null, name);

        return rootElement;
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
        rootElement = new Element(this, namespace, name);

        return rootElement;
    }

    @Override
    public void toMarkup(Document document, PrintWriter writer)
    {
        if (rootElement == null) throw new IllegalStateException(DomMessages.noRootElement());


        if (model.isXML())
        {
            writer.print("<?xml version=\"1.0\"");

            if (encoding != null) writer.printf(" encoding=\"%s\"", encoding);

            writer.print("?>\n");
        }

        // TODO: lead-in comments, directives.
        if (dtd != null)
        {
            dtd.toMarkup(writer);
        }

        rootElement.toMarkup(document, writer);
    }

    @Override
    public String toString()
    {
        if (rootElement == null) return "[empty Document]";

        return super.toString();
    }

    public Element getRootElement()
    {
        return rootElement;
    }

    /**
     * Tries to find an element in this document whose id is specified.
     *
     * @param id the value of the id attribute of the element being looked for
     * @return the element if found. null if not found.
     */
    public Element getElementById(String id)
    {
        return rootElement.getElementById(id);
    }

    public void dtd(String name, String publicId, String systemId)
    {
        dtd = new DTD(name, publicId, systemId);
    }
}
