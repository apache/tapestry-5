// Copyright 2006, 2007 The Apache Software Foundation
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
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.parser.AttributeToken;
import org.apache.tapestry.internal.parser.CommentToken;
import org.apache.tapestry.internal.parser.ExpansionToken;
import org.apache.tapestry.internal.parser.StartElementToken;
import org.apache.tapestry.internal.parser.TextToken;
import org.apache.tapestry.internal.structure.AttributePageElement;
import org.apache.tapestry.internal.structure.CommentPageElement;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.ComponentPageElementImpl;
import org.apache.tapestry.internal.structure.ExpansionPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.structure.PageElement;
import org.apache.tapestry.internal.structure.StartElementPageElement;
import org.apache.tapestry.internal.structure.TextPageElement;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.runtime.RenderQueue;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentMessagesSource;

public class PageElementFactoryImpl implements PageElementFactory
{
    private final ComponentInstantiatorSource _componentInstantiatorSource;

    private final ComponentClassResolver _componentClassResolver;

    private final TypeCoercer _typeCoercer;

    private final BindingSource _bindingSource;

    private final ComponentMessagesSource _messagesSource;

    public PageElementFactoryImpl(ComponentInstantiatorSource componentInstantiatorSource,
            ComponentClassResolver resolver, TypeCoercer typeCoercer, BindingSource bindingSource,
            ComponentMessagesSource messagesSource)
    {
        _componentInstantiatorSource = componentInstantiatorSource;
        _componentClassResolver = resolver;
        _typeCoercer = typeCoercer;
        _bindingSource = bindingSource;
        _messagesSource = messagesSource;
    }

    /** Singleton instance that represents any close tag of any element in any template. */
    private final PageElement _endElement = new PageElement()
    {
        public void render(MarkupWriter writer, RenderQueue queue)
        {
            writer.end();
        }

        @Override
        public String toString()
        {
            return "End";
        }
    };

    public PageElement newStartElement(StartElementToken token)
    {
        return new StartElementPageElement(token.getName());
    }

    public PageElement newTextElement(TextToken token)
    {
        return new TextPageElement(token.getText());
    }

    public PageElement newEndElement()
    {
        return _endElement;
    }

    public PageElement newAttributeElement(AttributeToken token)
    {
        return new AttributePageElement(token.getName(), token.getValue());
    }

    public PageElement newExpansionElement(ComponentResources componentResources,
            ExpansionToken token)
    {
        Binding binding = _bindingSource.newBinding(
                "expansion",
                componentResources,
                componentResources,
                TapestryConstants.PROP_BINDING_PREFIX,
                token.getExpression(),
                token.getLocation());

        return new ExpansionPageElement(binding, _typeCoercer);
    }

    public ComponentPageElement newComponentElement(Page page, ComponentPageElement container,
            String id, String componentType, String componentClassName, String elementName,
            Location location)
    {
        String finalClassName = componentClassName;

        // This awkwardness is making me think that the page loader should resolve the component
        // type before invoking this method (we would then remove the componentType parameter).

        if (InternalUtils.isNonBlank(componentType))
        {
            // The type actually overrides the specified class name. The class name is defined
            // by the type of the field. In many scenarios, the field type is a common interface,
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

        Instantiator instantiator = _componentInstantiatorSource.findInstantiator(finalClassName);

        // The container for any components is the loading component, regardless of
        // how the component elements are nested within the loading component's
        // template.

        ComponentPageElementImpl result = new ComponentPageElementImpl(page, container, id,
                elementName, instantiator, _typeCoercer, _messagesSource, location);

        page.addLifecycleListener(result);

        container.addEmbeddedElement(result);

        addMixins(result, instantiator);

        return result;
    }

    public ComponentPageElement newRootComponentElement(Page page, String componentType)
    {
        Instantiator instantiator = _componentInstantiatorSource.findInstantiator(componentType);

        ComponentPageElementImpl result = new ComponentPageElementImpl(page, instantiator,
                _typeCoercer, _messagesSource);

        addMixins(result, instantiator);

        page.addLifecycleListener(result);

        return result;
    }

    private void addMixins(ComponentPageElement component, Instantiator instantiator)
    {
        ComponentModel model = instantiator.getModel();
        for (String mixinClassName : model.getMixinClassNames())
            addMixinByClassName(component, mixinClassName);
    }

    public PageElement newRenderBodyElement(final ComponentPageElement component)
    {
        return new PageElement()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                component.enqueueBeforeRenderBody(queue);
            }

            @Override
            public String toString()
            {
                return String.format("RenderBody[%s]", component.getNestedId());
            }
        };
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

    public PageElement newCommentElement(CommentToken token)
    {
        return new CommentPageElement(token.getComment());
    }
}
