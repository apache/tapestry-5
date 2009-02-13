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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.internal.parser.*;
import org.apache.tapestry5.internal.services.*;
import org.apache.tapestry5.internal.structure.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.InvalidationListener;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * There's still a lot of room to beef up {@link org.apache.tapestry5.internal.pageload.ComponentAssembler} and {@link
 * org.apache.tapestry5.internal.pageload.EmbeddedComponentAssembler} to perform more static analysis.
 */
public class PageLoaderImpl implements PageLoader, InvalidationListener
{
    private static final class Key
    {
        private final String className;

        private final Locale locale;

        private Key(String className, Locale locale)
        {
            this.className = className;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!className.equals(key.className))
                return false;

            return locale.equals(key.locale);
        }

        @Override
        public int hashCode()
        {
            return 31 * className.hashCode() + locale.hashCode();
        }

        @Override
        public String toString()
        {
            return String.format("Key[%s, %s]", className, locale);
        }
    }

    private static final PageAssemblyAction POP_EMBEDDED_COMPONENT_ACTION = new PageAssemblyAction()
    {
        public void execute(PageAssembly pageAssembly)
        {
            pageAssembly.flushComposableRenderCommands();

            pageAssembly.createdElement.pop();
            pageAssembly.bodyElement.pop();
            pageAssembly.embeddedAssembler.pop();
        }
    };

    private static final RenderCommand END_ELEMENT = new RenderCommand()
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

    private static final PageAssemblyAction END_ELEMENT_ACTION = new PageAssemblyAction()
    {
        public void execute(PageAssembly pageAssembly)
        {
            pageAssembly.addRenderCommand(END_ELEMENT);
        }
    };

    private static final PageAssemblyAction FLUSH_COMPOSABLE_RENDER_COMMANDS_ACTION = new PageAssemblyAction()
    {
        public void execute(PageAssembly pageAssembly)
        {
            pageAssembly.flushComposableRenderCommands();
        }
    };

    private final Map<Key, ComponentAssembler> cache = CollectionFactory.newConcurrentMap();

    private final ComponentInstantiatorSource instantiatorSource;

    private final ComponentTemplateSource templateSource;

    private final PageElementFactory elementFactory;

    private final ComponentPageElementResourcesSource resourcesSource;

    private final ComponentClassResolver componentClassResolver;

    private final PersistentFieldManager persistentFieldManager;

    public PageLoaderImpl(ComponentInstantiatorSource instantiatorSource,
                          ComponentTemplateSource templateSource, PageElementFactory elementFactory,
                          ComponentPageElementResourcesSource resourcesSource,
                          ComponentClassResolver componentClassResolver,
                          PersistentFieldManager persistentFieldManager)
    {
        this.instantiatorSource = instantiatorSource;
        this.templateSource = templateSource;
        this.elementFactory = elementFactory;
        this.resourcesSource = resourcesSource;
        this.componentClassResolver = componentClassResolver;
        this.persistentFieldManager = persistentFieldManager;
    }

    public void objectWasInvalidated()
    {
        cache.clear();
    }

    public Page loadPage(String logicalPageName, Locale locale)
    {
        String pageClassName = componentClassResolver.resolvePageNameToClassName(logicalPageName);

        Page page = new PageImpl(logicalPageName, locale, persistentFieldManager);

        ComponentAssembler assembler = getAssembler(pageClassName, locale);

        ComponentPageElement rootElement = assembler.assembleRootComponent(page);

        page.setRootElement(rootElement);

        // The page is *loaded* before it is attached to the request.
        // This is to help ensure that no client-specific information leaks
        // into the page's default state.

        page.loaded();

        return page;
    }

    private ComponentAssembler getAssembler(String className, Locale locale)
    {
        Key key = new Key(className, locale);

        ComponentAssembler result = cache.get(key);

        if (result == null)
        {
            result = createAssembler(className, locale);
            cache.put(key, result);
        }

        return result;
    }

    private ComponentAssembler createAssembler(String className, Locale locale)
    {
        Instantiator instantiator = instantiatorSource.getInstantiator(className);

        ComponentModel componentModel = instantiator.getModel();

        ComponentTemplate template = templateSource.getTemplate(componentModel, locale);

        ComponentPageElementResources resources = resourcesSource.get(locale);

        ComponentAssembler assembler = new ComponentAssemblerImpl(instantiator, resources);

        // "Program" the assembler by adding actions to it. The actions execute every time a new page instance
        // is needed.

        programAssembler(assembler, template);

        return assembler;
    }

    /**
     * "Programs" the assembler by analyzing the component, its mixins and its embedded components (both in the template
     * and in the Java class), adding new PageAssemblyActions.
     */
    private void programAssembler(ComponentAssembler assembler, ComponentTemplate template)
    {
        assembler.validateEmbeddedIds(template);

        // TODO: Optimize for the case of the empty template.

        addImplementationMixins(assembler);

        processTemplate(assembler, template);
    }

    /**
     * Add any mixins defined for the component (implementation mixins, rather than instance mixins).
     */

    private void addImplementationMixins(ComponentAssembler assembler)
    {
        final List<String> mixinClassNames = assembler.getModel().getMixinClassNames();

        if (mixinClassNames.isEmpty()) return;

        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                ComponentPageElement element = pageAssembly.activeElement.peek();

                for (String className : mixinClassNames)
                {
                    Instantiator mixinInstantiator = instantiatorSource.getInstantiator(className);

                    element.addMixin(InternalUtils.lastTerm(className), mixinInstantiator);

                }
            }
        });
    }

    private void processTemplate(ComponentAssembler assembler, ComponentTemplate template)
    {
        if (template.isMissing())
        {
            // Pretend the template has a single <t:body> element.

            body(assembler);

            return;
        }

        TokenStream stream = new TokenStreamImpl(template);

        while (stream.more())
        {
            processTemplateToken(assembler, stream);
        }

        addFlushAction(assembler);
    }

    private void processTemplateToken(ComponentAssembler assembler, TokenStream stream)
    {
        // These tokens can appear at the top level, or at lower levels (this method is invoked
        // from token-processing loops inside element(), component(), etc.

        switch (stream.peekType())
        {
            case TEXT:

                text(assembler, stream.next(TextToken.class));
                break;

            case EXPANSION:
                expansion(assembler, stream.next(ExpansionToken.class));
                break;

            case BODY:
                stream.next();

                body(assembler);
                break;

            case START_ELEMENT:
                // Will consume past matching end token
                element(assembler, stream);
                break;

            case START_COMPONENT:
                // Will consume past matching end token
                component(assembler, stream);
                break;

            // ATTRIBUTE and END_ELEMENT can't happen at the top level, they're
            // handled at a lower level. (inside element(), component(), etc.)

            case COMMENT:
                comment(assembler, stream.next(CommentToken.class));
                break;

            case BLOCK:
                // Will consume past matching end token
                block(assembler, stream);
                break;

            case PARAMETER:
                // Will consume past the matching end token
                parameter(assembler, stream);
                break;

            case DTD:
                dtd(assembler, stream.next(DTDToken.class));
                break;

            case DEFINE_NAMESPACE_PREFIX:

                defineNamespacePrefix(assembler, stream.next(DefineNamespacePrefixToken.class));
                break;

            case CDATA:
                cdata(assembler, stream.next(CDATAToken.class));
                break;

            default:
                throw new IllegalStateException("Not yet implemented: " + stream.peekType());
        }
    }

    private void cdata(ComponentAssembler assembler, final CDATAToken token)
    {
        RenderCommand command = new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                writer.cdata(token.getContent());
            }

            @Override
            public String toString()
            {
                return String.format("CDATA[%s]", token.getLocation());
            }
        };

        addComposableCommand(assembler, command);

    }

    private void defineNamespacePrefix(ComponentAssembler assembler, final DefineNamespacePrefixToken token)
    {
        RenderCommand command = new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                writer.defineNamespace(token.getNamespaceURI(), token.getNamespacePrefix());
            }

            @Override
            public String toString()
            {
                return String.format("DefineNamespace[%s %s]", token.getNamespacePrefix(), token.getNamespaceURI());
            }
        };


        addComposableCommand(assembler, command);
    }

    private void dtd(ComponentAssembler assembler, final DTDToken token)
    {
        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                if (!pageAssembly.checkAndSetFlag("dtd-page-element-added"))
                {
                    RenderCommand command = new DTDPageElement(token.getName(), token.getPublicId(),
                                                               token.getSystemId());

                    // It doesn't really matter where this ends up in the tree as long as its inside
                    // a portion that always renders.

                    pageAssembly.addComposableRenderCommand(command);
                }
            }
        });
    }

    private void parameter(ComponentAssembler assembler, TokenStream stream)
    {
        final ParameterToken token = stream.next(ParameterToken.class);

        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                String parameterName = token.getName();

                pageAssembly.flushComposableRenderCommands();

                ComponentPageElement element = pageAssembly.createdElement.peek();

                BlockImpl block = new BlockImpl(token.getLocation(),
                                                String.format("Parameter %s of %s",
                                                              parameterName,
                                                              element.getCompleteId()));

                Binding binding = new LiteralBinding(token.getLocation(), "block parameter " + parameterName, block);

                EmbeddedComponentAssembler embeddedAssembler = pageAssembly.embeddedAssembler.peek();

                ParameterBinder binder = embeddedAssembler.createBinder(parameterName, binding);

                if (binder == null)
                {
                    String message = String.format(
                            "Component %s does not include a parameter '%s' (and does not support informal parameters).",
                            element.getCompleteId(),
                            parameterName);

                    throw new TapestryException(message, token.getLocation(), null);
                }

                binder.bind(pageAssembly.activeElement.peek(), element);

                pageAssembly.bodyElement.push(block);
            }
        });

        consumeToEndElementAndPopBodyElement(assembler, stream);
    }

    private void block(ComponentAssembler assembler, TokenStream stream)
    {
        final BlockToken token = stream.next(BlockToken.class);

        final String blockId = token.getId();

        // TODO: The description string should be calculatable statically, we just need
        // to keep yet another stack (of complete component ids).

        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                ComponentPageElement element = pageAssembly.activeElement.peek();

                String description = blockId == null
                                     ? String.format("Anonymous within %s", element.getCompleteId())
                                     : String.format("%s within %s", blockId, element.getCompleteId());

                BlockImpl block = new BlockImpl(token.getLocation(), description);

                if (blockId != null)
                    element.addBlock(blockId, block);

                // Start directing template content into the Block
                pageAssembly.flushComposableRenderCommands();
                pageAssembly.bodyElement.push(block);
            }
        });

        consumeToEndElementAndPopBodyElement(assembler, stream);
    }

    private void consumeToEndElementAndPopBodyElement(ComponentAssembler assembler, TokenStream stream)
    {
        while (true)
        {
            switch (stream.peekType())
            {
                case END_ELEMENT:

                    stream.next();

                    assembler.add(new PageAssemblyAction()
                    {
                        public void execute(PageAssembly pageAssembly)
                        {
                            pageAssembly.flushComposableRenderCommands();
                            pageAssembly.bodyElement.pop();
                        }
                    });

                    return;

                default:
                    processTemplateToken(assembler, stream);
            }
        }
    }

    private void comment(ComponentAssembler assembler, CommentToken token)
    {
        RenderCommand commentElement = new CommentPageElement(token.getComment());

        addComposableCommand(assembler, commentElement);
    }

    private void component(ComponentAssembler assembler, TokenStream stream)
    {
        StartComponentToken token = stream.next(StartComponentToken.class);

        EmbeddedComponentAssembler embeddedAssembler = startComponent(assembler, token);

        while (true)
        {
            switch (stream.peekType())
            {
                case ATTRIBUTE:

                    bindAttributeAsParameter(assembler, embeddedAssembler, stream.next(AttributeToken.class));

                    break;

                case END_ELEMENT:

                    stream.next();

                    assembler.add(POP_EMBEDDED_COMPONENT_ACTION);

                    return;

                default:
                    processTemplateToken(assembler, stream);
            }
        }

    }

    private void bindAttributeAsParameter(ComponentAssembler assembler, EmbeddedComponentAssembler embeddedAssembler,
                                          AttributeToken token)
    {
        addParameterBindingAction(assembler, embeddedAssembler, token.getName(), token.getValue(),
                                  BindingConstants.LITERAL);
    }

    private void element(ComponentAssembler assembler, TokenStream stream)
    {
        StartElementToken token = stream.next(StartElementToken.class);

        RenderCommand element = new StartElementPageElement(token.getNamespaceURI(), token.getName());

        addComposableCommand(assembler, element);

        while (true)
        {
            switch (stream.peekType())
            {
                case ATTRIBUTE:
                    attribute(assembler, stream.next(AttributeToken.class));
                    break;

                case END_ELEMENT:

                    assembler.add(END_ELEMENT_ACTION);

                    stream.next();

                    // Pop out a level.
                    return;

                default:
                    processTemplateToken(assembler, stream);
            }
        }

    }

    private EmbeddedComponentAssembler startComponent(ComponentAssembler assembler, StartComponentToken token)
    {
        String elementName = token.getElementName();

        // Initial guess: the type from the token (but this may be null in many cases).
        String embeddedType = token.getComponentType();

        // This may be null for an anonymous component.
        String embeddedId = token.getId();

        String embeddedComponentClassName = null;

        final EmbeddedComponentModel embeddedModel =
                embeddedId == null
                ? null
                : assembler.getModel().getEmbeddedComponentModel(embeddedId);

        if (embeddedId == null)
            embeddedId = assembler.generateEmbeddedId(embeddedType);

        if (embeddedModel != null)
        {
            String modelType = embeddedModel.getComponentType();

            if (InternalUtils.isNonBlank(modelType) && embeddedType != null)
            {
                String message = String.format(
                        "Embedded component '%s' provides a type attribute in the template ('%s') " +
                                "as well as in the component class ('%s'). " +
                                "You should not provide a type attribute in the template when defining an embedded component " +
                                "within the component class.",
                        embeddedId, embeddedType, modelType
                );

                throw new TapestryException(message, token, null);
            }

            embeddedType = modelType;
            embeddedComponentClassName = embeddedModel.getComponentClassName();
        }

        String componentClassName = embeddedComponentClassName;

        // This awkwardness is making me think that the page loader should resolve the component
        // type before invoking this method (we would then remove the componentType parameter).

        if (InternalUtils.isNonBlank(embeddedType))
        {
            // The type actually overrides the specified class name. The class name is defined
            // by the type of the field. In many scenarios, the field type is a common
            // interface,
            // and the type is used to determine the concrete class to instantiate.

            try
            {
                componentClassName = componentClassResolver.resolveComponentTypeToClassName(embeddedType);
            }
            catch (IllegalArgumentException ex)
            {
                throw new TapestryException(ex.getMessage(), token, ex);
            }
        }


        // OK, now we can record an action to get it instantiated.

        EmbeddedComponentAssembler embeddedAssembler = new EmbeddedComponentAssemblerImpl(instantiatorSource,
                                                                                          elementFactory,
                                                                                          componentClassName,
                                                                                          token.getLocation());

        addActionForEmbeddedComponent(assembler, embeddedAssembler, embeddedId, elementName, componentClassName,
                                      token.getLocation());


        addMixinActions(assembler, embeddedAssembler, embeddedModel, token.getMixins());

        bindParametersFromEmbeddedModel(assembler, embeddedAssembler, embeddedModel);


        if (embeddedModel != null && embeddedModel.getInheritInformalParameters())
        {
            // Another two-step:  The first "captures" the container and embedded component. The second
            // occurs at the end of the page setup.

            assembler.add(new PageAssemblyAction()
            {
                public void execute(PageAssembly pageAssembly)
                {
                    final ComponentPageElement container = pageAssembly.activeElement.peek();
                    final ComponentPageElement embedded = pageAssembly.createdElement.peek();

                    pageAssembly.deferred.add(new PageAssemblyAction()
                    {
                        public void execute(PageAssembly pageAssembly)
                        {
                            copyInformalParameters(container, embedded);
                        }
                    });
                }
            });

        }

        return embeddedAssembler;

    }

    private void copyInformalParameters(ComponentPageElement container, ComponentPageElement embedded)
    {
        // TODO:  Much more, this is an area where we can make things a bit more efficient by tracking
        // what has and hasn't been bound in the EmbeddedComponentAssembler (and identifying what is
        // and isn't informal).

        ComponentModel model = embedded.getComponentResources().getComponentModel();

        Map<String, Binding> informals = container.getInformalParameterBindings();

        for (String name : informals.keySet())
        {
            if (model.getParameterModel(name) != null) continue;

            Binding binding = informals.get(name);

            embedded.bindParameter(name, binding);
        }
    }

    private void bindParametersFromEmbeddedModel(ComponentAssembler assembler,
                                                 EmbeddedComponentAssembler embeddedAssembler,
                                                 EmbeddedComponentModel embeddedModel)
    {
        if (embeddedModel == null) return;

        for (String name : embeddedModel.getParameterNames())
        {
            String parameterValue = embeddedModel.getParameterValue(name);

            addParameterBindingAction(assembler, embeddedAssembler, name, parameterValue, BindingConstants.PROP);
        }
    }

    private void addParameterBindingAction(ComponentAssembler assembler,
                                           EmbeddedComponentAssembler embeddedAssembler,
                                           String parameterName,
                                           String parameterValue,
                                           String defaultBindingPrefix)
    {
        if (embeddedAssembler.isBound(parameterName)) return;

        embeddedAssembler.setBound(parameterName);

        if (parameterValue.startsWith(InternalConstants.INHERIT_BINDING_PREFIX))
        {

            String containerParameterName = parameterValue.substring(InternalConstants.INHERIT_BINDING_PREFIX.length());

            addInhertedBindingAction(assembler, parameterName, containerParameterName);
            return;
        }


        final ParameterBinder binder = embeddedAssembler.createBinder(parameterName, parameterValue,
                                                                      defaultBindingPrefix);

        // Null meaning an informal parameter and the component (and mixins) doesn't support informals.

        if (binder != null)
        {
            assembler.add(new PageAssemblyAction()
            {
                public void execute(PageAssembly pageAssembly)
                {
                    binder.bind(pageAssembly.activeElement.peek(), pageAssembly.createdElement.peek());
                }
            });
        }
    }

    /**
     * Adds a deferred action to the PageAssembly, to handle connecting the embedded components' parameter to the
     * container component's parameter once everything else has been built.
     *
     * @param assembler
     * @param parameterName
     * @param containerParameterName
     */
    private void addInhertedBindingAction(ComponentAssembler assembler,
                                          final String parameterName,
                                          final String containerParameterName)
    {
        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                // At the time this action executes, we'll be able to capture the containing and embedded
                // component. We can then defer the connection logic until after all other construction.

                final ComponentPageElement container = pageAssembly.activeElement.peek();
                final ComponentPageElement embedded = pageAssembly.createdElement.peek();

                pageAssembly.deferred.add(new PageAssemblyAction()
                {
                    public void execute(PageAssembly pageAssembly)
                    {
                        connectInheritedParameter(container, embedded, parameterName,
                                                  containerParameterName);
                    }
                });
            }
        });
    }

    private void connectInheritedParameter(ComponentPageElement container, ComponentPageElement embedded,
                                           String parameterName,
                                           String containerParameterName)
    {
        // TODO: This assumes that the two parameters are both on the core component and not on
        // a mixin.

        Binding containerBinding = container.getBinding(containerParameterName);

        if (containerBinding == null) return;

        String description = String.format("InheritedBinding[parameter %s %s(inherited from %s of %s)]",
                                           parameterName,
                                           embedded.getCompleteId(),
                                           containerParameterName,
                                           container.getCompleteId());

        // This helps with debugging, and re-orients any thrown exceptions
        // to the location of the inherited binding, rather than the container component's
        // binding.

        Binding inherited = new InheritedBinding(description, containerBinding, embedded.getLocation());

        embedded.bindParameter(parameterName, inherited);
    }

    private ComponentModel getModel(String componentClassName)
    {
        return instantiatorSource.getInstantiator(componentClassName).getModel();
    }

    private void addMixinActions(ComponentAssembler assembler,
                                 EmbeddedComponentAssembler embeddedAssembler, EmbeddedComponentModel model,
                                 String mixins
    )
    {
        if (model != null)
        {
            addMixinActionsFromEmbeddedModel(assembler, embeddedAssembler, model);
        }

        List<String> classNames = CollectionFactory.newList();

        for (String typeName : TapestryInternalUtils.splitAtCommas(mixins))
        {
            classNames.add(componentClassResolver.resolveMixinTypeToClassName(typeName));
        }

        addMixinActionsForClassNames(assembler, embeddedAssembler, classNames);
    }

    private void addMixinActionsFromEmbeddedModel(ComponentAssembler assembler,
                                                  EmbeddedComponentAssembler embeddedAssembler,
                                                  EmbeddedComponentModel model
    )
    {
        addMixinActionsForClassNames(assembler, embeddedAssembler, model.getMixinClassNames());
    }

    private void addMixinActionsForClassNames(ComponentAssembler assembler,
                                              EmbeddedComponentAssembler embeddedAssembler,
                                              final List<String> classNames
    )
    {
        for (String className : classNames)
        {
            embeddedAssembler.addInstanceMixin(getModel(className));
        }

        if (!classNames.isEmpty())
        {
            assembler.add(new PageAssemblyAction()
            {
                public void execute(PageAssembly pageAssembly)
                {
                    ComponentPageElement embedded = pageAssembly.createdElement.peek();

                    for (String className : classNames)
                    {
                        Instantiator instantiator = instantiatorSource.getInstantiator(className);

                        embedded.addMixin(InternalUtils.lastTerm(className), instantiator);
                    }
                }
            });
        }
    }

    private void addActionForEmbeddedComponent(ComponentAssembler assembler,
                                               final EmbeddedComponentAssembler embeddedAssembler,
                                               final String embeddedId,
                                               final String elementName,
                                               final String componentClassName,
                                               final Location location)
    {
        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                pageAssembly.flushComposableRenderCommands();

                Locale locale = pageAssembly.page.getLocale();

                ComponentAssembler assemblerForSubcomponent = getAssembler(componentClassName, locale);

                // Remeber: this pushes onto to the createdElement stack, but does not pop it.

                assemblerForSubcomponent.assembleEmbeddedComponent(pageAssembly, embeddedId, elementName, location);

                // ... which is why we can find it via peek() here.  And it's our responsibility
                // to clean it up.

                ComponentPageElement embeddedElement = pageAssembly.createdElement.peek();

                // Add the new element to the template of its container.

                pageAssembly.addRenderCommand(embeddedElement);

                // And redirect any futher content from this component's template to go into
                // the body of the embedded element.

                pageAssembly.bodyElement.push(embeddedElement);
                pageAssembly.embeddedAssembler.push(embeddedAssembler);

                // The means we need to pop the createdElement, bodyElement and embeddedAssembler stacks
                // when done with this sub-component, which is what POP_EMBEDDED_COMPONENT_ACTION does.
            }
        });
    }


    private void attribute(ComponentAssembler assembler, final AttributeToken token)
    {
        String value = token.getValue();

        // No expansion makes this easier, more efficient.
        if (value.indexOf(InternalConstants.EXPANSION_START) < 0)
        {
            RenderCommand command = new RenderAttribute(token);

            addComposableCommand(assembler, command);

            return;
        }

        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                InternalComponentResources resources = pageAssembly.activeElement.peek().getComponentResources();

                RenderCommand command = elementFactory.newAttributeElement(resources, token);

                // Still composable, BTW.

                pageAssembly.addRenderCommand(command);
            }
        });
    }

    private void body(ComponentAssembler assembler)
    {
        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                ComponentPageElement element = pageAssembly.activeElement.peek();

                pageAssembly.addRenderCommand(new RenderBodyElement(element));
            }
        });
    }

    private void expansion(ComponentAssembler assembler, final ExpansionToken token)
    {
        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                ComponentResources resources = pageAssembly.activeElement.peek().getComponentResources();

                // TODO: Add composably

                RenderCommand command = elementFactory.newExpansionElement(resources, token);

                pageAssembly.addRenderCommand(command);
            }
        });
    }

    private void text(ComponentAssembler assembler, TextToken textToken)
    {
        addComposableCommand(assembler, new TextPageElement(textToken.getText()));
    }

    private void addComposableCommand(ComponentAssembler assembler, final RenderCommand command)
    {
        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                pageAssembly.addComposableRenderCommand(command);
            }
        });
    }

    private void addFlushAction(ComponentAssembler assembler)
    {
        assembler.add(FLUSH_COMPOSABLE_RENDER_COMMANDS_ACTION);
    }

}

