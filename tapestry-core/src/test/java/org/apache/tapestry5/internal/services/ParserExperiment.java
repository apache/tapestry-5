// Copyright 2006 The Apache Software Foundation
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

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.net.URL;

/**
 * Used to experiment with namespace aware SAX parsers.
 */
public class ParserExperiment extends DefaultHandler
{
    private Locator locator;

    public static void main(String[] args) throws Exception
    {
        new ParserExperiment().parse("basic.tml");
    }

    public void parse(String file) throws Exception
    {
        parse(getClass().getResource(file));
    }

    public void parse(URL document) throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setNamespaceAware(true);
        // Equivalent:
        // factory.setFeature("http://xml.org/sax/features/namespaces", true);

        // Doesn't seem to do anything:
        factory.setFeature("http://apache.org/xml/features/validation/schema/normalized-value", true);

        // Doesn't seem to do anything:
        factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        // A non-validation parser is fine!

        SAXParser parser = factory.newSAXParser();

        InputSource source = new InputSource(new BufferedInputStream(document.openStream()));

        parser.parse(source, this);
    }

    private void log(String methodName, String... details)
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(String.format("%-25s:", methodName));

        if (locator != null)
        {
            buffer.append(String.format(" [Line %d, column %d]", locator.getLineNumber(), locator
                    .getColumnNumber()));
        }

        for (int i = 0; i < details.length; i++)
        {
            buffer.append("\n     ");
            buffer.append(details[i]);
        }

        System.out.println(buffer.toString());
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String string = new String(ch, start, length);
        String loggable = string.replaceAll("![\\w -]", ".").trim();

        log("characters", "start=" + start, "length=" + length, loggable);
    }

    @Override
    public void endDocument() throws SAXException
    {
        log("endDocument");
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        log("endElement", localName, "uri=" + uri, "qName=" + qName);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
        log("endPrefixMapping", prefix);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        log("ignorableWhitespace", "start=" + start, "length=" + length);
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException
    {
        log("notationDecl", name, "publicId=" + publicId, "systemId=" + systemId);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        log("pi", "target=" + target, "data=" + data);
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
        this.locator = locator;

        log("setDocumentLocator", "publicId=" + locator.getPublicId(), "systemId=" + locator.getSystemId());
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
        log("skippedEntity", name);
    }

    @Override
    public void startDocument() throws SAXException
    {
        log("startDocument");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        log("startElement", localName, "uri=" + uri, "qName=" + qName);

        int count = attributes.getLength();

        for (int i = 0; i < count; i++)
        {
            log("attribute", attributes.getLocalName(i), "value=" + attributes.getValue(i),
                "qName=" + attributes.getQName(i));
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        log("startPrefixMapping", "prefix=" + prefix, "uri=" + uri);
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
            throws SAXException
    {
        log("unparsedEntityDecl", name, "publicId=" + publicId, "systemId=" + systemId);
    }

}
