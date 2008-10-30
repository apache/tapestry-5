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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.parser.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.LocationImpl;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.util.Stack;
import org.slf4j.Logger;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Non-threadsafe implementation; the IOC service uses the perthread lifecycle.
 */
@Scope(ScopeConstants.PERTHREAD)
public class TemplateParserImpl implements TemplateParser, LexicalHandler, ContentHandler, EntityResolver
{
    private static final String MIXINS_ATTRIBUTE_NAME = "mixins";

    private static final String TYPE_ATTRIBUTE_NAME = "type";

    private static final String ID_ATTRIBUTE_NAME = "id";

    public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

    /**
     * Used as the namespace URI for Tapestry templates.
     */
    public static final String TAPESTRY_SCHEMA_5_0_0 = "http://tapestry.apache.org/schema/tapestry_5_0_0.xsd";

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-z]\\w*$", Pattern.CASE_INSENSITIVE);

    /**
     * Any amount of mixed simple whitespace (space, tab, form feed) mixed with at least one carriage return or line
     * feed, followed by any amount of whitespace.  Will be reduced to a single linefeed.
     */
    private static final Pattern REDUCE_LINEBREAKS_PATTERN = Pattern.compile("[ \\t\\f]*[\\r\\n]\\s*",
                                                                             Pattern.MULTILINE);

    /**
     * Used when compressing whitespace, matches any sequence of simple whitespace (space, tab, formfeed). Applied after
     * REDUCE_LINEBREAKS_PATTERN.
     */
    private static final Pattern REDUCE_WHITESPACE_PATTERN = Pattern.compile("[ \\t\\f]+", Pattern.MULTILINE);

    // Note the use of the non-greedy modifier; this prevents the pattern from merging multiple
    // expansions on the same text line into a single large
    // but invalid expansion.

    private static final String EXPANSION_REGEXP = "\\$\\{\\s*(.*?)\\s*}";

    private static final Pattern EXPANSION_PATTERN = Pattern.compile(EXPANSION_REGEXP);


    private XMLReader reader;

    // Resource being parsed
    private Resource templateResource;

    private Locator locator;

    private final List<TemplateToken> tokens = CollectionFactory.newList();

    private final boolean compressWhitespaceDefault;

    /**
     * Because {@link org.xml.sax.ContentHandler#startPrefixMapping(String, String)} events arrive before the
     * corresponding {@link org.xml.sax.ContentHandler#startElement(String, String, String, org.xml.sax.Attributes)}
     * events, we need to accumlate the {@link org.apache.tapestry5.internal.parser.DefineNamespacePrefixToken}s ahead
     * of time to get the correct ordering in the output tokens list.
     */
    private final List<DefineNamespacePrefixToken> defineNamespaceTokens = CollectionFactory.newList();

    // Non-blank ids from start component elements

    private final Map<String, Location> componentIds = CollectionFactory.newMap();

    // Used to accumulate text provided by the characters() method. Even contiguous characters may
    // be broken up across multiple invocations due to parser internals. We accumulate those
    // together before forming a text token.

    private final StringBuilder textBuffer = new StringBuilder();

    private Location textStartLocation;

    private boolean textIsCData;

    private boolean insideBody;

    private boolean insideBodyErrorLogged;

    private boolean ignoreEvents;

    private final Logger logger;

    private final Map<String, URL> configuration;

    private final Stack<Runnable> endTagHandlerStack = new Stack<Runnable>();

    private boolean compressWhitespace;

    private final Stack<Boolean> compressWhitespaceStack = new Stack<Boolean>();

    private final Runnable endOfElementHandler = new Runnable()
    {
        public void run()
        {
            tokens.add(new EndElementToken(getCurrentLocation()));

            // Restore the flag to how it was before the element was parsed.

            compressWhitespace = compressWhitespaceStack.pop();
        }
    };

    private final Runnable ignoreEndElement = new Runnable()
    {
        public void run()
        {
            compressWhitespace = compressWhitespaceStack.pop();
        }
    };


    public TemplateParserImpl(Logger logger, Map<String, URL> configuration,

                              @Symbol(SymbolConstants.COMPRESS_WHITESPACE)
                              boolean compressWhitespaceDefault)
    {
        this.logger = logger;
        this.configuration = configuration;
        this.compressWhitespaceDefault = compressWhitespaceDefault;

        reset();
    }

    private void reset()
    {
        tokens.clear();
        defineNamespaceTokens.clear();
        componentIds.clear();
        templateResource = null;
        locator = null;
        textBuffer.setLength(0);
        textStartLocation = null;
        textIsCData = false;
        insideBody = false;
        insideBodyErrorLogged = false;
        ignoreEvents = true;

        endTagHandlerStack.clear();
        compressWhitespaceStack.clear();
    }

    public ComponentTemplate parseTemplate(Resource templateResource)
    {
        compressWhitespace = compressWhitespaceDefault;

        if (reader == null)
        {
            try
            {
                reader = XMLReaderFactory.createXMLReader();

                reader.setContentHandler(this);

                reader.setEntityResolver(this);

                reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

                reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ServicesMessages.newParserError(templateResource, ex), ex);
            }
        }

        if (!templateResource.exists())
            throw new RuntimeException(ServicesMessages.missingTemplateResource(templateResource));

        this.templateResource = templateResource;

        try
        {
            InputSource source = new InputSource(templateResource.openStream());

            reader.parse(source);

            return new ComponentTemplateImpl(this.templateResource, tokens, componentIds);
        }
        catch (Exception ex)
        {
            // Some parsers get in an unknown state when an error occurs, and are are not
            // subsequently useable.

            reader = null;

            throw new TapestryException(ServicesMessages.templateParseError(templateResource, ex), getCurrentLocation(),
                                        ex);
        }
        finally
        {
            reset();
        }
    }

    public void setDocumentLocator(Locator locator)
    {
        this.locator = locator;
    }

    /**
     * Accumulates the characters into a text buffer.
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (ignoreEvents) return;

        if (insideBody()) return;

        if (textBuffer.length() == 0) textStartLocation = getCurrentLocation();

        textBuffer.append(ch, start, length);
    }


    /**
     * Adds tokens corresponding to the content in the text buffer. For a non-CDATA section, we also search for
     * expansions (thus we may add more than one token). Clears the text buffer.
     */
    private void processTextBuffer()
    {
        if (textBuffer.length() == 0) return;

        String text = textBuffer.toString();

        textBuffer.setLength(0);

        if (textIsCData)
        {
            tokens.add(new CDATAToken(text, textStartLocation));

            return;
        }

        if (compressWhitespace)
        {
            text = compressWhitespaceInText(text);

            if (InternalUtils.isBlank(text)) return;
        }


        addTokensForText(text);
    }

    private String compressWhitespaceInText(String text)
    {
        String linebreaksReduced = REDUCE_LINEBREAKS_PATTERN.matcher(text).replaceAll("\n");

        return REDUCE_WHITESPACE_PATTERN.matcher(linebreaksReduced).replaceAll(" ");
    }

    /**
     * Scans the text, using a regular expression pattern, for expansion patterns, and adds appropriate tokens for what
     * it finds.
     *
     * @param text to add as {@link org.apache.tapestry5.internal.parser.TextToken}s and {@link
     *             org.apache.tapestry5.internal.parser.ExpansionToken}s
     */
    private void addTokensForText(String text)
    {
        Matcher matcher = EXPANSION_PATTERN.matcher(text);

        int startx = 0;

        // The big problem with all this code is that everything gets assigned to the
        // start of the text block, even if there are line breaks leading up to it.
        // That's going to take a lot more work and there are bigger fish to fry.  In addition,
        // TAPESTRY-2028 means that the whitespace has likely been stripped out of the text
        // already anyway.

        while (matcher.find())
        {
            int matchStart = matcher.start();

            if (matchStart != startx)
            {
                String prefix = text.substring(startx, matchStart);

                tokens.add(new TextToken(prefix, textStartLocation));
            }

            // Group 1 includes the real text of the expansion, with whitespace around the
            // expression (but inside the curly braces) excluded.

            String expression = matcher.group(1);

            tokens.add(new ExpansionToken(expression, textStartLocation));

            startx = matcher.end();
        }

        // Catch anything after the final regexp match.

        if (startx < text.length())
            tokens.add(new TextToken(text.substring(startx, text.length()), textStartLocation));
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        ignoreEvents = false;

        if (insideBody) throw new IllegalStateException(ServicesMessages
                .mayNotNestElementsInsideBody(localName));

        // Add any accumulated text into a text token
        processTextBuffer();

        if (TAPESTRY_SCHEMA_5_0_0.equals(uri))
        {
            startTapestryElement(localName, attributes);
            return;
        }

        startPossibleComponent(attributes, uri, localName, null);
    }

    /**
     * Checks to see if currently inside a t:body element (which should always be empty). Content is ignored inside a
     * body. If inside a body, then a warning is logged (but only one warning per body element).
     *
     * @return true if inside t:body, false otherwise
     */
    private boolean insideBody()
    {
        if (insideBody)
        {
            // Limit to one logged error per infraction.

            if (!insideBodyErrorLogged)
                logger.error(ServicesMessages.contentInsideBodyNotAllowed(getCurrentLocation()));

            insideBodyErrorLogged = true;
        }

        return insideBody;
    }

    private void startTapestryElement(String localName, Attributes attributes)
    {
        if (localName.equalsIgnoreCase("body"))
        {
            startBody();
            return;
        }

        if (localName.equalsIgnoreCase("parameter"))
        {
            startParameter(attributes);
            return;
        }

        if (localName.equalsIgnoreCase("block"))
        {
            startBlock(attributes);
            return;
        }

        if (localName.equalsIgnoreCase("container"))
        {
            startContainer(localName, attributes);
            return;
        }

        // The component type is derived from the element name. Since element names may not contain
        // slashes, we convert periods to slashes. Later down the pipeline, they'll probably be
        // converted back into periods, as part of a fully qualified class name.

        String componentType = localName.replace('.', '/');

        // With a component type specified, it's not just possibly a component ...
        startPossibleComponent(attributes, null, null, componentType);
    }

    private void startContainer(String elementName, Attributes attributes)
    {
        compressWhitespaceStack.push(compressWhitespace);

        // Neither the container nor its end tag are considered tokens, just the contents inside.

        endTagHandlerStack.push(ignoreEndElement);

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String name = attributes.getLocalName(i);

            // The name will be blank for an xmlns: attribute

            if (InternalUtils.isBlank(name)) continue;

            String uri = attributes.getURI(i);
            String value = attributes.getValue(i);

            if (isXMLSpaceAttribute(uri, name, value)) continue;

            throw new TapestryException(ServicesMessages.attributeNotAllowed(elementName), getCurrentLocation(), null);
        }
    }

    private void startBlock(Attributes attributes)
    {
        addEndOfElementHandler();

        String blockId = findSingleParameter("block", "id", attributes);

        validateId(blockId, "invalid-block-id");

        // null is ok for blockId

        tokens.add(new BlockToken(blockId, getCurrentLocation()));

        // TODO: Check for an xml:space attribute
    }

    private void startParameter(Attributes attributes)
    {
        addEndOfElementHandler();

        String parameterName = findSingleParameter("parameter", "name", attributes);

        if (InternalUtils.isBlank(parameterName))
            throw new TapestryException(ServicesMessages.parameterElementNameRequired(), getCurrentLocation(), null);

        tokens.add(new ParameterToken(parameterName, getCurrentLocation()));
    }

    /**
     * Should be called *before* the _compressWhitespace is changed.
     */
    private void addEndOfElementHandler()
    {
        // Record how the flag was set at the start of the element

        compressWhitespaceStack.push(compressWhitespace);

        endTagHandlerStack.push(endOfElementHandler);
    }

    private String findSingleParameter(String elementName, String attributeName, Attributes attributes)
    {
        String result = null;

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String uri = attributes.getURI(i);
            String name = attributes.getLocalName(i);
            String value = attributes.getValue(i);

            if (isXMLSpaceAttribute(uri, name, value)) continue;

            if (name.equals(attributeName))
            {
                result = value;
                continue;
            }

            // Only the named attribute is allowed.

            throw new TapestryException(ServicesMessages.undefinedTapestryAttribute(elementName, name, attributeName),
                                        getCurrentLocation(), null);
        }

        return result;
    }

    private boolean isXMLSpaceAttribute(String uri, String name, String value)
    {

        if (uri.equals(XML_NAMESPACE_URI) && name.equals("space"))
        {
            // "preserve" turns off whitespace compression
            // "default" (the other option, but we'll accept anything) turns it on (or leaves it on, more likely).

            compressWhitespace = !"preserve".equalsIgnoreCase(value);

            return true;
        }

        return false;
    }

    private String nullForBlank(String input)
    {
        return InternalUtils.isBlank(input) ? null : input;
    }

    /**
     * @param attributes     the attributes for the element
     * @param namespaceURI   the namespace URI for the element (or the empty string)
     * @param elementName    the name of the element (to be assigned to the new token), may be null for a component in
     *                       the Tapestry namespace
     * @param identifiedType the type of the element, usually null, but may be the component type derived from elewment
     *                       name
     */
    private void startPossibleComponent(Attributes attributes, String namespaceURI, String elementName,
                                        String identifiedType)
    {

        // Add an end handler to match this start tag.

        addEndOfElementHandler();

        String id = null;
        String type = identifiedType;
        String mixins = null;
        int count = attributes.getLength();
        Location location = getCurrentLocation();
        List<TemplateToken> attributeTokens = CollectionFactory.newList();

        for (int i = 0; i < count; i++)
        {
            String name = attributes.getLocalName(i);

            // The name will be blank for an xmlns: attribute

            if (InternalUtils.isBlank(name)) continue;

            String uri = attributes.getURI(i);

            String value = attributes.getValue(i);

            if (TAPESTRY_SCHEMA_5_0_0.equals(uri))
            {
                if (name.equalsIgnoreCase(ID_ATTRIBUTE_NAME))
                {
                    id = nullForBlank(value);

                    validateId(id, "invalid-component-id");

                    continue;
                }

                if (type == null && name.equalsIgnoreCase(TYPE_ATTRIBUTE_NAME))
                {
                    type = nullForBlank(value);
                    continue;
                }

                if (name.equalsIgnoreCase(MIXINS_ATTRIBUTE_NAME))
                {
                    mixins = nullForBlank(value);
                    continue;
                }

                // Anything else is the name of a Tapestry component parameter that is simply
                // not part of the template's doctype for the element being instrumented.
            }


            if (isXMLSpaceAttribute(uri, name, value)) continue;

            attributeTokens.add(new AttributeToken(uri, name, value, location));
        }

        boolean isComponent = (id != null || type != null);

        // If provided t:mixins but not t:id or t:type, then its not quite a component

        if (mixins != null && !isComponent)
            throw new TapestryException(ServicesMessages.mixinsInvalidWithoutIdOrType(elementName), location, null);

        if (isComponent)
        {
            tokens.add(new StartComponentToken(elementName, id, type, mixins, location));
        }
        else
        {
            tokens.add(new StartElementToken(namespaceURI, elementName, location));
        }

        addDefineNamespaceTokens();

        tokens.addAll(attributeTokens);

        if (id != null)
            componentIds.put(id, location);

        // TODO: Is there value in having different end elements for components vs. ordinary
        // elements?

    }

    private void validateId(String id, String messageKey)
    {
        if (id == null) return;

        if (ID_PATTERN.matcher(id).matches()) return;

        // Not a match.

        throw new TapestryException(ServicesMessages.invalidId(messageKey, id), getCurrentLocation(), null);
    }

    private void startBody()
    {
        tokens.add(new BodyToken(getCurrentLocation()));

        insideBody = true;
        insideBodyErrorLogged = false;

        endTagHandlerStack.push(new Runnable()
        {
            public void run()
            {
                insideBody = false;

                // And don't add an end element token.
            }
        });
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        processTextBuffer();

        endTagHandlerStack.pop().run();
    }

    private Location getCurrentLocation()
    {
        if (locator == null) return null;

        return new LocationImpl(templateResource, locator.getLineNumber(), locator
                .getColumnNumber());
    }

    /**
     * Adds any namespace tokens accumulated from just before the current element. The list of namespace tokens is then
     * cleared.
     */
    private void addDefineNamespaceTokens()
    {
        tokens.addAll(defineNamespaceTokens);

        defineNamespaceTokens.clear();
    }

    public void comment(char[] ch, int start, int length) throws SAXException
    {
        if (ignoreEvents || insideBody()) return;

        processTextBuffer();

        // Remove excess whitespace. The Comment DOM node will add a leadig and trailing space.

        String comment = new String(ch, start, length).trim();

        // TODO: Perhaps comments need to be "aggregated" the same way we aggregate text and CDATA.
        // Hm. Probably not. Any whitespace between one comment and the next will become a
        // TextToken.
        // Unless we trim whitespace between consecutive comments ... and on down the rabbit hole.
        // Oops -- unless a single comment may be passed into this method as multiple calls
        // (have to check how multiline comments are handled).
        // Tests against Sun's built in parser does show that multiline comments are still
        // provided as a single call to comment(), so we're good for the meantime (until we find
        // out some parsers aren't so compliant).

        tokens.add(new CommentToken(comment, getCurrentLocation()));
    }

    public void endCDATA() throws SAXException
    {
        // Add a token for any accumulated CDATA.

        processTextBuffer();

        // Again, CDATA doesn't nest, so we know we're back to ordinary markup.

        textIsCData = false;
    }

    public void startCDATA() throws SAXException
    {
        if (ignoreEvents || insideBody()) return;

        processTextBuffer();

        // Because CDATA doesn't mix with any other SAX/lexical events, we can simply turn on a flag
        // here and turn it off when we see the end.

        textIsCData = true;
    }

    // Empty methods defined by the various interfaces.

    public void endDTD() throws SAXException
    {
    }

    public void endEntity(String name) throws SAXException
    {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
        // notes:
        // 1) a DTD has to occur at the very start of a document. Since we don't start
        // recording characters until we hit the first element of a document (see
        // characters and startElement), there should be no text to process.
        // It's worth noting that the sax parser will puke if any of the following
        // occur:
        // 1) a doctype is encountered multiple times in the same document
        // 2) a doctype is encountered anywhere other than the very first item
        // in a document.
        // Hence, the assumption made in 1 should hold.
        // Since an exception is thrown for case #1 above, we can just add the DTDToken.
        // When we go to process the token (in PageLoaderProcessor), we can make sure
        // that the final page has only a single DTDToken (the first one).
        tokens.add(new DTDToken(name, publicId, systemId, getCurrentLocation()));
    }

    public void startEntity(String name) throws SAXException
    {
    }

    public void endDocument() throws SAXException
    {
    }


    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
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

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        // Not interested in the Tapestry namespace (that is never sent to the client).

        if (uri.equals(TAPESTRY_SCHEMA_5_0_0)) return;

        // The prefix may be blank, which happens when the xmlns attribute is used to define the
        // namespace for the default namespace, and when a document has an explicit DOCTYPE.

        DefineNamespacePrefixToken token = new DefineNamespacePrefixToken(uri, prefix, getCurrentLocation());

        defineNamespaceTokens.add(token);
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
    {
        URL url = configuration.get(publicId);

        if (url != null) return new InputSource(url.openStream());

        return null;
    }
}
