// Copyright 2009-2013 The Apache Software Foundation
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

import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.LocationImpl;
import org.xml.sax.*;
import org.xml.sax.ext.Attributes2;
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

    public static final String TRANSITIONAL_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";

    private static final DTDData HTML5_DTD_DATA = new DTDData("html", null, null);

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
            if (locator == null)
            {
                if (cachedLocation == null)
                {
                    cachedLocation = new LocationImpl(resource);
                }
            } else {
                int line = locator.getLineNumber();

                if (currentLine != line)
                    cachedLocation = null;

                if (cachedLocation == null)
                {
                    // lineOffset accounts for the extra line when a doctype is injected. The line number reported
                    // from the XML parser inlcudes the phantom doctype line, the lineOffset is used to subtract one
                    // to get the real line number.
                    cachedLocation = new LocationImpl(resource, line + lineOffset);
                }
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
                        url, ExceptionUtils.toMessage(ex)), ex);
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
            characters(ch, start, length);
        }

        public void startDTD(final String name, final String publicId, final String systemId)
                throws SAXException
        {
            insideDTD = true;

            if (!ignoreDTD)
            {
                DTDData data = html5DTD ? HTML5_DTD_DATA : new DTDData(name, publicId, systemId);

                add(XMLTokenType.DTD).dtdData = data;
            }
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

        /**
         * Checks for the extra namespace injected when the transitional doctype is injected (which
         * occurs when the template contains no doctype).
         */
        private boolean ignoreURI(String uri)
        {
            return ignoreDTD && uri.equals("http://www.w3.org/1999/xhtml");
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException
        {
            XMLToken token = add(XMLTokenType.START_ELEMENT);

            token.uri = ignoreURI(uri) ? "" : uri;
            token.localName = localName;
            token.qName = qName;

            // The XML parser tends to reuse the same Attributes object, so
            // capture the data out of it.

            Attributes2 a2 = (attributes instanceof Attributes2) ? (Attributes2) attributes : null;

            if (attributes.getLength() == 0)
            {
                token.attributes = Collections.emptyList();
            } else
            {
                token.attributes = CollectionFactory.newList();

                for (int i = 0; i < attributes.getLength(); i++)
                {
                    // Filter out attributes that are not present in the XML input stream, but were
                    // instead provided by DTD defaulting.

                    if (a2 != null && !a2.isSpecified(i))
                    {
                        continue;
                    }

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
            if (ignoreDTD && prefix.equals("") && uri.equals("http://www.w3.org/1999/xhtml"))
            {
                return;
            }

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

    private boolean html5DTD, ignoreDTD;

    private int lineOffset;

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

        InputStream stream = null;

        try
        {
            stream = openStream();
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

    enum State
    {
        MAYBE_XML, MAYBE_DOCTYPE, JUST_COPY
    }

    private InputStream openStream() throws IOException
    {
        InputStream rawStream = resource.openStream();

        String transformationEncoding = "UTF8";

        InputStreamReader rawReader = new InputStreamReader(rawStream, transformationEncoding);
        LineNumberReader reader = new LineNumberReader(rawReader);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(bos, transformationEncoding));

        State state = State.MAYBE_XML;

        try
        {
            while (true)
            {
                String line = reader.readLine();

                if (line == null)
                {
                    break;
                }

                switch (state)
                {

                    case MAYBE_XML:

                        if (line.toLowerCase().startsWith("<?xml"))
                        {
                            writer.println(line);
                            state = State.MAYBE_DOCTYPE;
                            continue;
                        }

                    case MAYBE_DOCTYPE:

                        if (line.trim().length() == 0)
                        {
                            writer.println(line);
                            continue;
                        }

                        String lineLower = line.toLowerCase();

                        if (lineLower.equals("<!doctype html>"))
                        {
                            html5DTD = true;
                            writer.println(TRANSITIONAL_DOCTYPE);
                            state = State.JUST_COPY;
                            continue;
                        }


                        if (lineLower.startsWith("<!doctype"))
                        {
                            writer.println(line);
                            state = State.JUST_COPY;
                            continue;
                        }

                        // No doctype, let's provide one.

                        ignoreDTD = true;
                        lineOffset = -1;
                        writer.println(TRANSITIONAL_DOCTYPE);

                        state = State.JUST_COPY;

                        // And drop down to writing out the actual line, and all following lines.

                    case JUST_COPY:
                        writer.println(line);
                }
            }
        } finally
        {
            writer.close();
            reader.close();
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }

    private XMLToken token()
    {
        return cursor == -1 ? null : tokens.get(cursor);
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
