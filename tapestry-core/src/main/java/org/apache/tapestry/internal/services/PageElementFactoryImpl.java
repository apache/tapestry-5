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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import static org.apache.tapestry.TapestryConstants.PROP_BINDING_PREFIX;
import org.apache.tapestry.internal.parser.AttributeToken;
import org.apache.tapestry.internal.parser.ExpansionToken;
import org.apache.tapestry.internal.structure.*;
import org.apache.tapestry.ioc.Location;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.runtime.RenderQueue;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.ComponentClassResolver;

import java.util.List;
import java.util.Locale;

public class PageElementFactoryImpl implements PageElementFactory
{
    private final ComponentInstantiatorSource _componentInstantiatorSource;

    private final ComponentClassResolver _componentClassResolver;

    private final TypeCoercer _typeCoercer;

    private final BindingSource _bindingSource;

    private final PageResourcesSource _pageResourcesSource;

    private static final String EXPANSION_START = "${";

    private static class LiteralStringProvider implements StringProvider
    {
        private final String _string;

        LiteralStringProvider(String string)
        {
            _string = string;
        }

        public String provideString()
        {
            return _string;
        }
    }

    public PageElementFactoryImpl(ComponentInstantiatorSource componentInstantiatorSource,
                                  ComponentClassResolver resolver, TypeCoercer typeCoercer, BindingSource bindingSource,
                                  PageResourcesSource pageResourcesSource)
    {
        _componentInstantiatorSource = componentInstantiatorSource;
        _componentClassResolver = resolver;
        _typeCoercer = typeCoercer;
        _bindingSource = bindingSource;
        _pageResourcesSource = pageResourcesSource;
    }

    public PageElement newAttributeElement(ComponentResources componentResources, AttributeToken token)
    {
        final StringProvider provider = parseAttributeExpansionExpression(token.getValue(), componentResources,
                                                                          token.getLocation());

        final String namespace = token.getNamespaceURI();
        final String name = token.getName();

        return new PageElement()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                writer.attributeNS(namespace, name, provider.provideString());
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
            int expansionx = expression.indexOf(EXPANSION_START, startx);

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

            final Binding binding = _bindingSource.newBinding("attribute expansion", resources, resources,
                                                              PROP_BINDING_PREFIX, expansion, location);

            final StringProvider provider = new StringProvider()
            {
                public String provideString()
                {
                    try
                    {
                        Object raw = binding.get();

                        return _typeCoercer.coerce(raw, String.class);
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

    public PageElement newExpansionElement(ComponentResources componentResources, ExpansionToken token)
    {
        Binding binding = _bindingSource.newBinding("expansion", componentResources, componentResources,
                                                    PROP_BINDING_PREFIX, token.getExpression(), token.getLocation());

        return new ExpansionPageElement(binding, _typeCoercer);
    }

    public ComponentPageElement newComponentElement(Page page, ComponentPageElement container, String id,
                                                    String componentType, String componentClassName, String elementName,
                                                    Location location)
    {
        try
        {
            String finalClassName = componentClassName;

            // This awkwardness is making me think that the page loader should resolve the component
            // type before invoking this method (we would then remove the componentType parameter).

            if (InternalUtils.isNonBlank(componentType))
            {
                // The type actually overrides the specified class name. The class name is defined
                // by the type of the field. In many scenarios, the field type is a common
                // interface,
                // and the type is used to determine the concrete class to instantiate.

                try
                {
                    finalClassName = _componentClassResolver
                            .resolveComponentTypeToClassName(componentType);
                }
                catch (IllegalArgumentException ex)
                {
                    throw new TapestryException(ex.getMessage(), location, ex);
                }
            }

            Instantiator instantiator = _componentInstantiatorSource
                    .findInstantiator(finalClassName);

            // This is actually a good place to check for recursive templates, here where we've
            // resolved
            // the component type to a fully qualified class name.

            checkForRecursion(finalClassName, container, location);

            // The container for any components is the loading component, regardless of
            // how the component elements are nested within the loading component's
            // template.

            ComponentPageElement result = container.newChild(id, elementName, instantiator, location);

            page.addLifecycleListener(result);

            addMixins(result, instantiator);

            return result;
        }
        catch (RuntimeException ex)
        {
            throw new TapestryException(ex.getMessage(), location, ex);
        }
    }

    private void checkForRecursion(String componentClassName, ComponentPageElement container, Location location)
    {
        // Container may be null for a root element;

        if (container == null) return;

        ComponentResources resources = container.getComponentResources();

        while (resources != null)
        {
            if (resources.getComponentModel().getComponentClassName().equals(componentClassName))
                throw new TapestryException(ServicesMessages.componentRecursion(componentClassName), location, null);

            resources = resources.getContainerResources();
        }
    }

    public ComponentPageElement newRootComponentElement(Page page, String componentType, Locale locale)
    {
        Instantiator instantiator = _componentInstantiatorSource.findInstantiator(componentType);

        PageResources pageResources = _pageResourcesSource.get(locale);

        ComponentPageElement result = new ComponentPageElementImpl(page, instantiator, pageResources);

        page.addLifecycleListener(result);

        addMixins(result, instantiator);

        return result;
    }

    private void addMixins(ComponentPageElement component, Instantiator instantiator)
    {
        ComponentModel model = instantiator.getModel();
        for (String mixinClassName : model.getMixinClassNames())
            addMixinByClassName(component, mixinClassName);
    }

    public void addMixinByTypeName(ComponentPageElement component, String mixinType)
    {
        String mixinClassName = _componentClassResolver.resolveMixinTypeToClassName(mixinType);

        addMixinByClassName(component, mixinClassName);
    }

    public void addMixinByClassName(ComponentPageElement component, String mixinClassName)
    {
        Instantiator mixinInstantiator = _componentInstantiatorSource
                .findInstantiator(mixinClassName);

        component.addMixin(mixinInstantiator);
    }

    public Binding newBinding(String parameterName, ComponentResources loadingComponentResources,
                              ComponentResources embeddedComponentResources, String defaultBindingPrefix,
                              String expression, Location location)
    {

        if (expression.contains(EXPANSION_START))
        {
            StringProvider provider = parseAttributeExpansionExpression(expression, loadingComponentResources,
                                                                        location);

            return new AttributeExpansionBinding(provider, location);
        }

        return _bindingSource.newBinding(parameterName, loadingComponentResources,
                                         embeddedComponentResources, defaultBindingPrefix, expression, location);
    }
}
