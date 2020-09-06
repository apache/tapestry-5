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
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.internal.parser.*;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.tapestry5.internal.services.SaxTemplateParser.Version.*;

/**
 * SAX-based template parser logic, taking a {@link Resource} to a Tapestry
 * template file and returning
 * a {@link ComponentTemplate}.
 *
 * Earlier versions of this code used the StAX (streaming XML parser), but that
 * was really, really bad for Google App Engine. This version uses SAX under the
 * covers, but kind of replicates the important bits of the StAX API as
 * {@link XMLTokenStream}.
 *
 * @since 5.2.0
 */
@SuppressWarnings(
        {"JavaDoc"})
public class SaxTemplateParser
{
    private static final String MIXINS_ATTRIBUTE_NAME = "mixins";

    private static final String TYPE_ATTRIBUTE_NAME = "type";

    private static final String ID_ATTRIBUTE_NAME = "id";

    public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

    private static final Map<String, Version> NAMESPACE_URI_TO_VERSION = CollectionFactory.newMap();

    {
        NAMESPACE_URI_TO_VERSION.put("http://tapestry.apache.org/schema/tapestry_5_0_0.xsd", T_5_0);
        NAMESPACE_URI_TO_VERSION.put("http://tapestry.apache.org/schema/tapestry_5_1_0.xsd", T_5_1);
        // 5.2 didn't change the schmea, so the 5_1_0.xsd was still used.
        // 5.3 fixes an incorrect element name in the XSD ("replacement" should be "replace")
        // The parser code here always expected "replace".
        NAMESPACE_URI_TO_VERSION.put("http://tapestry.apache.org/schema/tapestry_5_3.xsd", T_5_3);
        // 5.4 is pretty much the same as 5.3, but allows block inside extend
        // as per TAP5-1847
        NAMESPACE_URI_TO_VERSION.put("http://tapestry.apache.org/schema/tapestry_5_4.xsd", T_5_4);
    }

    /**
     * Special namespace used to denote Block parameters to components, as a
     * (preferred) alternative to the t:parameter
     * element. The simple element name is the name of the parameter.
     */
    private static final String TAPESTRY_PARAMETERS_URI = "tapestry:parameter";

    /**
     * URI prefix used to identify a Tapestry library, the remainder of the URI
     * becomes a prefix on the element name.
     */
    private static final String LIB_NAMESPACE_URI_PREFIX = "tapestry-library:";

    /**
     * Pattern used to parse the path portion of the library namespace URI. A
     * series of simple identifiers with slashes
     * allowed as seperators.
     */

    private static final Pattern LIBRARY_PATH_PATTERN = Pattern.compile("^[a-z]\\w*(/[a-z]\\w*)*$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-z]\\w*$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Any amount of mixed simple whitespace (space, tab, form feed) mixed with
     * at least one carriage return or line
     * feed, followed by any amount of whitespace. Will be reduced to a single
     * linefeed.
     */
    private static final Pattern REDUCE_LINEBREAKS_PATTERN = Pattern.compile(
            "[ \\t\\f]*[\\r\\n]\\s*", Pattern.MULTILINE);

    /**
     * Used when compressing whitespace, matches any sequence of simple
     * whitespace (space, tab, formfeed). Applied after
     * REDUCE_LINEBREAKS_PATTERN.
     */
    private static final Pattern REDUCE_WHITESPACE_PATTERN = Pattern.compile("[ \\t\\f]+",
            Pattern.MULTILINE);

    // Note the use of the non-greedy modifier; this prevents the pattern from
    // merging multiple
    // expansions on the same text line into a single large
    // but invalid expansion.

    private static final Pattern EXPANSION_PATTERN = Pattern.compile("\\$\\{\\s*(((?!\\$\\{).)*)\\s*}");
    private static final char EXPANSION_STRING_DELIMITTER = '\'';
    private static final char OPEN_BRACE = '{';
    private static final char CLOSE_BRACE = '}';

    private static final Set<String> MUST_BE_ROOT = CollectionFactory.newSet("extend", "container");

    private final Resource resource;

    private final XMLTokenStream tokenStream;

    private final StringBuilder textBuffer = new StringBuilder();

    private final List<TemplateToken> tokens = CollectionFactory.newList();

    // This starts pointing at tokens but occasionally shifts to a list inside
    // the overrides Map.
    private List<TemplateToken> tokenAccumulator = tokens;

    /**
     * Primarily used as a set of componentIds (to check for duplicates and
     * conflicts).
     */
    private final Map<String, Location> componentIds = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Map from override id to a list of tokens; this actually works both for
     * overrides defined by this template and
     * overrides provided by this template.
     */
    private Map<String, List<TemplateToken>> overrides;

    private boolean extension;

    private Location textStartLocation;

    private boolean active = true;

    private boolean strictMixinParameters = false;

    private final Map<String, Boolean> extensionPointIdSet = CollectionFactory.newCaseInsensitiveMap();

    public SaxTemplateParser(Resource resource, Map<String, URL> publicIdToURL)
    {
        this.resource = resource;
        this.tokenStream = new XMLTokenStream(resource, publicIdToURL);
    }

    public ComponentTemplate parse(boolean compressWhitespace)
    {
        try
        {
            tokenStream.parse();

            TemplateParserState initialParserState = new TemplateParserState()
                    .compressWhitespace(compressWhitespace);

            root(initialParserState);

            return new ComponentTemplateImpl(resource, tokens, componentIds, extension, strictMixinParameters, overrides);
        } catch (Exception ex)
        {
            throw new TapestryException(String.format("Failure parsing template %s: %s", resource,
                    ExceptionUtils.toMessage(ex)), tokenStream.getLocation(), ex);
        }

    }

    void root(TemplateParserState state)
    {
        while (active && tokenStream.hasNext())
        {
            switch (tokenStream.next())
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

    private void rootElement(TemplateParserState initialState)
    {
        TemplateParserState state = setupForElement(initialState);

        String uri = tokenStream.getNamespaceURI();
        String name = tokenStream.getLocalName();
        Version version = NAMESPACE_URI_TO_VERSION.get(uri);

        if (T_5_1.sameOrEarlier(version))
        {
            if (name.equalsIgnoreCase("extend"))
            {
                extend(state);
                return;
            }
        }

        if (version != null)
        {
            if (name.equalsIgnoreCase("container"))
            {
                container(state);
                return;
            }
        }

        element(state);
    }

    private void extend(TemplateParserState state)
    {
        extension = true;

        while (active)
        {
            switch (tokenStream.next())
            {
                case START_ELEMENT:

                    if (isTemplateVersion(Version.T_5_1) && isElementName("replace"))
                    {
                        replace(state);
                        break;
                    }

                    boolean is54 = isTemplateVersion(Version.T_5_4);

                    if (is54 && isElementName("block"))
                    {
                        block(state);
                        break;
                    }

                    throw new RuntimeException(
                            is54
                                    ? "Child element of <extend> must be <replace> or <block>."
                                    : "Child element of <extend> must be <replace>.");

                case END_ELEMENT:

                    return;

                // Ignore spaces and comments directly inside <extend>.

                case COMMENT:
                case SPACE:
                    break;

                // Other non-whitespace content (characters, etc.) are forbidden.

                case CHARACTERS:
                    if (InternalUtils.isBlank(tokenStream.getText()))
                        break;

                default:
                    unexpectedEventType();
            }
        }
    }

    /**
     * Returns true if the <em>local name</em> is the element name (ignoring case).
     */
    private boolean isElementName(String elementName)
    {
        return tokenStream.getLocalName().equalsIgnoreCase(elementName);
    }

    /**
     * Returns true if the template version is at least the required version.
     */
    private boolean isTemplateVersion(Version requiredVersion)
    {
        Version templateVersion = NAMESPACE_URI_TO_VERSION.get(tokenStream.getNamespaceURI());

        return requiredVersion.sameOrEarlier(templateVersion);
    }

    private void replace(TemplateParserState state)
    {
        String id = getRequiredIdAttribute();

        addContentToOverride(setupForElement(state), id);
    }

    private void unexpectedEventType()
    {
        XMLTokenType eventType = tokenStream.getEventType();

        throw new IllegalStateException(String.format("Unexpected XML parse event %s.", eventType
                .name()));
    }

    private void dtd()
    {
        DTDData dtdInfo = tokenStream.getDTDInfo();

        tokenAccumulator.add(new DTDToken(dtdInfo.rootName, dtdInfo.publicId, dtdInfo
                .systemId, getLocation()));
    }

    private Location getLocation()
    {
        return tokenStream.getLocation();
    }

    /**
     * Processes an element through to its matching end tag.
     *
     * An element can be:
     *
     * a Tapestry component via &lt;t:type&gt;
     *
     * a Tapestry component via t:type="type" and/or t:id="id"
     *
     * a Tapestry component via a library namespace
     *
     * A parameter element via &lt;t:parameter&gt;
     *
     * A parameter element via &lt;p:name&gt;
     *
     * A &lt;t:remove&gt; element (in the 5.1 schema)
     *
     * A &lt;t:content&gt; element (in the 5.1 schema)
     *
     * A &lt;t:block&gt; element
     *
     * The body &lt;t:body&gt;
     *
     * An ordinary element
     */
    void element(TemplateParserState initialState)
    {
        TemplateParserState state = setupForElement(initialState);

        String uri = tokenStream.getNamespaceURI();
        String name = tokenStream.getLocalName();
        Version version = NAMESPACE_URI_TO_VERSION.get(uri);

        if (T_5_1.sameOrEarlier(version))
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
                throw new RuntimeException(
                        "The <replace> element may only appear directly within an extend element.");
            }

            if (MUST_BE_ROOT.contains(name))
                mustBeRoot(name);
        }

        if (version != null)
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
                if (T_5_3.sameOrEarlier(version))
                {
                    throw new RuntimeException(
                            String.format("The <parameter> element has been deprecated in Tapestry 5.3 in favour of '%s' namespace.", TAPESTRY_PARAMETERS_URI));
                }

                classicParameter(state);

                return;
            }

            possibleTapestryComponent(state, null, tokenStream.getLocalName().replace('.', '/'));

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

        possibleTapestryComponent(state, tokenStream.getLocalName(), null);
    }

    /**
     * Processes a body of an element including text and (recursively) nested
     * elements. Adds an
     * {@link org.apache.tapestry5.internal.parser.TokenType#END_ELEMENT} token
     * before returning.
     *
     * @param state
     */
    private void processBody(TemplateParserState state)
    {
        while (active)
        {
            switch (tokenStream.next())
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
     * Handles an extension point, putting a RenderExtension token in position
     * in the template.
     *
     * @param state
     */
    private void extensionPoint(TemplateParserState state)
    {
        // An extension point adds a token that represents where the override
        // (either the default
        // provided in the parent template, or the true override from a child
        // template) is positioned.

        String id = getRequiredIdAttribute();

        if (extensionPointIdSet.containsKey(id))
        {
            throw new TapestryException(String.format("Extension point '%s' is already defined for this template. Extension point ids must be unique.", id), getLocation(), null);
        } else
        {
            extensionPointIdSet.put(id, true);
        }

        tokenAccumulator.add(new ExtensionPointToken(id, getLocation()));

        addContentToOverride(state.insideComponent(false), id);
    }

    private String getRequiredIdAttribute()
    {
        String id = getSingleParameter("id");

        if (InternalUtils.isBlank(id))
            throw new RuntimeException(String.format("The <%s> element must have an id attribute.",
                    tokenStream.getLocalName()));

        return id;
    }

    private void addContentToOverride(TemplateParserState state, String id)

    {
        List<TemplateToken> savedTokenAccumulator = tokenAccumulator;

        tokenAccumulator = CollectionFactory.newList();

        // TODO: id should probably be unique; i.e., you either define an
        // override or you
        // provide an override, but you don't do both in the same template.

        if (overrides == null)
            overrides = CollectionFactory.newCaseInsensitiveMap();

        overrides.put(id, tokenAccumulator);

        while (active)
        {
            switch (tokenStream.next())
            {
                case START_ELEMENT:
                    element(state);
                    break;

                case END_ELEMENT:

                    processTextBuffer(state);

                    // Restore everthing to how it was before the
                    // extention-point was reached.

                    tokenAccumulator = savedTokenAccumulator;
                    return;

                default:
                    textContent(state);
            }
        }
    }

    private void mustBeRoot(String name)
    {
        throw new RuntimeException(String.format(
                "Element <%s> is only valid as the root element of a template.", name));
    }

    /**
     * Triggered by &lt;t:content&gt; element; limits template content to just
     * what's inside.
     */

    private void limitContent(TemplateParserState state)
    {
        if (state.isCollectingContent())
            throw new IllegalStateException(
                    "The <content> element may not be nested within another <content> element.");

        TemplateParserState newState = state.collectingContent().insideComponent(false);

        // Clear out any tokens that precede the <t:content> element

        tokens.clear();

        // I'm not happy about this; you really shouldn't define overrides just
        // to clear them out,
        // but it is consistent. Perhaps this should be an error if overrides is
        // non-empty.

        overrides = null;

        // Make sure that if the <t:content> appears inside a <t:replace> or
        // <t:extension-point>, that
        // it is still handled correctly.

        tokenAccumulator = tokens;

        while (active)
        {
            switch (tokenStream.next())
            {
                case START_ELEMENT:
                    element(newState);
                    break;

                case END_ELEMENT:

                    // The active flag is global, once we hit it, the entire
                    // parse is aborted, leaving
                    // tokens with just tokens defined inside <t:content>.

                    processTextBuffer(newState);

                    active = false;

                    break;

                default:
                    textContent(state);
            }
        }

    }

    private void removeContent()
    {
        int depth = 1;

        while (active)
        {
            switch (tokenStream.next())
            {
                case START_ELEMENT:
                    depth++;
                    break;

                // The matching end element.

                case END_ELEMENT:
                    depth--;

                    if (depth == 0)
                        return;

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
    private void libraryNamespaceComponent(TemplateParserState state)
    {
        String uri = tokenStream.getNamespaceURI();

        // The library path is encoded into the namespace URI.

        String path = uri.substring(LIB_NAMESPACE_URI_PREFIX.length());

        if (!LIBRARY_PATH_PATTERN.matcher(path).matches())
            throw new RuntimeException(String.format("The path portion of library namespace URI '%s' is not valid: it must be a simple identifier, or a series of identifiers seperated by slashes.", uri));

        possibleTapestryComponent(state, null, path + "/" + tokenStream.getLocalName());
    }

    /**
     * @param elementName
     * @param identifiedType
     *         the type of the element, usually null, but may be the
     *         component type derived from element
     */
    private void possibleTapestryComponent(TemplateParserState state, String elementName,
                                           String identifiedType)
    {
        String id = null;
        String type = identifiedType;
        String mixins = null;

        int count = tokenStream.getAttributeCount();

        Location location = getLocation();

        List<TemplateToken> attributeTokens = CollectionFactory.newList();

        for (int i = 0; i < count; i++)
        {
            QName qname = tokenStream.getAttributeName(i);

            if (isXMLSpaceAttribute(qname))
                continue;

            // The name will be blank for an xmlns: attribute

            String localName = qname.getLocalPart();

            if (InternalUtils.isBlank(localName))
                continue;

            String uri = qname.getNamespaceURI();

            String value = tokenStream.getAttributeValue(i);


            Version version = NAMESPACE_URI_TO_VERSION.get(uri);

            if (version != null)
            {
                // We are kind of assuming that the namespace URI appears once, in the outermost element of the template.
                // And we don't and can't handle the case that it appears multiple times in the template.

                if (T_5_4.sameOrEarlier(version)) {
                    strictMixinParameters = true;
                }

                if (localName.equalsIgnoreCase(ID_ATTRIBUTE_NAME))
                {
                    id = nullForBlank(value);

                    validateId(id, "Component id '%s' is not valid; component ids must be valid Java identifiers: start with a letter, and consist of letters, numbers and underscores.");

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

                // Anything else is the name of a Tapestry component parameter
                // that is simply
                // not part of the template's doctype for the element being
                // instrumented.
            }

            attributeTokens.add(new AttributeToken(uri, localName, value, location));
        }

        boolean isComponent = (id != null || type != null);

        // If provided t:mixins but not t:id or t:type, then its not quite a
        // component

        if (mixins != null && !isComponent)
            throw new TapestryException(String.format("You may not specify mixins for element <%s> because it does not represent a component (which requires either an id attribute or a type attribute).", elementName),
                    location, null);

        if (isComponent)
        {
            tokenAccumulator.add(new StartComponentToken(elementName, id, type, mixins, location));
        } else
        {
            tokenAccumulator.add(new StartElementToken(tokenStream.getNamespaceURI(), elementName,
                    location));
        }

        addDefineNamespaceTokens();

        tokenAccumulator.addAll(attributeTokens);

        if (id != null)
            componentIds.put(id, location);

        processBody(state.insideComponent(isComponent));
    }

    private void addDefineNamespaceTokens()
    {
        for (int i = 0; i < tokenStream.getNamespaceCount(); i++)
        {
            String uri = tokenStream.getNamespaceURI(i);

            // These URIs are strictly part of the server-side Tapestry template
            // and are not ever sent to the client.

            if (NAMESPACE_URI_TO_VERSION.containsKey(uri))
                continue;

            if (uri.equals(TAPESTRY_PARAMETERS_URI))
                continue;

            if (uri.startsWith(LIB_NAMESPACE_URI_PREFIX))
                continue;

            tokenAccumulator.add(new DefineNamespacePrefixToken(uri, tokenStream
                    .getNamespacePrefix(i), getLocation()));
        }
    }

    private TemplateParserState checkForXMLSpaceAttribute(TemplateParserState state)
    {
        for (int i = 0; i < tokenStream.getAttributeCount(); i++)
        {
            QName qName = tokenStream.getAttributeName(i);

            if (isXMLSpaceAttribute(qName))
            {
                boolean compress = !"preserve".equals(tokenStream.getAttributeValue(i));

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
     * Handler for Tapestry 5.0's "classic" &lt;t:parameter&gt; element. This
     * turns into a {@link org.apache.tapestry5.internal.parser.ParameterToken}
     * and the body and end element are provided normally.
     */
    private void classicParameter(TemplateParserState state)
    {
        String parameterName = getSingleParameter("name");

        if (InternalUtils.isBlank(parameterName))
            throw new TapestryException("The name attribute of the <parameter> element must be specified.",
                    getLocation(), null);

        ensureParameterWithinComponent(state);

        tokenAccumulator.add(new ParameterToken(parameterName, getLocation()));

        processBody(state.insideComponent(false));
    }

    private void ensureParameterWithinComponent(TemplateParserState state)
    {
        if (!state.isInsideComponent())
            throw new RuntimeException(
                    "Block parameters are only allowed directly within component elements.");
    }

    /**
     * Tapestry 5.1 uses a special namespace (usually mapped to "p:") and the
     * name becomes the parameter element.
     */
    private void parameterElement(TemplateParserState state)
    {
        ensureParameterWithinComponent(state);

        if (tokenStream.getAttributeCount() > 0)
            throw new TapestryException("A block parameter element does not allow any additional attributes. The element name defines the parameter name.",
                    getLocation(), null);

        tokenAccumulator.add(new ParameterToken(tokenStream.getLocalName(), getLocation()));

        processBody(state.insideComponent(false));
    }

    /**
     * Checks that a body element is empty. Returns after the body's close
     * element. Adds a single body token (but not an
     * end token).
     */
    private void body()
    {
        tokenAccumulator.add(new BodyToken(getLocation()));

        while (active)
        {
            switch (tokenStream.next())
            {
                case END_ELEMENT:
                    return;

                default:
                    throw new IllegalStateException(String.format("Content inside a Tapestry body element is not allowed (at %s). The content has been ignored.", getLocation()));
            }
        }
    }

    /**
     * Driven by the &lt;t:container&gt; element, this state adds elements for
     * its body but not its start or end tags.
     *
     * @param state
     */
    private void container(TemplateParserState state)
    {
        while (active)
        {
            switch (tokenStream.next())
            {
                case START_ELEMENT:
                    element(state);
                    break;

                // The matching end-element for the container. Don't add a
                // token.

                case END_ELEMENT:

                    processTextBuffer(state);

                    return;

                default:
                    textContent(state);
            }
        }
    }

    /**
     * A block adds a token for its start tag and end tag and allows any content
     * within.
     */
    private void block(TemplateParserState state)
    {
        String blockId = getSingleParameter("id");

        validateId(blockId, "Block id '%s' is not valid; block ids must be valid Java identifiers: start with a letter, and consist of letters, numbers and underscores.");

        tokenAccumulator.add(new BlockToken(blockId, getLocation()));

        processBody(state.insideComponent(false));
    }

    private String getSingleParameter(String attributeName)
    {
        String result = null;

        for (int i = 0; i < tokenStream.getAttributeCount(); i++)
        {
            QName qName = tokenStream.getAttributeName(i);

            if (isXMLSpaceAttribute(qName))
                continue;

            if (qName.getLocalPart().equalsIgnoreCase(attributeName))
            {
                result = tokenStream.getAttributeValue(i);
                continue;
            }

            // Only the named attribute is allowed.

            throw new TapestryException(String.format("Element <%s> does not support an attribute named '%s'. The only allowed attribute name is '%s'.", tokenStream
                    .getLocalName(), qName.toString(), attributeName), getLocation(), null);
        }

        return result;
    }

    private void validateId(String id, String messageKey)
    {
        if (id == null)
            return;

        if (ID_PATTERN.matcher(id).matches())
            return;

        // Not a match.

        throw new TapestryException(String.format(messageKey, id), getLocation(), null);
    }

    private boolean isXMLSpaceAttribute(QName qName)
    {
        return XML_NAMESPACE_URI.equals(qName.getNamespaceURI())
                && "space".equals(qName.getLocalPart());
    }

    /**
     * Processes text content if in the correct state, or throws an exception.
     * This is used as a default for matching
     * case statements.
     *
     * @param state
     */
    private void textContent(TemplateParserState state)
    {
        switch (tokenStream.getEventType())
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

        textBuffer.append(tokenStream.getText());
    }

    private void cdata(TemplateParserState state)
    {
        processTextBuffer(state);

        tokenAccumulator.add(new CDATAToken(tokenStream.getText(), getLocation()));
    }

    private void comment(TemplateParserState state)
    {
        processTextBuffer(state);

        String comment = tokenStream.getText();

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

            if (InternalUtils.isBlank(text))
                return;
        }

        addTokensForText(text);
    }

    /**
     * Reduces vertical whitespace to a single newline, then reduces horizontal
     * whitespace to a single space.
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
     * Scans the text, using a regular expression pattern, for expansion
     * patterns, and adds appropriate tokens for what
     * it finds.
     *
     * @param text
     *         to add as
     *         {@link org.apache.tapestry5.internal.parser.TextToken}s and
     *         {@link org.apache.tapestry5.internal.parser.ExpansionToken}s
     */
    private void addTokensForText(String text)
    {
        Matcher matcher = EXPANSION_PATTERN.matcher(text);

        int startx = 0;

        // The big problem with all this code is that everything gets assigned
        // to the
        // start of the text block, even if there are line breaks leading up to
        // it.
        // That's going to take a lot more work and there are bigger fish to
        // fry. In addition,
        // TAPESTRY-2028 means that the whitespace has likely been stripped out
        // of the text
        // already anyway.
        while (matcher.find())
        {
            int matchStart = matcher.start();

            if (matchStart != startx)
            {
                String prefix = text.substring(startx, matchStart);
                tokenAccumulator.add(new TextToken(prefix, textStartLocation));
            }

            // Group 1 includes the real text of the expansion, with whitespace
            // around the
            // expression (but inside the curly braces) excluded.
            // But note that we run into a problem.  The original 
            // EXPANSION_PATTERN used a reluctant quantifier to match the 
            // smallest instance of ${} possible.  But if you have ${'}'} or 
            // ${{'key': 'value'}} (maps, cf TAP5-1605) then you run into issues
            // b/c the expansion becomes {'key': 'value' which is wrong.
            // A fix to use greedy matching with negative lookahead to prevent 
            // ${...}...${...} all matching a single expansion is close, but 
            // has issues when an expansion is used inside a javascript function
            // (see TAP5-1620). The solution is to use the greedy 
            // EXPANSION_PATTERN as before to bound the search for a single 
            // expansion, then check for {} consistency, ignoring opening and 
            // closing braces that occur within '' (the property expression 
            // language doesn't support "" for strings). That should include: 
            // 'This string has a } in it' and 'This string has a { in it.'
            // Note also that the property expression language doesn't support
            // escaping the string character ('), so we don't have to worry 
            // about that. 
            String expression = matcher.group(1);
            //count of 'open' braces. Expression ends when it hits 0. In most cases,
            // it should end up as 1 b/c "expression" is everything inside ${}, so 
            // the following will typically not find the end of the expression.
            int openBraceCount = 1;
            int expressionEnd = expression.length();
            boolean inQuote = false;
            for (int i = 0; i < expression.length(); i++)
            {
                char c = expression.charAt(i);
                //basically, if we're inQuote, we ignore everything until we hit the quote end, so we only care if the character matches the quote start (meaning we're at the end of the quote).
                //note that I don't believe expression support escaped quotes...
                if (c == EXPANSION_STRING_DELIMITTER)
                {
                    inQuote = !inQuote;
                    continue;
                } else if (inQuote)
                {
                    continue;
                } else if (c == CLOSE_BRACE)
                {
                    openBraceCount--;
                    if (openBraceCount == 0)
                    {
                        expressionEnd = i;
                        break;
                    }
                } else if (c == OPEN_BRACE)
                {
                    openBraceCount++;
                }
            }
            if (expressionEnd < expression.length())
            {
                //then we gobbled up some } that we shouldn't have... like the closing } of a javascript
                //function.
                tokenAccumulator.add(new ExpansionToken(expression.substring(0, expressionEnd), textStartLocation));
                //can't just assign to 
                startx = matcher.start(1) + expressionEnd + 1;
            } else
            {
                tokenAccumulator.add(new ExpansionToken(expression.trim(), textStartLocation));

                startx = matcher.end();
            }
        }

        // Catch anything after the final regexp match.

        if (startx < text.length())
            tokenAccumulator.add(new TextToken(text.substring(startx, text.length()),
                    textStartLocation));
    }

    static enum Version
    {
        T_5_0(5, 0), T_5_1(5, 1), T_5_3(5, 3), T_5_4(5, 4);

        private int major;
        private int minor;


        private Version(int major, int minor)
        {
            this.major = major;
            this.minor = minor;
        }

        /**
         * Returns true if this Version is the same as, or ordered before the other Version. This is often used to enable new
         * template features for a specific version.
         */
        public boolean sameOrEarlier(Version other)
        {
            if (other == null)
                return false;

            if (this == other)
                return true;

            return major <= other.major && minor <= other.minor;
        }
    }

}
