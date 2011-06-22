// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.parser.AttributeToken;
import org.apache.tapestry5.internal.parser.ExpansionToken;
import org.apache.tapestry5.internal.structure.ExpansionPageElement;
import org.apache.tapestry5.ioc.Location;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.BindingSource;

import java.util.List;

public class PageElementFactoryImpl implements PageElementFactory
{
    private final TypeCoercer typeCoercer;

    private final BindingSource bindingSource;

    private static class LiteralStringProvider implements StringProvider
    {
        private final String string;

        LiteralStringProvider(String string)
        {
            this.string = string;
        }

        public String provideString()
        {
            return string;
        }
    }

    public PageElementFactoryImpl(TypeCoercer typeCoercer, BindingSource bindingSource)
    {
        this.typeCoercer = typeCoercer;
        this.bindingSource = bindingSource;
    }

    public RenderCommand newAttributeElement(ComponentResources componentResources, final AttributeToken token)
    {
        final StringProvider provider = parseAttributeExpansionExpression(token.getValue(), componentResources,
                                                                          token.getLocation());

        final String namespace = token.getNamespaceURI();
        final String name = token.getName();

        return new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                writer.attributeNS(namespace, name, provider.provideString());
            }

            public String toString()
            {
                return String.format("AttributeNS[%s %s \"%s\"]", namespace, name, token.getValue());
            }
        };
    }

    private StringProvider parseAttributeExpansionExpression(String expression, ComponentResources resources,
                                                             final Location location)
    {
        final List<StringProvider> providers = newList();

        int startx = 0;

        while (true)
        {
            int expansionx = expression.indexOf(InternalConstants.EXPANSION_START, startx);

            // No more expansions, add in the rest of the string as a literal.

            if (expansionx < 0)
            {
                if (startx < expression.length())
                    providers.add(new LiteralStringProvider(expression.substring(startx)));
                break;
            }

            // Add in a literal string chunk for the characters between the last expansion and
            // this expansion.

            if (startx != expansionx)
                providers.add(new LiteralStringProvider(expression.substring(startx, expansionx)));

            int endx = expression.indexOf("}", expansionx);

            if (endx < 0) throw new TapestryException(ServicesMessages
                    .unclosedAttributeExpression(expression), location, null);

            String expansion = expression.substring(expansionx + 2, endx);

            final Binding binding = bindingSource.newBinding("attribute expansion", resources, resources,
                                                             BindingConstants.PROP, expansion, location);

            final StringProvider provider = new StringProvider()
            {
                public String provideString()
                {
                    try
                    {
                        Object raw = binding.get();

                        return typeCoercer.coerce(raw, String.class);
                    }
                    catch (Exception ex)
                    {
                        throw new TapestryException(ex.getMessage(), location, ex);
                    }
                }
            };

            providers.add(provider);

            // Restart the search after '}'

            startx = endx + 1;
        }

        // Simplify the typical case, where the entire attribute is just a single expansion:

        if (providers.size() == 1) return providers.get(0);

        return new StringProvider()
        {

            public String provideString()
            {
                StringBuilder builder = new StringBuilder();

                for (StringProvider provider : providers)
                    builder.append(provider.provideString());

                return builder.toString();
            }
        };
    }

    public RenderCommand newExpansionElement(ComponentResources componentResources, ExpansionToken token)
    {
        Binding binding = bindingSource.newBinding("expansion", componentResources, componentResources,
                                                   BindingConstants.PROP, token.getExpression(), token.getLocation());

        return new ExpansionPageElement(binding, typeCoercer);
    }

    public Binding newBinding(String parameterName, ComponentResources loadingComponentResources,
                              ComponentResources embeddedComponentResources, String defaultBindingPrefix,
                              String expression, Location location)
    {

        if (expression.contains(InternalConstants.EXPANSION_START))
        {
            StringProvider provider = parseAttributeExpansionExpression(expression, loadingComponentResources,
                                                                        location);

            return new AttributeExpansionBinding(location, provider);
        }

        return bindingSource.newBinding(parameterName, loadingComponentResources,
                                        embeddedComponentResources, defaultBindingPrefix, expression, location);
    }
}
