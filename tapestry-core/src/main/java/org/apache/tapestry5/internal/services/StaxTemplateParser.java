// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.parser.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.LocationImpl;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.codehaus.stax2.DTDInfo;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link org.apache.tapestry5.internal.services.TemplateParser} based on the <a
 * href="http://en.wikipedia.org/wiki/StAX">Streaming API for XML</a>.   It uses a few features of Stax2 and is
 * therefore dependent on the Woodstock STAX parser.
 */
@SuppressWarnings({ "JavaDoc" })
public class StaxTemplateParser
{
    private static final String MIXINS_ATTRIBUTE_NAME = "mixins";

    private static final String TYPE_ATTRIBUTE_NAME = "type";

    private static final String ID_ATTRIBUTE_NAME = "id";

    public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

    /**
     * Used as the namespace URI for Tapestry templates.
     */
    public static final String TAPESTRY_SCHEMA_5_0_0 = "http://tapestry.apache.org/schema/tapestry_5_0_0.xsd";

    /**
     * Adds several new elements.
     */
    public static final String TAPESTRY_SCHEMA_5_1_0 = "http://tapestry.apache.org/schema/tapestry_5_1_0.xsd";

    // Might want to change this from a Set to a map from URI to version number (if we hit a 3rd version of the namespace URI).
    private static final Set<String> TAPESTRY_SCHEMA_URIS = CollectionFactory.newSet(TAPESTRY_SCHEMA_5_0_0,
                                                                                     TAPESTRY_SCHEMA_5_1_0);

    /**
     * Special namespace used to denote Block parameters to components, as a (preferred) alternative to the t:parameter
     * element.  The simple element name is the name of the parameter.
     */
    private static final String TAPESTRY_PARAMETERS_URI = "tapestry:parameter";

    /**
     * URI prefix used to identify a Tapestry library, the remainder of the URI becomes a prefix on the element name.
     */
    private static final String LIB_NAMESPACE_URI_PREFIX = "tapestry-library:";

    /**
     * Pattern used to parse the path portion of the library namespace URI.  A series of simple identifiers with slashes
     * allowed as seperators.
     */

    private static final Pattern LIBRARY_PATH_PATTERN = Pattern.compile("^[a-z]\\w*(/[a-z]\\w*)*$",
                                                                        Pattern.CASE_INSENSITIVE);

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

    private static final Pattern EXPANSION_PATTERN = Pattern.compile("\\$\\{\\s*(.*?)\\s*}");

    private static final String[] EVENT_NAMES = { "",
            "START_ELEMENT", "END_ELEMENT", "PROCESSING_INSTRUCTION",
            "CHARACTERS", "COMMENT", "SPACE", "START_DOCUMENT",
            "END_DOCUMENT", "ENTITY_REFERENCE", "ATTRIBUTE", "DTD", "CDATA",
            "NAMESPACE", "NOTATION_DECLARATION", "ENTITY_DECLARATION" };

    private static final Set<String> MUST_BE_ROOT = CollectionFactory.newSet("extend", "container");

    private final Resource resource;

    private final XMLStreamReader2 reader;

    private final StringBuilder textBuffer = new StringBuilder();

    private final List<TemplateToken> tokens = CollectionFactory.newList();

    // This starts pointing at tokens but occasionally shifts to a list inside the overrides Map.
    private List<TemplateToken> tokenAccumulator = tokens;

    /**
     * Primarily used as a set of componentIds (to check for duplicates and conflicts).
     */
    private final Map<String, Location> componentIds = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Map from override id to a list of tokens; this actually works both for overrides defined by this template and
     * overrides provided by this template.
     */
    private Map<String, List<TemplateToken>> overrides;

    private boolean extension;

    private Location textStartLocation;

    private boolean active = true;

    private Location cachedLocation;

    public StaxTemplateParser(Resource resource, XMLInputFactory2 inputFactory) throws XMLStreamException, IOException
    {
        this.resource = resource;
        this.reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(resource.openStream());
    }

    public ComponentTemplate parse(boolean compressWhitespace)
    {
        TemplateParserState initialParserState = new TemplateParserState().compressWhitespace(compressWhitespace);

        try
        {
            root(initialParserState);

            reader.close();
        }
        catch (Exception ex)
        {
            try
            {
                reader.closeCompletely();
            }
            catch (XMLStreamException e)
            {
                // Ignore it.
            }

            throw new TapestryException(ServicesMessages.templateParseError(resource, ex), getLocation(),
                                        ex);
        }

        return new ComponentTemplateImpl(resource, tokens, componentIds, extension, overrides);
    }

    void root(TemplateParserState state) throws XMLStreamException
    {
        while (active && reader.hasNext())
        {
            switch (reader.next())
            {
                case DTD:

                    dtd();

                    break;

                case START_ELEMENT:

                    rootElement(state);

                    break;

                case END_DOCUMENT:
                    // Ignore it.
                    break;

                default:
                    textContent(state);
            }
        }
    }

    private void rootElement(TemplateParserState initialState) throws XMLStreamException
    {
        TemplateParserState state = setupForElement(initialState);

        String uri = reader.getNamespaceURI();
        String name = reader.getLocalName();

        if (TAPESTRY_SCHEMA_5_1_0.equals(uri))
        {
            if (name.equalsIgnoreCase("extend"))
            {
                extend(state);
                return;
            }
        }

        if (TAPESTRY_SCHEMA_URIS.contains(uri))
        {
            if (name.equalsIgnoreCase("container"))
            {
                container(state);
                return;
            }
        }


        element(state);
    }

    private void extend(TemplateParserState state) throws XMLStreamException
    {
        extension = true;

        while (active)
        {
            switch (reader.next())
            {
                case START_ELEMENT:

                    if (reader.getNamespaceURI().equals(
                            TAPESTRY_SCHEMA_5_1_0) && reader.getLocalName().equalsIgnoreCase(
                            "replace"))
                    {
                        replace(state);
                        break;
                    }

                    throw new RuntimeException("Child element of <extend> must be <replace>.");

                case END_ELEMENT:

                    return;

                // Ignore spaces and characters inside <extend>.

                case COMMENT:
                case SPACE:
                    break;

                // Other content (characters, etc.) are forbidden.

                case CHARACTERS:
                    if (InternalUtils.isBlank(reader.getText()))
                        break;

                default:
                    unexpectedEventType();
            }
        }
    }

    private void replace(TemplateParserState state) throws XMLStreamException
    {
        String id = getRequiredIdAttribute();

        addContentToOverride(setupForElement(state), id);
    }

    private void unexpectedEventType()
    {
        int eventType = reader.getEventType();

        throw new IllegalStateException(
                String.format("Unexpected XML parse event %s.", EVENT_NAMES[eventType]));
    }

    private void dtd() throws XMLStreamException
    {
        DTDInfo dtdInfo = reader.getDTDInfo();

        tokenAccumulator.add(new DTDToken(dtdInfo.getDTDRootName(), dtdInfo.getDTDPublicId(), dtdInfo.getDTDSystemId(),
                                          getLocation()));
    }

    private Location getLocation()
    {
        int lineNumber = reader.getLocation().getLineNumber();

        if (cachedLocation != null && cachedLocation.getLine() != lineNumber)
            cachedLocation = null;

        if (cachedLocation == null)
            cachedLocation = new LocationImpl(resource, lineNumber);

        return cachedLocation;
    }

    /**
     * Processes an element through to its matching end tag.
     * <p/>
     * An element can be:
     * <p/>
     * a Tapestry component via &lt;t:type&gt;
     * <p/>
     * a Tapestry component via t:type="type"  and/or t:id="id"
     * <p/>
     * a Tapestry component via a library namespace
     * <p/>
     * A parameter element via &lt;t:parameter&gt;
     * <p/>
     * A parameter element via &lt;p:name&gt;
     * <p/>
     * A &lt;t:remove&gt; element (in the 5.1 schema)
     * <p/>
     * A &lt;t:content&gt; element (in the 5.1 schema)
     * <p/>
     * A &lt;t:block&gt; element
     * <p/>
     * The body &lt;t:body&gt;
     * <p/>
     * An ordinary element
     */
    void element(TemplateParserState initialState) throws XMLStreamException
    {
        TemplateParserState state = setupForElement(initialState);

        String uri = reader.getNamespaceURI();
        String name = reader.getLocalName();

        if (TAPESTRY_SCHEMA_5_1_0.equals(uri))
        {

            if (name.equalsIgnoreCase("remove"))
            {
                removeContent();

                return;
            }

            if (name.equalsIgnoreCase("content"))
            {
                limitContent(state);

                return;
            }

            if (name.equalsIgnoreCase("extension-point"))
            {
                extensionPoint(state);

                return;
            }

            if (name.equalsIgnoreCase("replace"))
            {
                throw new RuntimeException("The <replace> element may only appear directly within an extend element.");
            }

            if (MUST_BE_ROOT.contains(name))
                mustBeRoot(name);
        }

        if (TAPESTRY_SCHEMA_URIS.contains(uri))
        {

            if (name.equalsIgnoreCase("body"))
            {
                body();
                return;
            }

            if (name.equalsIgnoreCase("container"))
            {
                mustBeRoot(name);
            }

            if (name.equalsIgnoreCase("block"))
            {
                block(state);
                return;
            }

            if (name.equalsIgnoreCase("parameter"))
            {
                classicParameter(state);

                return;
            }

            possibleTapestryComponent(state, null, reader.getLocalName().replace('.', '/'));

            return;
        }

        if (uri != null && uri.startsWith(LIB_NAMESPACE_URI_PREFIX))
        {
            libraryNamespaceComponent(state);

            return;
        }

        if (TAPESTRY_PARAMETERS_URI.equals(uri))
        {
            parameterElement(state);

            return;
        }

        // Just an ordinary element ... unless it has t:id or t:type

        possibleTapestryComponent(state, reader.getLocalName(), null);
    }

    /**
     * Processes a body of an element including text and (recursively) nested elements. Adds an {@link
     * org.apache.tapestry5.internal.parser.TokenType#END_ELEMENT} token before returning.
     *
     * @param state
     * @throws XMLStreamException
     */
    private void processBody(TemplateParserState state)
            throws XMLStreamException
    {
        while (active)
        {
            switch (reader.next())
            {
                case START_ELEMENT:

                    // The recursive part: when we see a new element start.

                    element(state);
                    break;

                case END_ELEMENT:

                    // At the end of an element, we're done and can return.
                    // This is the matching end element for the start element
                    // that invoked this method.

                    endElement(state);

                    return;

                default:
                    textContent(state);
            }
        }
    }

    private TemplateParserState setupForElement(TemplateParserState initialState)
    {
        processTextBuffer(initialState);

        return checkForXMLSpaceAttribute(initialState);
    }

    /**
     * Handles an extension point, putting a RenderExtension token in position in the template.
     *
     * @param state
     * @throws XMLStreamException
     */
    private void extensionPoint(TemplateParserState state) throws XMLStreamException
    {
        // An extension point adds a token that represents where the override (either the default
        // provided in the parent template, or the true override from a child template) is positioned.

        String id = getRequiredIdAttribute();

        tokenAccumulator.add(new ExtensionPointToken(id, getLocation()));

        addContentToOverride(state.insideComponent(false), id);
    }

    private String getRequiredIdAttribute()
    {
        String id = getSingleParameter("id");

        if (InternalUtils.isBlank(id))
            throw new RuntimeException(String.format("The <%s> element must have an id attribute.",
                                                     reader.getLocalName()));

        return id;
    }

    private void addContentToOverride(TemplateParserState state, String id)
            throws XMLStreamException
    {
        List<TemplateToken> savedTokenAccumulator = tokenAccumulator;

        tokenAccumulator = CollectionFactory.newList();

        // TODO: id should probably be unique; i.e., you either define an override or you
        // provide an override, but you don't do both in the same template.

        if (overrides == null)
            overrides = CollectionFactory.newCaseInsensitiveMap();

        overrides.put(id, tokenAccumulator);

        while (active)
        {
            switch (reader.next())
            {
                case START_ELEMENT:
                    element(state);
                    break;

                case END_ELEMENT:

                    processTextBuffer(state);

                    // Restore everthing to how it was before the extention-point was reached.

                    tokenAccumulator = savedTokenAccumulator;
                    return;

                default:
                    textContent(state);
            }
        }
    }

    private void mustBeRoot(String name)
    {
        throw new RuntimeException(
                String.format("Element <%s> is only valid as the root element of a template.", name));
    }

    /**
     * Triggered by &lt;t:content&gt; element; limits template content to just what's inside.
     */

    private void limitContent(TemplateParserState state) throws XMLStreamException
    {
        if (state.isCollectingContent())
            throw new IllegalStateException(
                    "The <content> element may not be nested within another <content> element.");


        TemplateParserState newState = state.collectingContent().insideComponent(false);

        // Clear out any tokens that precede the <t:content> element

        tokens.clear();

        // I'm not happy about this; you really shouldn't define overrides just to clear them out,
        // but it is consistent. Perhaps this should be an error if overrides is non-empty.

        overrides = null;

        // Make sure that if the <t:content> appears inside a <t:replace> or <t:extension-point>, that
        // it is still handled correctly.

        tokenAccumulator = tokens;

        while (active)
        {
            switch (reader.next())
            {
                case START_ELEMENT:
                    element(newState);
                    break;

                case END_ELEMENT:

                    // The active flag is global, once we hit it, the entire parse is aborted, leaving
                    // tokens with just tokens defined inside <t:content>.

                    active = false;

                    break;

                default:
                    textContent(state);
            }
        }

    }

    private void removeContent() throws XMLStreamException
    {
        int depth = 1;

        while (active)
        {
            switch (reader.next())
            {
                case START_ELEMENT:
                    depth++;
                    break;

                // The matching end element.

                case END_ELEMENT:
                    depth--;

                    if (depth == 0) return;

                    break;

                default:
                    // Ignore anything else (text, comments, etc.)
            }
        }
    }

    private String nullForBlank(String input)
    {
        return InternalUtils.isBlank(input) ? null : input;
    }


    /**
     * Added in release 5.1.
     */
    private void libraryNamespaceComponent(TemplateParserState state) throws XMLStreamException
    {
        String uri = reader.getNamespaceURI();

        // The library path is encoded into the namespace URI.

        String path = uri.substring(LIB_NAMESPACE_URI_PREFIX.length());

        if (!LIBRARY_PATH_PATTERN.matcher(path).matches())
            throw new RuntimeException(ServicesMessages.invalidPathForLibraryNamespace(uri));

        possibleTapestryComponent(state, null, path + "/" + reader.getLocalName());
    }

    /**
     * @param elementName
     * @param identifiedType the type of the element, usually null, but may be the component type derived from element
     */
    private void possibleTapestryComponent(TemplateParserState state, String elementName, String identifiedType)
            throws XMLStreamException
    {
        String id = null;
        String type = identifiedType;
        String mixins = null;

        int count = reader.getAttributeCount();

        Location location = getLocation();

        List<TemplateToken> attributeTokens = CollectionFactory.newList();

        for (int i = 0; i < count; i++)
        {
            QName qname = reader.getAttributeName(i);

            if (isXMLSpaceAttribute(qname)) continue;

            // The name will be blank for an xmlns: attribute

            String localName = qname.getLocalPart();

            if (InternalUtils.isBlank(localName)) continue;

            String uri = qname.getNamespaceURI();

            String value = reader.getAttributeValue(i);

            if (TAPESTRY_SCHEMA_URIS.contains(uri))
            {
                if (localName.equalsIgnoreCase(ID_ATTRIBUTE_NAME))
                {
                    id = nullForBlank(value);

                    validateId(id, "invalid-component-id");

                    continue;
                }

                if (type == null && localName.equalsIgnoreCase(TYPE_ATTRIBUTE_NAME))
                {
                    type = nullForBlank(value);
                    continue;
                }

                if (localName.equalsIgnoreCase(MIXINS_ATTRIBUTE_NAME))
                {
                    mixins = nullForBlank(value);
                    continue;
                }

                // Anything else is the name of a Tapestry component parameter that is simply
                // not part of the template's doctype for the element being instrumented.
            }


            attributeTokens.add(new AttributeToken(uri, localName, value, location));
        }

        boolean isComponent = (id != null || type != null);

        // If provided t:mixins but not t:id or t:type, then its not quite a component

        if (mixins != null && !isComponent)
            throw new TapestryException(ServicesMessages.mixinsInvalidWithoutIdOrType(elementName), location, null);

        if (isComponent)
        {
            tokenAccumulator.add(new StartComponentToken(elementName, id, type, mixins, location));
        }
        else
        {
            tokenAccumulator.add(new StartElementToken(reader.getNamespaceURI(), elementName, location));
        }

        addDefineNamespaceTokens();

        tokenAccumulator.addAll(attributeTokens);

        if (id != null)
            componentIds.put(id, location);

        processBody(state.insideComponent(isComponent));
    }

    private void addDefineNamespaceTokens()
    {
        for (int i = 0; i < reader.getNamespaceCount(); i++)
        {
            String uri = reader.getNamespaceURI(i);

            // These URIs are strictly part of the server-side Tapestry template and are not ever sent to the client.

            if (TAPESTRY_SCHEMA_URIS.contains(uri)) continue;

            if (uri.equals(TAPESTRY_PARAMETERS_URI)) continue;

            if (uri.startsWith(LIB_NAMESPACE_URI_PREFIX)) continue;

            tokenAccumulator.add(new DefineNamespacePrefixToken(uri, reader.getNamespacePrefix(i),
                                                                getLocation()));
        }
    }


    private TemplateParserState checkForXMLSpaceAttribute(TemplateParserState state)
    {
        for (int i = 0; i < reader.getAttributeCount(); i++)
        {
            QName qName = reader.getAttributeName(i);

            if (isXMLSpaceAttribute(qName))
            {
                boolean compress = !"preserve".equals(reader.getAttributeValue(i));

                return state.compressWhitespace(compress);
            }
        }

        return state;
    }

    /**
     * Processes the text buffer and then adds an end element token.
     */
    private void endElement(TemplateParserState state)
    {
        processTextBuffer(state);

        tokenAccumulator.add(new EndElementToken(getLocation()));
    }

    /**
     * Handler for Tapestry 5.0's "classic" &lt;t:parameter&gt; element. This turns into a {@link
     * org.apache.tapestry5.internal.parser.ParameterToken} and the body and end element are provided normally.
     */
    private void classicParameter(TemplateParserState state) throws XMLStreamException
    {
        String parameterName = getSingleParameter("name");

        if (InternalUtils.isBlank(parameterName))
            throw new TapestryException(ServicesMessages.parameterElementNameRequired(), getLocation(), null);

        ensureParameterWithinComponent(state);

        tokenAccumulator.add(new ParameterToken(parameterName, getLocation()));

        processBody(state.insideComponent(false));
    }

    private void ensureParameterWithinComponent(TemplateParserState state)
    {
        if (!state.isInsideComponent())
            throw new RuntimeException("Block parameters are only allowed directly within component elements.");
    }

    /**
     * Tapestry 5.1 uses a special namespace (usually mapped to "p:") and the name becomes the parameter element.
     */
    private void parameterElement(TemplateParserState state) throws XMLStreamException
    {
        ensureParameterWithinComponent(state);

        if (reader.getAttributeCount() > 0)
            throw new TapestryException(ServicesMessages.parameterElementDoesNotAllowAttributes(), getLocation(),
                                        null);

        tokenAccumulator.add(new ParameterToken(reader.getLocalName(), getLocation()));

        processBody(state.insideComponent(false));
    }


    /**
     * Checks that a body element is empty. Returns after the body's close element. Adds a single body token (but not an
     * end token).
     */
    private void body() throws XMLStreamException
    {
        tokenAccumulator.add(new BodyToken(getLocation()));

        while (active)
        {
            switch (reader.next())
            {
                case END_ELEMENT:
                    return;

                default:
                    throw new IllegalStateException(ServicesMessages.contentInsideBodyNotAllowed(getLocation()));
            }
        }
    }

    /**
     * Driven by the &lt;t:container&gt; element, this state adds elements for its body but not its start or end tags.
     *
     * @param state
     * @throws XMLStreamException
     */
    private void container(TemplateParserState state) throws XMLStreamException
    {
        while (active)
        {
            switch (reader.next())
            {
                case START_ELEMENT:
                    element(state);
                    break;

                // The matching end-element for the container. Don't add a token.

                case END_ELEMENT:

                    processTextBuffer(state);

                    return;

                default:
                    textContent(state);
            }
        }
    }

    /**
     * A block adds a token for its start tag and end tag and allows any content within.
     */
    private void block(TemplateParserState state) throws XMLStreamException
    {
        String blockId = getSingleParameter("id");

        validateId(blockId, "invalid-block-id");

        tokenAccumulator.add(new BlockToken(blockId, getLocation()));

        processBody(state.insideComponent(false));
    }

    private String getSingleParameter(String attributeName)
    {
        String result = null;

        for (int i = 0; i < reader.getAttributeCount(); i++)
        {
            QName qName = reader.getAttributeName(i);

            if (isXMLSpaceAttribute(qName)) continue;

            if (qName.getLocalPart().equalsIgnoreCase(attributeName))
            {
                result = reader.getAttributeValue(i);
                continue;
            }

            // Only the named attribute is allowed.

            throw new TapestryException(ServicesMessages.undefinedTapestryAttribute(reader.getLocalName(),
                                                                                    qName.toString(), attributeName),
                                        getLocation(), null);
        }

        return result;
    }

    private void validateId(String id, String messageKey)
    {
        if (id == null) return;

        if (ID_PATTERN.matcher(id).matches()) return;

        // Not a match.

        throw new TapestryException(ServicesMessages.invalidId(messageKey, id), getLocation(), null);
    }

    private boolean isXMLSpaceAttribute(QName qName)
    {
        return XML_NAMESPACE_URI.equals(qName.getNamespaceURI()) &&
                "space".equals(qName.getLocalPart());
    }


    /**
     * Processes text content if in the correct state, or throws an exception. This is used as a default for matching
     * case statements.
     *
     * @param state
     */
    private void textContent(TemplateParserState state)
    {
        switch (reader.getEventType())
        {
            case COMMENT:
                comment(state);
                break;

            case CDATA:
                cdata(state);
                break;

            case CHARACTERS:
            case SPACE:
                characters();
                break;

            default:
                unexpectedEventType();
        }
    }

    private void characters()
    {
        if (textStartLocation == null)
            textStartLocation = getLocation();

        textBuffer.append(reader.getText());
    }

    private void cdata(TemplateParserState state)
    {
        processTextBuffer(state);

        tokenAccumulator.add(new CDATAToken(reader.getText(), getLocation()));
    }

    private void comment(TemplateParserState state)
    {
        processTextBuffer(state);

        // Trim the excess whitespace; the Comment DOM node will add a leading/trailing space.

        String comment = reader.getText().trim();

        tokenAccumulator.add(new CommentToken(comment, getLocation()));
    }

    /**
     * Processes the accumulated text in the text buffer as a text token.
     */
    private void processTextBuffer(TemplateParserState state)
    {
        if (textBuffer.length() != 0)
            convertTextBufferToTokens(state);

        textStartLocation = null;
    }

    private void convertTextBufferToTokens(TemplateParserState state)
    {
        String text = textBuffer.toString();

        textBuffer.setLength(0);

        if (state.isCompressWhitespace())
        {
            text = compressWhitespaceInText(text);

            if (InternalUtils.isBlank(text)) return;
        }

        addTokensForText(text);
    }

    /**
     * Reduces vertical whitespace to a single newline, then reduces horizontal whitespace to a single space.
     *
     * @param text
     * @return compressed version of text
     */
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

                tokenAccumulator.add(new TextToken(prefix, textStartLocation));
            }

            // Group 1 includes the real text of the expansion, with whitespace around the
            // expression (but inside the curly braces) excluded.

            String expression = matcher.group(1);

            tokenAccumulator.add(new ExpansionToken(expression, textStartLocation));

            startx = matcher.end();
        }

        // Catch anything after the final regexp match.

        if (startx < text.length())
            tokenAccumulator.add(new TextToken(text.substring(startx, text.length()), textStartLocation));
    }

}
