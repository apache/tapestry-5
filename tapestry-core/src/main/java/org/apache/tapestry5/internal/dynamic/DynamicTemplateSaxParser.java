// Copyright 2011-2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.dynamic;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.internal.services.XMLTokenStream;
import org.apache.tapestry5.internal.services.XMLTokenType;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.dynamic.DynamicDelegate;
import org.apache.tapestry5.services.dynamic.DynamicTemplate;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Does the heavy lifting for {@link DynamicTemplateParserImpl}.
 */
public class DynamicTemplateSaxParser
{
    private final Resource resource;

    private final BindingSource bindingSource;

    private final XMLTokenStream tokenStream;

    private static final Pattern PARAM_ID_PATTERN = Pattern.compile("^param:(\\p{Alpha}\\w*)$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern EXPANSION_PATTERN = Pattern.compile("\\$\\{\\s*(.*?)\\s*}");

    private static final DynamicTemplateElement END = new DynamicTemplateElement()
    {
        public void render(MarkupWriter writer, RenderQueue queue, DynamicDelegate delegate)
        {
            // End the previously started element
            writer.end();
        }
    };

    public DynamicTemplateSaxParser(Resource resource, BindingSource bindingSource, Map<String, URL> publicIdToURL)
    {
        this.resource = resource;
        this.bindingSource = bindingSource;

        this.tokenStream = new XMLTokenStream(resource, publicIdToURL);
    }

    public DynamicTemplate parse()
    {
        try
        {
            tokenStream.parse();

            return toDynamicTemplate(root());
        } catch (Exception ex)
        {
            throw new TapestryException(String.format("Failure parsing dynamic template %s: %s", resource,
                    ExceptionUtils.toMessage(ex)), tokenStream.getLocation(), ex);
        }
    }

    // Note the use of static methods; otherwise the compiler sets this$0 to point to the DynamicTemplateSaxParser,
    // creating an unwanted reference that keeps the parser from being GCed.

    private static DynamicTemplate toDynamicTemplate(List<DynamicTemplateElement> elements)
    {
        final Flow<DynamicTemplateElement> flow = F.flow(elements).reverse();

        return new DynamicTemplate()
        {
            public RenderCommand createRenderCommand(final DynamicDelegate delegate)
            {
                final Mapper<DynamicTemplateElement, RenderCommand> toRenderCommand = createToRenderCommandMapper(delegate);

                return new RenderCommand()
                {
                    public void render(MarkupWriter writer, RenderQueue queue)
                    {
                        Worker<RenderCommand> pushOnQueue = createQueueRenderCommand(queue);

                        flow.map(toRenderCommand).each(pushOnQueue);
                    }
                };
            }
        };
    }

    private List<DynamicTemplateElement> root()
    {
        List<DynamicTemplateElement> result = CollectionFactory.newList();

        while (tokenStream.hasNext())
        {
            switch (tokenStream.next())
            {
                case START_ELEMENT:
                    result.add(element());
                    break;

                case END_DOCUMENT:
                    // Ignore it.
                    break;

                default:
                    addTextContent(result);
            }
        }

        return result;
    }

    private DynamicTemplateElement element()
    {
        String elementURI = tokenStream.getNamespaceURI();
        String elementName = tokenStream.getLocalName();

        String blockId = null;

        int count = tokenStream.getAttributeCount();

        List<DynamicTemplateAttribute> attributes = CollectionFactory.newList();

        Location location = getLocation();

        for (int i = 0; i < count; i++)
        {
            QName qname = tokenStream.getAttributeName(i);

            // The name will be blank for an xmlns: attribute

            String localName = qname.getLocalPart();

            if (InternalUtils.isBlank(localName))
                continue;

            String uri = qname.getNamespaceURI();

            String value = tokenStream.getAttributeValue(i);

            if (localName.equals("id"))
            {
                Matcher matcher = PARAM_ID_PATTERN.matcher(value);

                if (matcher.matches())
                {
                    blockId = matcher.group(1);
                    continue;
                }
            }

            Mapper<DynamicDelegate, String> attributeValueExtractor = createCompositeExtractorFromText(value, location);

            attributes.add(new DynamicTemplateAttribute(uri, localName, attributeValueExtractor));
        }

        if (blockId != null)
            return block(blockId);

        List<DynamicTemplateElement> body = CollectionFactory.newList();

        boolean atEnd = false;
        while (!atEnd)
        {
            switch (tokenStream.next())
            {
                case START_ELEMENT:

                    // Recurse into this new element
                    body.add(element());

                    break;

                case END_ELEMENT:
                    body.add(END);
                    atEnd = true;

                    break;

                default:

                    addTextContent(body);
            }
        }

        return createElementWriterElement(elementURI, elementName, attributes, body);
    }

    private static DynamicTemplateElement createElementWriterElement(final String elementURI, final String elementName,
                                                                     final List<DynamicTemplateAttribute> attributes, List<DynamicTemplateElement> body)
    {
        final Flow<DynamicTemplateElement> bodyFlow = F.flow(body).reverse();

        return new DynamicTemplateElement()
        {
            public void render(MarkupWriter writer, RenderQueue queue, DynamicDelegate delegate)
            {
                // Write the element ...

                writer.elementNS(elementURI, elementName);

                // ... and the attributes

                for (DynamicTemplateAttribute attribute : attributes)
                {
                    attribute.write(writer, delegate);
                }

                // And convert the DTEs for the direct children of this element into RenderCommands and push them onto
                // the queue. This includes the child that will end the started element.

                Mapper<DynamicTemplateElement, RenderCommand> toRenderCommand = createToRenderCommandMapper(delegate);
                Worker<RenderCommand> pushOnQueue = createQueueRenderCommand(queue);

                bodyFlow.map(toRenderCommand).each(pushOnQueue);
            }
        };
    }

    private DynamicTemplateElement block(final String blockId)
    {
        Location location = getLocation();

        removeContent();

        return createBlockElement(blockId, location);
    }

    private static DynamicTemplateElement createBlockElement(final String blockId, final Location location)
    {
        return new DynamicTemplateElement()
        {
            public void render(MarkupWriter writer, RenderQueue queue, DynamicDelegate delegate)
            {
                try
                {
                    Block block = delegate.getBlock(blockId);

                    queue.push((RenderCommand) block);
                } catch (Exception ex)
                {
                    throw new TapestryException(String.format(
                            "Exception rendering block '%s' as part of dynamic template: %s", blockId,
                            ExceptionUtils.toMessage(ex)), location, ex);
                }
            }
        };
    }

    private Location getLocation()
    {
        return tokenStream.getLocation();
    }

    private void removeContent()
    {
        int depth = 1;

        while (true)
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

    void addTextContent(List<DynamicTemplateElement> elements)
    {
        switch (tokenStream.getEventType())
        {
            case COMMENT:
                elements.add(comment());
                break;

            case CHARACTERS:
            case SPACE:
                addTokensForText(elements);
                break;

            default:
                unexpectedEventType();
        }
    }

    private void addTokensForText(List<DynamicTemplateElement> elements)
    {
        Mapper<DynamicDelegate, String> composite = createCompositeExtractorFromText(tokenStream.getText(),
                tokenStream.getLocation());

        elements.add(createTextWriterElement(composite));
    }

    private static DynamicTemplateElement createTextWriterElement(final Mapper<DynamicDelegate, String> composite)
    {
        return new DynamicTemplateElement()
        {
            public void render(MarkupWriter writer, RenderQueue queue, DynamicDelegate delegate)
            {
                String value = composite.map(delegate);

                writer.write(value);
            }
        };
    }

    private Mapper<DynamicDelegate, String> createCompositeExtractorFromText(String text, Location location)
    {
        Matcher matcher = EXPANSION_PATTERN.matcher(text);

        List<Mapper<DynamicDelegate, String>> extractors = CollectionFactory.newList();

        int startx = 0;

        while (matcher.find())
        {
            int matchStart = matcher.start();

            if (matchStart != startx)
            {
                String prefix = text.substring(startx, matchStart);

                extractors.add(createTextExtractor(prefix));
            }

            // Group 1 includes the real text of the expansion, with whitespace
            // around the
            // expression (but inside the curly braces) excluded.

            String expression = matcher.group(1);

            extractors.add(createExpansionExtractor(expression, location, bindingSource));

            startx = matcher.end();
        }

        // Catch anything after the final regexp match.

        if (startx < text.length())
            extractors.add(createTextExtractor(text.substring(startx, text.length())));

        if (extractors.size() == 1)
            return extractors.get(0);

        return creatCompositeExtractor(extractors);
    }

    private static Mapper<DynamicDelegate, String> creatCompositeExtractor(
            final List<Mapper<DynamicDelegate, String>> extractors)
    {
        return new Mapper<DynamicDelegate, String>()
        {
            public String map(final DynamicDelegate delegate)
            {
                StringBuilder builder = new StringBuilder();

                for (Mapper<DynamicDelegate, String> extractor : extractors)
                {
                    String value = extractor.map(delegate);

                    if (value != null)
                        builder.append(value);
                }

                return builder.toString();
            }
        };
    }

    private DynamicTemplateElement comment()
    {
        return createCommentElement(tokenStream.getText());
    }

    private static DynamicTemplateElement createCommentElement(final String content)
    {
        return new DynamicTemplateElement()
        {
            public void render(MarkupWriter writer, RenderQueue queue, DynamicDelegate delegate)
            {
                writer.comment(content);
            }
        };
    }

    private static Mapper<DynamicDelegate, String> createTextExtractor(final String content)
    {
        return new Mapper<DynamicDelegate, String>()
        {
            public String map(DynamicDelegate delegate)
            {
                return content;
            }
        };
    }

    private static Mapper<DynamicDelegate, String> createExpansionExtractor(final String expression,
                                                                            final Location location, final BindingSource bindingSource)
    {
        return new Mapper<DynamicDelegate, String>()
        {
            public String map(DynamicDelegate delegate)
            {
                try
                {
                    Binding binding = bindingSource.newBinding("dynamic template binding", delegate
                            .getComponentResources().getContainerResources(), delegate.getComponentResources(),
                            BindingConstants.PROP, expression, location);

                    Object boundValue = binding.get();

                    return boundValue == null ? null : boundValue.toString();
                } catch (Throwable t)
                {
                    throw new TapestryException(ExceptionUtils.toMessage(t), location, t);
                }
            }
        };
    }

    private <T> T unexpectedEventType()
    {
        XMLTokenType eventType = tokenStream.getEventType();

        throw new IllegalStateException(String.format("Unexpected XML parse event %s.", eventType.name()));
    }

    private static Worker<RenderCommand> createQueueRenderCommand(final RenderQueue queue)
    {
        return new Worker<RenderCommand>()
        {
            public void work(RenderCommand value)
            {
                queue.push(value);
            }
        };
    }

    private static RenderCommand toRenderCommand(final DynamicTemplateElement value, final DynamicDelegate delegate)
    {
        return new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                value.render(writer, queue, delegate);
            }
        };
    }

    private static Mapper<DynamicTemplateElement, RenderCommand> createToRenderCommandMapper(
            final DynamicDelegate delegate)
    {
        return new Mapper<DynamicTemplateElement, RenderCommand>()
        {
            public RenderCommand map(final DynamicTemplateElement value)
            {
                return toRenderCommand(value, delegate);
            }
        };
    }
}
