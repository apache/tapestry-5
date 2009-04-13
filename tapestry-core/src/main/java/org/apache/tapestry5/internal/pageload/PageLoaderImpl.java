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
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.internal.parser.*;
import org.apache.tapestry5.internal.services.*;
import org.apache.tapestry5.internal.structure.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.util.Stack;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.InvalidationListener;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * There's still a lot of room to beef up {@link org.apache.tapestry5.internal.pageload.ComponentAssembler} and {@link
 * org.apache.tapestry5.internal.pageload.EmbeddedComponentAssembler} to perform more static analysis.
 * <p/>
 * Loading a page involves a recurive process of creating {@link org.apache.tapestry5.internal.pageload.ComponentAssembler}s:
 * for the root component, then down the tree for each embedded component. A ComponentAssembler is largely a collection
 * of {@link org.apache.tapestry5.internal.pageload.PageAssemblyAction}s. Once created, a ComponentAssembler can quickly
 * assemble any number of component instances. All of the expensive logic, such as fitting template tokens together and
 * matching parameters to bindings, is done as part of the one-time construction of the ComponentAssembler. The end
 * result removes a huge amount of computational redundancy that was present in Tapestry 5.0, but to understand this,
 * you need to split your mind into two phases: construction (of the ComponentAssemblers) and assembly. It's twisted ...
 * and perhaps a bit functional and Monadic.
 * <p/>
 * And truly, <em>This is the Tapestry Heart, This is the Tapestry Soul...</em>
 */
public class PageLoaderImpl implements PageLoader, InvalidationListener, ComponentAssemblerSource
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

            return className.equals(key.className) && locale.equals(key.locale);
        }

        @Override
        public int hashCode()
        {
            return 31 * className.hashCode() + locale.hashCode();
        }
    }

    private static final PageAssemblyAction POP_EMBEDDED_COMPONENT_ACTION = new PageAssemblyAction()
    {
        public void execute(PageAssembly pageAssembly)
        {
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

    private final Map<Key, ComponentAssembler> cache = CollectionFactory.newConcurrentMap();

    private final ComponentInstantiatorSource instantiatorSource;

    private final ComponentTemplateSource templateSource;

    private final PageElementFactory elementFactory;

    private final ComponentPageElementResourcesSource resourcesSource;

    private final ComponentClassResolver componentClassResolver;

    private final PersistentFieldManager persistentFieldManager;

    private final StringInterner interner;

    public PageLoaderImpl(ComponentInstantiatorSource instantiatorSource,
                          ComponentTemplateSource templateSource, PageElementFactory elementFactory,
                          ComponentPageElementResourcesSource resourcesSource,
                          ComponentClassResolver componentClassResolver,
                          PersistentFieldManager persistentFieldManager, StringInterner interner)
    {
        this.instantiatorSource = instantiatorSource;
        this.templateSource = templateSource;
        this.elementFactory = elementFactory;
        this.resourcesSource = resourcesSource;
        this.componentClassResolver = componentClassResolver;
        this.persistentFieldManager = persistentFieldManager;
        this.interner = interner;
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

    public ComponentAssembler getAssembler(String className, Locale locale)
    {
        Key key = new Key(className, locale);

        ComponentAssembler result = cache.get(key);

        if (result == null)
        {
            // There's a window here where two threads may create the same assembler simultaneously;
            // the extra assembler will be discarded.

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

        ComponentAssembler assembler = new ComponentAssemblerImpl(this, instantiatorSource, componentClassResolver,
                                                                  instantiator, resources, locale);

        // "Program" the assembler by adding actions to it. The actions interact with a
        // PageAssembly object (a fresh one for each new page being created). 

        programAssembler(assembler, template);

        return assembler;
    }

    /**
     * "Programs" the assembler by analyzing the component, its mixins and its embedded components (both in the template
     * and in the Java class), adding new PageAssemblyActions.
     */
    private void programAssembler(ComponentAssembler assembler, ComponentTemplate template)
    {
        TokenStream stream = createTokenStream(assembler, template);

        AssemblerContext context = new AssemblerContext(assembler, stream);

        if (template.isMissing())
        {
            // Pretend the template has a single <t:body> element.

            body(context);

            return;
        }


        while (context.more())
        {
            processTemplateToken(context);
        }

        context.flushComposable();
    }

    /**
     * Creates the TokenStream by pre-processing the templates, looking for {@link org.apache.tapestry5.internal.parser.ExtensionPointToken}s
     * and replacing them with appropriate overrides. Also validates that all embedded ids are accounted for.
     */
    private TokenStream createTokenStream(ComponentAssembler assembler, ComponentTemplate template)
    {
        List<TemplateToken> tokens = CollectionFactory.newList();

        Stack<TemplateToken> queue = CollectionFactory.newStack();

        List<ComponentTemplate> overrideSearch = buildOverrideSearch(assembler, template);

        // The base template is the first non-extension template upwards in the hierarchy
        // from this component.

        ComponentTemplate baseTemplate = getLast(overrideSearch);

        pushAll(queue, baseTemplate.getTokens());

        while (!queue.isEmpty())
        {
            TemplateToken token = queue.pop();

            // When an ExtensionPoint is found, it is replaced with the tokens of its override.

            if (token.getTokenType().equals(TokenType.EXTENSION_POINT))
            {
                ExtensionPointToken extensionPointToken = (ExtensionPointToken) token;

                queueOverrideTokensForExtensionPoint(extensionPointToken, queue, overrideSearch);

            }
            else
            {
                tokens.add(token);
            }
        }

        // Build up a map of component ids to locations

        Collections.reverse(overrideSearch);

        Map<String, Location> componentIds = CollectionFactory.newCaseInsensitiveMap();

        for (ComponentTemplate ct : overrideSearch)
        {
            componentIds.putAll(ct.getComponentIds());
        }

        // Validate that every emebedded component id in the template (or inherited from an extended template)
        // is accounted for.

        assembler.validateEmbeddedIds(componentIds, template.getResource());

        return new TokenStreamImpl(tokens);
    }

    private static <T> T getLast(List<T> list)
    {
        int count = list.size();

        return list.get(count - 1);
    }

    private void queueOverrideTokensForExtensionPoint(ExtensionPointToken extensionPointToken,
                                                      Stack<TemplateToken> queue,
                                                      List<ComponentTemplate> overrideSearch)
    {
        String extentionPointId = extensionPointToken.getExtentionPointId();

        // Work up from the component, through its base classes, towards the last non-extension template.

        for (ComponentTemplate t : overrideSearch)
        {
            List<TemplateToken> tokens = t.getExtensionPointTokens(extentionPointId);

            if (tokens != null)
            {
                pushAll(queue, tokens);
                return;
            }
        }

        // Sanity check: since an extension point defines its own default, it's going to be hard to
        // not find an override, somewhere, for it.

        throw new TapestryException(
                String.format("Could not find an override for extension point '%s'.", extentionPointId),
                extensionPointToken.getLocation(), null);
    }

    private List<ComponentTemplate> buildOverrideSearch(ComponentAssembler assembler, ComponentTemplate template)
    {
        List<ComponentTemplate> result = CollectionFactory.newList();
        result.add(template);

        ComponentModel model = assembler.getModel();

        ComponentTemplate lastTemplate = template;

        while (lastTemplate.isExtension())
        {
            ComponentModel parentModel = model.getParentModel();

            if (parentModel == null)
            {
                throw new RuntimeException(String.format(
                        "Component %s uses an extension template, but does not have a parent component.",
                        model.getComponentClassName()));
            }

            ComponentTemplate parentTemplate = templateSource.getTemplate(parentModel, assembler.getLocale());

            result.add(parentTemplate);

            lastTemplate = parentTemplate;

            model = parentModel;
        }

        return result;
    }

    /**
     * Push all the tokens onto the stack, in reverse order, so that the last token is deepest and the first token is
     * most shallow (first to come off the queue).
     */
    private void pushAll(Stack<TemplateToken> queue, List<TemplateToken> tokens)
    {
        for (int i = tokens.size() - 1; i >= 0; i--)
            queue.push(tokens.get(i));
    }

    private void processTemplateToken(AssemblerContext context)
    {
        // These tokens can appear at the top level, or at lower levels (this method is invoked
        // from token-processing loops inside element(), component(), etc.

        switch (context.peekType())
        {
            case TEXT:

                text(context);
                break;

            case EXPANSION:
                expansion(context);
                break;

            case BODY:
                context.next();

                body(context);
                break;

            case START_ELEMENT:
                // Will consume past matching end token
                element(context);
                break;

            case START_COMPONENT:
                // Will consume past matching end token
                component(context);
                break;

            // ATTRIBUTE and END_ELEMENT can't happen at the top level, they're
            // handled at a lower level. (inside element(), component(), etc.)

            case COMMENT:
                comment(context);
                break;

            case BLOCK:
                // Will consume past matching end token
                block(context);
                break;

            case PARAMETER:
                // Will consume past the matching end token
                parameter(context);
                break;

            case DTD:
                dtd(context);
                break;

            case DEFINE_NAMESPACE_PREFIX:

                defineNamespacePrefix(context);
                break;

            case CDATA:
                cdata(context);
                break;

            default:
                throw new IllegalStateException("Not yet implemented: " + context.peekType());
        }
    }

    private void cdata(AssemblerContext context)
    {
        final CDATAToken token = context.next(CDATAToken.class);

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

        context.addComposable(command);

    }

    private void defineNamespacePrefix(AssemblerContext context)
    {
        final DefineNamespacePrefixToken token = context.next(DefineNamespacePrefixToken.class);

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


        context.addComposable(command);
    }

    private void dtd(AssemblerContext context)
    {
        final DTDToken token = context.next(DTDToken.class);

        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                if (!pageAssembly.checkAndSetFlag("dtd-page-element-added"))
                {
                    RenderCommand command = new DTDPageElement(token.getName(), token.getPublicId(),
                                                               token.getSystemId());

                    // It doesn't really matter where this ends up in the tree as long as its inside
                    // a portion that always renders.

                    pageAssembly.addRenderCommand(command);
                }
            }
        });
    }

    private void parameter(AssemblerContext context)
    {
        final ParameterToken token = context.next(ParameterToken.class);

        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                String parameterName = token.getName();

                ComponentPageElement element = pageAssembly.createdElement.peek();

                BlockImpl block = new BlockImpl(token.getLocation(),
                                                interner.format("Parameter %s of %s",
                                                                parameterName,
                                                                element.getCompleteId()));

                Binding binding = new LiteralBinding(token.getLocation(), "block parameter " + parameterName, block);

                EmbeddedComponentAssembler embeddedAssembler = pageAssembly.embeddedAssembler.peek();

                ParameterBinder binder = embeddedAssembler.createParameterBinder(parameterName);

                if (binder == null)
                {
                    String message = String.format(
                            "Component %s does not include a formal parameter '%s' (and does not support informal parameters).",
                            element.getCompleteId(),
                            parameterName);

                    throw new TapestryException(message, token.getLocation(), null);
                }

                binder.bind(pageAssembly.createdElement.peek(), binding);

                pageAssembly.bodyElement.push(block);
            }
        });

        consumeToEndElementAndPopBodyElement(context);
    }

    private void block(AssemblerContext context)
    {
        final BlockToken token = context.next(BlockToken.class);

        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                String blockId = token.getId();

                ComponentPageElement element = pageAssembly.activeElement.peek();

                String description = blockId == null
                                     ? interner.format("Anonymous within %s", element.getCompleteId())
                                     : interner.format("%s within %s", blockId, element.getCompleteId());

                BlockImpl block = new BlockImpl(token.getLocation(), description);

                if (blockId != null)
                    element.addBlock(blockId, block);

                // Start directing template content into the Block
                pageAssembly.bodyElement.push(block);
            }
        });

        consumeToEndElementAndPopBodyElement(context);
    }

    private void consumeToEndElementAndPopBodyElement(AssemblerContext context)
    {
        while (true)
        {
            switch (context.peekType())
            {
                case END_ELEMENT:

                    context.next();

                    context.add(new PageAssemblyAction()
                    {
                        public void execute(PageAssembly pageAssembly)
                        {
                            pageAssembly.bodyElement.pop();
                        }
                    });

                    return;

                default:
                    processTemplateToken(context);
            }
        }
    }

    private void comment(AssemblerContext context)
    {
        CommentToken token = context.next(CommentToken.class);

        RenderCommand commentElement = new CommentPageElement(token.getComment());

        context.addComposable(commentElement);
    }

    private void component(AssemblerContext context)
    {
        EmbeddedComponentAssembler embeddedAssembler = startComponent(context);

        while (true)
        {
            switch (context.peekType())
            {
                case ATTRIBUTE:

                    bindAttributeAsParameter(context, embeddedAssembler);

                    break;

                case END_ELEMENT:

                    context.next();

                    context.add(POP_EMBEDDED_COMPONENT_ACTION);

                    return;

                default:
                    processTemplateToken(context);
            }
        }
    }

    private void bindAttributeAsParameter(AssemblerContext context, EmbeddedComponentAssembler embeddedAssembler)
    {
        AttributeToken token = context.next(AttributeToken.class);

        addParameterBindingAction(context, embeddedAssembler, token.getName(), token.getValue(),
                                  BindingConstants.LITERAL, token.getLocation());
    }

    private void element(AssemblerContext context)
    {
        StartElementToken token = context.next(StartElementToken.class);

        RenderCommand element = new StartElementPageElement(token.getNamespaceURI(), token.getName());

        context.addComposable(element);

        while (true)
        {
            switch (context.peekType())
            {
                case ATTRIBUTE:
                    attribute(context);
                    break;

                case END_ELEMENT:

                    context.next();

                    context.addComposable(END_ELEMENT);

                    // Pop out a level.
                    return;

                default:
                    processTemplateToken(context);
            }
        }

    }

    private EmbeddedComponentAssembler startComponent(AssemblerContext context)
    {
        StartComponentToken token = context.next(StartComponentToken.class);

        ComponentAssembler assembler = context.assembler;
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

        EmbeddedComponentAssembler embeddedAssembler =
                assembler.createEmbeddedAssembler(embeddedId,
                                                  componentClassName,
                                                  embeddedModel,
                                                  token.getMixins(),
                                                  token.getLocation());

        addActionForEmbeddedComponent(context, embeddedAssembler, embeddedId, elementName, componentClassName);

        addParameterBindingActions(context, embeddedAssembler, embeddedModel);

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

    private void addParameterBindingActions(AssemblerContext context,
                                            EmbeddedComponentAssembler embeddedAssembler,
                                            EmbeddedComponentModel embeddedModel)
    {
        if (embeddedModel == null) return;

        for (String parameterName : embeddedModel.getParameterNames())
        {
            String parameterValue = embeddedModel.getParameterValue(parameterName);

            addParameterBindingAction(context,
                                      embeddedAssembler,
                                      parameterName,
                                      parameterValue,
                                      BindingConstants.PROP,
                                      embeddedModel.getLocation());
        }
    }

    private void addParameterBindingAction(AssemblerContext context,
                                           final EmbeddedComponentAssembler embeddedAssembler,
                                           final String parameterName,
                                           final String parameterValue,
                                           final String metaDefaultBindingPrefix,
                                           final Location location)
    {
        if (embeddedAssembler.isBound(parameterName)) return;

        embeddedAssembler.setBound(parameterName);

        if (parameterValue.startsWith(InternalConstants.INHERIT_BINDING_PREFIX))
        {
            String containerParameterName = parameterValue.substring(InternalConstants.INHERIT_BINDING_PREFIX.length());

            addInheritedBindingAction(context, parameterName, containerParameterName);
            return;
        }


        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                // Because of published parameters, we have to wait until page assembly time to throw out
                // informal parameters bound to components that don't support informal parameters ...
                // otherwise we'd throw out (sometimes!) published parameters.

                final ParameterBinder binder = embeddedAssembler.createParameterBinder(parameterName);

                // Null meaning an informal parameter and the component (and mixins) doesn't support informals.

                if (binder != null)
                {
                    final String defaultBindingPrefix = binder.getDefaultBindingPrefix(metaDefaultBindingPrefix);

                    InternalComponentResources containerResources = pageAssembly.activeElement.peek().getComponentResources();

                    ComponentPageElement embeddedElement = pageAssembly.createdElement.peek();
                    InternalComponentResources embeddedResources = embeddedElement.getComponentResources();

                    Binding binding = elementFactory.newBinding(parameterName,
                                                                containerResources,
                                                                embeddedResources,
                                                                defaultBindingPrefix,
                                                                parameterValue,
                                                                location);

                    binder.bind(embeddedElement, binding);
                }
            }
        });
    }

    /**
     * Adds a deferred action to the PageAssembly, to handle connecting the embedded components' parameter to the
     * container component's parameter once everything else has been built.
     *
     * @param assembler
     * @param parameterName
     * @param containerParameterName
     */
    private void addInheritedBindingAction(AssemblerContext context,
                                           final String parameterName,
                                           final String containerParameterName)
    {
        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                // At the time this action executes, we'll be able to capture the containing and embedded
                // component. We can then defer the connection logic until after all other construction.

                final ComponentPageElement container = pageAssembly.activeElement.peek();
                final ComponentPageElement embedded = pageAssembly.createdElement.peek();

                // Parameters are normally bound bottom to top. Inherited parameters run differently, and should be
                // top to bottom. 
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

    private void connectInheritedParameter(ComponentPageElement container,
                                           ComponentPageElement embedded,
                                           String parameterName,
                                           String containerParameterName)
    {
        // TODO: This assumes that the two parameters are both on the core component and not on
        // a mixin. I think this could be improved with more static analysis.

        Binding containerBinding = container.getBinding(containerParameterName);

        if (containerBinding == null) return;

        // This helps with debugging, and re-orients any thrown exceptions
        // to the location of the inherited binding, rather than the container component's
        // binding.

        // Binding inherited = new InheritedBinding(description, containerBinding, embedded.getLocation());

        embedded.bindParameter(parameterName, containerBinding);
    }

    private void addActionForEmbeddedComponent(AssemblerContext context,
                                               final EmbeddedComponentAssembler embeddedAssembler,
                                               final String embeddedId,
                                               final String elementName,
                                               final String componentClassName)
    {
        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                pageAssembly.checkForRecursion(componentClassName, embeddedAssembler.getLocation());

                Locale locale = pageAssembly.page.getLocale();

                ComponentAssembler assemblerForSubcomponent = getAssembler(componentClassName, locale);

                // Remeber: this pushes onto to the createdElement stack, but does not pop it.

                assemblerForSubcomponent.assembleEmbeddedComponent(pageAssembly, embeddedAssembler, embeddedId,
                                                                   elementName, embeddedAssembler.getLocation());

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


    private void attribute(AssemblerContext context)
    {
        final AttributeToken token = context.next(AttributeToken.class);

        String value = token.getValue();

        // No expansion makes this easier, more efficient.
        if (value.indexOf(InternalConstants.EXPANSION_START) < 0)
        {
            RenderCommand command = new RenderAttribute(token);

            context.addComposable(command);

            return;
        }

        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                InternalComponentResources resources = pageAssembly.activeElement.peek().getComponentResources();

                RenderCommand command = elementFactory.newAttributeElement(resources, token);

                pageAssembly.addRenderCommand(command);
            }
        });
    }

    private void body(AssemblerContext context)
    {
        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                ComponentPageElement element = pageAssembly.activeElement.peek();

                pageAssembly.addRenderCommand(new RenderBodyElement(element));
            }
        });
    }

    private void expansion(AssemblerContext context)
    {
        final ExpansionToken token = context.next(ExpansionToken.class);

        context.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                ComponentResources resources = pageAssembly.activeElement.peek().getComponentResources();

                RenderCommand command = elementFactory.newExpansionElement(resources, token);

                pageAssembly.addRenderCommand(command);
            }
        });
    }

    private void text(AssemblerContext context)
    {
        TextToken textToken = context.next(TextToken.class);

        context.addComposable(new TextPageElement(textToken.getText()));
    }

}

