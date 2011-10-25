// Copyright 2009, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.LocationImpl;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parses a document as a stream of XML tokens. It includes a special hack (as of Tapestry 5.3) to support the HTML5 doctype ({@code <!DOCTYPE html>})
 * as if it were the XHTML transitional doctype
 * ({@code <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">}).
 */
public class XMLTokenStream
{
    private final class SaxHandler implements LexicalHandler, EntityResolver, ContentHandler
    {
        private Locator locator;

        private int currentLine = -1;

        private Location cachedLocation;

        private Location textLocation;

        private final StringBuilder builder = new StringBuilder();

        private boolean inCDATA, insideDTD;

        private List<NamespaceMapping> namespaceMappings = CollectionFactory.newList();

        private Location getLocation()
        {
            int line = locator.getLineNumber();

            if (currentLine != line)
                cachedLocation = null;

            if (cachedLocation == null)
            {
                cachedLocation = new LocationImpl(resource, line);
            }

            return cachedLocation;
        }

        private XMLToken add(XMLTokenType type)
        {
            XMLToken token = new XMLToken(type, getLocation());

            tokens.add(token);

            return token;
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                IOException
        {
            URL url = publicIdToURL.get(publicId);

            try
            {
                if (url != null)
                    return new InputSource(url.openStream());
            } catch (IOException ex)
            {
                throw new SAXException(String.format("Unable to open stream for resource %s: %s",
                        url, InternalUtils.toMessage(ex)), ex);
            }

            return null;
        }

        public void comment(char[] ch, int start, int length) throws SAXException
        {
            if (insideDTD)
                return;

            // TODO: Coalesce?
            add(XMLTokenType.COMMENT).text = new String(ch, start, length);
        }

        public void startCDATA() throws SAXException
        {
            // TODO: Flush characters?

            inCDATA = true;
        }

        public void endCDATA() throws SAXException
        {
            if (builder.length() != 0)
            {
                add(XMLTokenType.CDATA).text = builder.toString();
            }

            builder.setLength(0);
            inCDATA = false;
        }

        public void characters(char[] ch, int start, int length) throws SAXException
        {
            if (inCDATA)
            {
                builder.append(ch, start, length);
                return;
            }

            XMLToken token = new XMLToken(XMLTokenType.CHARACTERS, textLocation);
            token.text = new String(ch, start, length);

            tokens.add(token);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
        {
        }

        public void startDTD(final String name, final String publicId, final String systemId)
                throws SAXException
        {
            insideDTD = true;

            DTDData data = new DTDData()
            {

                public String getSystemId()
                {
                    return html5DTD ? null : systemId;
                }

                public String getRootName()
                {
                    return name;
                }

                public String getPublicId()
                {
                    return html5DTD ? null : publicId;
                }
            };

            add(XMLTokenType.DTD).dtdData = data;
        }

        public void endDocument() throws SAXException
        {
            add(XMLTokenType.END_DOCUMENT);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            add(XMLTokenType.END_ELEMENT);
        }

        public void setDocumentLocator(Locator locator)
        {
            this.locator = locator;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException
        {
            XMLToken token = add(XMLTokenType.START_ELEMENT);

            token.uri = uri;
            token.localName = localName;
            token.qName = qName;

            // The XML parser tends to reuse the same Attributes object, so
            // capture the data out of it.

            if (attributes.getLength() == 0)
            {
                token.attributes = Collections.emptyList();
            } else
            {
                token.attributes = CollectionFactory.newList();

                for (int i = 0; i < attributes.getLength(); i++)
                {
                    String prefixedName = attributes.getQName(i);

                    int lastColon = prefixedName.lastIndexOf(':');

                    String prefix = lastColon > 0 ? prefixedName.substring(0, lastColon) : "";

                    QName qname = new QName(attributes.getURI(i), attributes.getLocalName(i),
                            prefix);

                    token.attributes.add(new AttributeInfo(qname, attributes.getValue(i)));
                }
            }

            token.namespaceMappings = CollectionFactory.newList(namespaceMappings);

            namespaceMappings.clear();

            // Any text collected starts here as well:

            textLocation = getLocation();
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException
        {
            namespaceMappings.add(new NamespaceMapping(prefix, uri));
        }

        public void endDTD() throws SAXException
        {
            insideDTD = false;
        }

        public void endEntity(String name) throws SAXException
        {
        }

        public void startEntity(String name) throws SAXException
        {
        }

        public void endPrefixMapping(String prefix) throws SAXException
        {
        }

        public void processingInstruction(String target, String data) throws SAXException
        {
        }

        public void skippedEntity(String name) throws SAXException
        {
        }

        public void startDocument() throws SAXException
        {
        }
    }

    private int cursor = -1;

    private final List<XMLToken> tokens = CollectionFactory.newList();

    private final Resource resource;

    private final Map<String, URL> publicIdToURL;

    private Location exceptionLocation;

    private boolean html5DTD;

    public XMLTokenStream(Resource resource, Map<String, URL> publicIdToURL)
    {
        this.resource = resource;
        this.publicIdToURL = publicIdToURL;
    }

    public void parse() throws SAXException, IOException
    {
        SaxHandler handler = new SaxHandler();

        XMLReader reader = XMLReaderFactory.createXMLReader();

        reader.setContentHandler(handler);
        reader.setEntityResolver(handler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

        InputStream stream = openStream();

        try
        {
            reader.parse(new InputSource(stream));
        } catch (IOException ex)
        {
            this.exceptionLocation = handler.getLocation();

            throw ex;
        } catch (SAXException ex)
        {
            this.exceptionLocation = handler.getLocation();

            throw ex;
        } catch (RuntimeException ex)
        {
            this.exceptionLocation = handler.getLocation();

            throw ex;
        } finally
        {
            InternalUtils.close(stream);
        }
    }

    private InputStream openStream() throws IOException
    {

        InputStream rawStream = resource.openStream();

        InputStreamReader rawReader = new InputStreamReader(rawStream);
        LineNumberReader reader = new LineNumberReader(rawReader);

        try
        {
            String firstLine = reader.readLine();

            if ("<!DOCTYPE html>".equalsIgnoreCase(firstLine))
            {
                // When we hit the doctype later, ignore the transitional PUBLIC and SYSTEM ids and
                // treat it like a proper HTML5 doctype.
                html5DTD = true;
                return substituteTransitionalDoctype(reader);
            }

            // Open a fresh stream for the parser to operate on.

            return resource.openStream();

        } finally
        {
            reader.close();
        }
    }

    private InputStream substituteTransitionalDoctype(LineNumberReader reader) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
        PrintWriter writer = new PrintWriter(bos);

        writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");

        while (true)
        {
            String line = reader.readLine();
            if (line == null)
            {
                break;
            }

            writer.println(line);
        }

        writer.close();

        return new ByteArrayInputStream(bos.toByteArray());
    }


    private XMLToken token()
    {
        return tokens.get(cursor);
    }

    /**
     * Returns the type of the next token.
     */
    public XMLTokenType next()
    {
        cursor++;

        // TODO: Check for overflow?

        return getEventType();
    }

    public int getAttributeCount()
    {
        return token().attributes.size();
    }

    public QName getAttributeName(int i)
    {
        return token().attributes.get(i).attributeName;
    }

    public DTDData getDTDInfo()
    {
        return token().dtdData;
    }

    public XMLTokenType getEventType()
    {
        return token().type;
    }

    public String getLocalName()
    {
        return token().localName;
    }

    public Location getLocation()
    {
        if (exceptionLocation != null)
            return exceptionLocation;

        return token().getLocation();
    }

    public int getNamespaceCount()
    {
        return token().namespaceMappings.size();
    }

    public String getNamespacePrefix(int i)
    {
        return token().namespaceMappings.get(i).prefix;
    }

    public String getNamespaceURI()
    {
        return token().uri;
    }

    public String getNamespaceURI(int i)
    {
        return token().namespaceMappings.get(i).uri;
    }

    public String getText()
    {
        return token().text;
    }

    public boolean hasNext()
    {
        return cursor < tokens.size() - 1;
    }

    public String getAttributeValue(int i)
    {
        return token().attributes.get(i).value;
    }

}
