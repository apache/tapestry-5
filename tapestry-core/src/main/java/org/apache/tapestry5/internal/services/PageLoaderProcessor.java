// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.internal.pageload.CompositeRenderCommand;
import org.apache.tapestry5.internal.parser.*;
import org.apache.tapestry5.internal.structure.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.*;
import org.apache.tapestry5.ioc.util.Stack;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ComponentClassResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Contains all the work-state related to the {@link PageLoaderImpl}.
 * <p/>
 * <em>This is the Tapestry heart, this is the Tapestry soul ...</em>
 */
class PageLoaderProcessor
{
    /**
     * Special prefix for parameters that are inherited from named parameters of their container.
     */
    private static final String INHERIT_PREFIX = "inherit:";

    private static final Runnable NO_OP = new Runnable()
    {
        public void run()
        {
            // Do nothing.
        }
    };


    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private Stack<ComponentPageElement> activeElementStack = CollectionFactory.newStack();

    private boolean addAttributesAsComponentBindings = false;

    private boolean dtdAdded;

    private final Stack<BodyPageElement> bodyPageElementStack = CollectionFactory.newStack();

    // You can use a stack as a queue
    private final Stack<ComponentPageElement> componentQueue = CollectionFactory.newStack();

    private final Stack<Boolean> discardEndTagStack = CollectionFactory.newStack();

    private final Stack<Runnable> endElementCommandStack = CollectionFactory.newStack();

    /**
     * Used as a queue of Runnable objects used to handle final setup.
     */
    private final List<Runnable> finalization = CollectionFactory.newList();

    private final List<RenderCommand> compositedRenderCommands = CollectionFactory.newList();

    private final IdAllocator idAllocator = new IdAllocator();

    private final LinkFactory linkFactory;

    private final ComponentClassResolver componentClassResolver;

    private ComponentModel loadingComponentModel;

    private ComponentPageElement loadingElement;

    private final Map<String, Map<String, Binding>> componentIdToBindingMap = CollectionFactory.newMap();

    private Locale locale;

    private final OneShotLock lock = new OneShotLock();

    private Page page;

    private final PageElementFactory pageElementFactory;

    private final PersistentFieldManager persistentFieldManager;

    private final ComponentTemplateSource templateSource;

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

    private static class RenderBodyElement implements RenderCommand
    {
        private final ComponentPageElement component;

        public RenderBodyElement(ComponentPageElement component)
        {
            this.component = component;
        }

        public void render(MarkupWriter writer, RenderQueue queue)
        {
            component.enqueueBeforeRenderBody(queue);
        }

        @Override
        public String toString()
        {
            return String.format("RenderBody[%s]", component.getNestedId());
        }
    }

    PageLoaderProcessor(ComponentTemplateSource templateSource, PageElementFactory pageElementFactory,
                        LinkFactory linkFactory, PersistentFieldManager persistentFieldManager,
                        ComponentClassResolver componentClassResolver)
    {
        this.templateSource = templateSource;
        this.pageElementFactory = pageElementFactory;
        this.linkFactory = linkFactory;
        this.persistentFieldManager = persistentFieldManager;
        this.componentClassResolver = componentClassResolver;
    }

    private void bindParameterFromTemplate(ComponentPageElement component, AttributeToken token)
    {
        String name = token.getName();
        ComponentResources resources = component.getComponentResources();

        // If already bound (i.e., from the component class, via @Component), then
        // ignore the value in the template. This may need improving to just ignore
        // the value if it is an unprefixed literal string.

        if (resources.isBound(name)) return;

        // Meta default of literal for the template.

        String defaultBindingPrefix = determineDefaultBindingPrefix(component, name,
                                                                    BindingConstants.LITERAL);

        Binding binding = findBinding(loadingElement, component, name, token.getValue(), defaultBindingPrefix,
                                      token.getLocation());

        if (binding != null)
        {
            component.bindParameter(name, binding);

            Map<String, Binding> bindingMap = componentIdToBindingMap.get(component
                    .getCompleteId());
            bindingMap.put(name, binding);
        }
    }

    private void addMixinsToComponent(ComponentPageElement component, EmbeddedComponentModel model, String mixins)
    {
        if (model != null)
        {
            for (String mixinClassName : model.getMixinClassNames())
                pageElementFactory.addMixinByClassName(component, mixinClassName);
        }

        if (mixins != null)
        {
            for (String type : COMMA_PATTERN.split(mixins))
                pageElementFactory.addMixinByTypeName(component, type);
        }
    }


    /**
     * @param model                embededded model defining the new component, from an {@link
     *                             org.apache.tapestry5.annotations.Component} annotation
     * @param loadingComponent     the currently loading container component
     * @param newComponent         the new child of the container whose parameters are being bound
     * @param newComponentBindings map of bindings for the new component (used to handle inheriting of informal
     *                             parameters)
     */
    private void bindParametersFromModel(EmbeddedComponentModel model, ComponentPageElement loadingComponent,
                                         ComponentPageElement newComponent, Map<String, Binding> newComponentBindings)
    {
        for (String name : model.getParameterNames())
        {
            String value = model.getParameterValue(name);

            String defaultBindingPrefix = determineDefaultBindingPrefix(newComponent, name,
                                                                        BindingConstants.PROP);

            Binding binding = findBinding(loadingComponent, newComponent, name, value, defaultBindingPrefix,
                                          newComponent.getLocation());

            if (binding != null)
            {
                newComponent.bindParameter(name, binding);

                // So that the binding can be shared if inherited by a subcomponent
                newComponentBindings.put(name, binding);
            }
        }
    }

    /**
     * Creates a new binding, or returns an existing binding (or null) for the "inherit:" binding prefix. Mostly a
     * wrapper around {@link BindingSource#newBinding(String, ComponentResources, ComponentResources, String, String,
     * Location)
     *
     * @return the new binding, or an existing binding (if inherited), or null (if inherited, and the containing
     *         parameter is not bound)
     */
    private Binding findBinding(ComponentPageElement loadingComponent, ComponentPageElement component, String name,
                                String value, String defaultBindingPrefix, Location location)
    {
        if (value.startsWith(INHERIT_PREFIX))
        {
            String loadingParameterName = value.substring(INHERIT_PREFIX.length());
            Map<String, Binding> loadingComponentBindingMap = componentIdToBindingMap
                    .get(loadingComponent.getCompleteId());

            // This may return null if the parameter is not bound in the loading component.

            Binding existing = loadingComponentBindingMap.get(loadingParameterName);

            if (existing == null) return null;

            String description = String.format("InheritedBinding[parameter %s %s(inherited from %s of %s)]", name,
                                               component.getCompleteId(), loadingParameterName,
                                               loadingComponent.getCompleteId());

            // This helps with debugging, and re-orients any thrown exceptions
            // to the location of the inherited binding, rather than the container component's
            // binding.

            return new InheritedBinding(description, existing, location);
        }

        return pageElementFactory.newBinding(name, loadingComponent.getComponentResources(),
                                             component.getComponentResources(), defaultBindingPrefix, value, location);
    }

    /**
     * Determines the default binding prefix for a particular parameter.
     *
     * @param component                      the component which will have a parameter bound
     * @param parameterName                  the name of the parameter
     * @param informalParameterBindingPrefix the default to use for informal parameters
     * @return the binding prefix
     */
    private String determineDefaultBindingPrefix(ComponentPageElement component, String parameterName,
                                                 String informalParameterBindingPrefix)
    {
        String defaultBindingPrefix = component.getDefaultBindingPrefix(parameterName);

        return defaultBindingPrefix != null ? defaultBindingPrefix : informalParameterBindingPrefix;
    }


    private void addToBody(RenderCommand element)
    {
        bodyPageElementStack.peek().addToBody(element);
    }

    private void attribute(AttributeToken token)
    {
        // This kind of bookkeeping is ugly, we probably should have distinct (if very similar)
        // tokens for attributes and for parameter bindings.

        if (addAttributesAsComponentBindings)
        {
            ComponentPageElement activeElement = activeElementStack.peek();

            bindParameterFromTemplate(activeElement, token);
            return;
        }

        RenderCommand element = pageElementFactory.newAttributeElement(loadingElement
                .getComponentResources(), token);

        addComposableCommand(element);
    }

    private void body()
    {
        flushComposedCommands();

        addToBody(new RenderBodyElement(loadingElement));

        // BODY tokens are *not* matched by END_ELEMENT tokens. Nor will there be
        // text or comment content "inside" the BODY.
    }

    private void comment(CommentToken token)
    {
        RenderCommand commentElement = new CommentPageElement(token.getComment());

        addComposableCommand(commentElement);
    }

    /**
     * Invoked whenever a token (start, startComponent, etc.) is encountered that will eventually have a matching end
     * token. Sets up the behavior for the end token.
     *
     * @param discard if true, the end is discarded (if false the end token is added to the active body element)
     * @param command command to execute to return processor state back to what it was before the command executed
     */
    private void configureEnd(boolean discard, Runnable command)
    {
        discardEndTagStack.push(discard);
        endElementCommandStack.push(command);
    }

    private void endElement()
    {
        // discard will be false if the matching start token was for a static element, and will be
        // true otherwise (component, block, parameter).

        boolean discard = discardEndTagStack.pop();

        if (!discard) addComposableCommand(END_ELEMENT);

        Runnable command = endElementCommandStack.pop();

        // Used to return environment to prior state.

        command.run();
    }

    private void expansion(ExpansionToken token)
    {
        RenderCommand element = pageElementFactory.newExpansionElement(loadingElement
                .getComponentResources(), token);

        addComposableCommand(element);
    }

    private String generateEmbeddedId(String embeddedType, IdAllocator idAllocator)
    {
        // Component types may be in folders; strip off the folder part for starters.

        int slashx = embeddedType.lastIndexOf("/");

        String baseId = embeddedType.substring(slashx + 1).toLowerCase();

        // The idAllocator is pre-loaded with all the component ids from the template, so even
        // if the lower-case type matches the id of an existing component, there won't be a name
        // collision.

        return idAllocator.allocateId(baseId);
    }

    /**
     * As currently implemented, this should be invoked just once and then the PageLoaderProcessor instance should be
     * discarded.
     */
    public Page loadPage(String logicalPageName, String pageClassName, Locale locale)
    {
        // Ensure that loadPage() may only be invoked once.

        lock.lock();

        this.locale = locale;

        // Todo: Need a resources object for Pages, not just ComponentPageElement ... too many
        // parameters here.

        page = new PageImpl(logicalPageName, this.locale, linkFactory, persistentFieldManager, componentClassResolver);

        loadRootComponent(pageClassName);

        workComponentQueue();

        // Take care of any finalization logic that's been deferred out.

        for (Runnable r : finalization)
        {
            r.run();
        }

        // The page is *loaded* before it is attached to the request.
        // This is to help ensure that no client-specific information leaks
        // into the page.

        page.loaded();

        return page;
    }

    private void loadRootComponent(String className)
    {
        ComponentPageElement rootComponent = pageElementFactory.newRootComponentElement(page, className, locale);

        page.setRootElement(rootComponent);

        componentQueue.push(rootComponent);
    }

    /**
     * Do you smell something? I'm smelling that this class needs to be redesigned to not need a central method this
     * large and hard to test. I think a lot of instance and local variables need to be bundled up into some kind of
     * process object. This code is effectively too big to be tested except through integration testing.
     */
    private void loadTemplateForComponent(final ComponentPageElement loadingElement)
    {
        this.loadingElement = loadingElement;
        loadingComponentModel = loadingElement.getComponentResources().getComponentModel();

        String componentClassName = loadingComponentModel.getComponentClassName();
        ComponentTemplate template = templateSource.getTemplate(loadingComponentModel, locale);

        // When the template for a component is missing, we pretend it consists of just a RenderBody
        // phase. Missing is not an error ... many component simply do not have a template.

        if (template.isMissing())
        {
            this.loadingElement.addToTemplate(new RenderBodyElement(this.loadingElement));
            return;
        }

        // Pre-allocate ids to avoid later name collisions.

        // Don't have a case-insensitive Set, so we'll make due with a Map
        Map<String, Boolean> embeddedIds = collectedEmbeddedComponentIds(loadingComponentModel);

        idAllocator.clear();

        final Map<String, Location> componentIdsMap = template.getComponentIds();

        for (String id : componentIdsMap.keySet())
        {
            idAllocator.allocateId(id);
            embeddedIds.remove(id);
        }

        if (!embeddedIds.isEmpty())
            throw new RuntimeException(
                    ServicesMessages.embeddedComponentsNotInTemplate(embeddedIds.keySet(), componentClassName,
                                                                     template.getResource()));

        addAttributesAsComponentBindings = false;

        // The outermost elements of the template belong in the loading component's template list,
        // not its body list. This shunt allows everyone else to not have to make that decision,
        // they can add to the "body" and (if there isn't an active component), the shunt will
        // add the element to the component's template.

        BodyPageElement shunt = new BodyPageElement()
        {
            public void addToBody(RenderCommand element)
            {
                loadingElement.addToTemplate(element);
            }
        };

        bodyPageElementStack.push(shunt);

        for (TemplateToken token : template.getTokens())
        {
            switch (token.getTokenType())
            {
                case TEXT:
                    text((TextToken) token);
                    break;

                case EXPANSION:
                    expansion((ExpansionToken) token);
                    break;

                case BODY:
                    body();
                    break;

                case START_ELEMENT:
                    startElement((StartElementToken) token);
                    break;

                case START_COMPONENT:
                    startComponent((StartComponentToken) token);
                    break;

                case ATTRIBUTE:
                    attribute((AttributeToken) token);
                    break;

                case END_ELEMENT:
                    endElement();
                    break;

                case COMMENT:
                    comment((CommentToken) token);
                    break;

                case BLOCK:
                    block((BlockToken) token);
                    break;

                case PARAMETER:
                    parameter((ParameterToken) token);
                    break;

                case DTD:
                    dtd((DTDToken) token);
                    break;

                case DEFINE_NAMESPACE_PREFIX:
                    defineNamespacePrefix((DefineNamespacePrefixToken) token);
                    break;

                case CDATA:
                    cdata((CDATAToken) token);
                    break;

                default:
                    throw new IllegalStateException("Not implemented yet: " + token);
            }
        }


        flushComposedCommands();

        // For neatness / symmetry:

        bodyPageElementStack.pop(); // the shunt

        // TODO: Check that all stacks are empty. That should never happen, as long
        // as the ComponentTemplate is valid.
    }

    /**
     * Returns a pseudo-set (a case insensitive map where the values are always true) for all the embedded component ids
     * in the component.
     *
     * @param componentModel
     * @return map whose keys are the embedded component ids
     */
    private Map<String, Boolean> collectedEmbeddedComponentIds(ComponentModel componentModel)
    {
        Map<String, Boolean> result = CollectionFactory.newCaseInsensitiveMap();

        for (String id : componentModel.getEmbeddedComponentIds())
            result.put(id, true);

        return result;
    }

    private void cdata(final CDATAToken token)
    {
        RenderCommand element = new RenderCommand()
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

        addComposableCommand(element);
    }

    private void defineNamespacePrefix(final DefineNamespacePrefixToken token)
    {
        RenderCommand element = new RenderCommand()
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

        addComposableCommand(element);
    }

    private void parameter(ParameterToken token)
    {
        flushComposedCommands();

        ComponentPageElement element = activeElementStack.peek();
        String name = token.getName();

        BlockImpl block = new BlockImpl(token.getLocation(),
                                        String.format("Parmeter %s of %s", name, element.getCompleteId()));

        Binding binding = new LiteralBinding("block parameter " + name, block, token.getLocation());

        // TODO: Check that the t:parameter doesn't appear outside of an embedded component.

        element.bindParameter(name, binding);

        setupBlock(block);
    }

    private void setupBlock(BodyPageElement block)
    {
        bodyPageElementStack.push(block);

        Runnable cleanup = new Runnable()
        {
            public void run()
            {
                flushComposedCommands();

                bodyPageElementStack.pop();
            }
        };

        configureEnd(true, cleanup);
    }

    private void block(BlockToken token)
    {
        flushComposedCommands();

        String id = token.getId();
        // Don't use the page element factory here becauses we need something that is both Block and
        // BodyPageElement and don't want to use casts.

        String description = id == null
                             ? String.format("Anonymous within %s", loadingElement.getCompleteId())
                             : String.format("%s within %s", id, loadingElement.getCompleteId());

        BlockImpl block = new BlockImpl(token.getLocation(), description);

        if (id != null) loadingElement.addBlock(id, block);

        setupBlock(block);
    }

    private void startComponent(StartComponentToken token)
    {
        String elementName = token.getElementName();

        // Initial guess: the type from the token (but this may be null in many cases).
        String embeddedType = token.getComponentType();

        // This may be null for an anonymous component.
        String embeddedId = token.getId();

        String embeddedComponentClassName = null;

        final EmbeddedComponentModel embeddedModel = embeddedId == null
                                                     ? null
                                                     : loadingComponentModel.getEmbeddedComponentModel(embeddedId);

        // We know that if embeddedId is null, embeddedType is not.

        if (embeddedId == null) embeddedId = generateEmbeddedId(embeddedType, idAllocator);

        if (embeddedModel != null)
        {
            String modelType = embeddedModel.getComponentType();

            if (InternalUtils.isNonBlank(modelType) && embeddedType != null)
                throw new TapestryException(ServicesMessages.compTypeConflict(embeddedId, embeddedType, modelType),
                                            token, null);

            embeddedType = modelType;
            embeddedComponentClassName = embeddedModel.getComponentClassName();
        }

        // We only have the embeddedModel if the embeddedId was specified.  If embeddedType was ommitted
        // and

        if (InternalUtils.isBlank(embeddedType) && embeddedModel == null)
            throw new TapestryException(
                    ServicesMessages.noTypeForEmbeddedComponent(embeddedId,
                                                                loadingComponentModel.getComponentClassName()),
                    token, null);

        final ComponentPageElement newComponent = pageElementFactory.newComponentElement(page, loadingElement,
                                                                                         embeddedId, embeddedType,
                                                                                         embeddedComponentClassName,
                                                                                         elementName,
                                                                                         token.getLocation());

        addMixinsToComponent(newComponent, embeddedModel, token.getMixins());

        final Map<String, Binding> newComponentBindings = CollectionFactory.newMap();
        componentIdToBindingMap.put(newComponent.getCompleteId(), newComponentBindings);

        if (embeddedModel != null)
            bindParametersFromModel(embeddedModel, loadingElement, newComponent, newComponentBindings);

        flushComposedCommands();

        addToBody(newComponent);

        // Remember to load the template for this new component
        componentQueue.push(newComponent);

        // Any attribute tokens that immediately follow should be
        // used to bind parameters.

        addAttributesAsComponentBindings = true;

        // Any attributes (including component parameters) that come up belong on this component.

        activeElementStack.push(newComponent);

        // Set things up so that content inside the component is added to the component's body.

        bodyPageElementStack.push(newComponent);

        // And clean that up when the end element is reached.


        final ComponentModel newComponentModel = newComponent.getComponentResources().getComponentModel();

        // If the component was from an embedded @Component annotation, and it is inheritting informal parameters,
        // and the component in question supports informal parameters, than get those inheritted informal parameters ...
        // but later (this helps ensure that <t:parameter> elements that may provide informal parameters are
        // visible when the informal parameters are copied to the child component).

        if (embeddedModel != null && embeddedModel.getInheritInformalParameters() && newComponentModel.getSupportsInformalParameters())
        {
            final ComponentPageElement loadingElement = this.loadingElement;

            Runnable finalizer = new Runnable()
            {
                public void run()
                {
                    handleInformalParameters(loadingElement, embeddedModel, newComponent, newComponentModel,
                                             newComponentBindings);
                }
            };

            finalization.add(finalizer);
        }


        Runnable cleanup = new Runnable()
        {
            public void run()
            {
                flushComposedCommands();

                activeElementStack.pop();
                bodyPageElementStack.pop();
            }
        };

        // The start tag is not added to the body of the component, so neither should
        // the end tag.
        configureEnd(true, cleanup);
    }

    /**
     * Invoked when a component's end tag is reached, to check and process informal parameters as per the {@link
     * org.apache.tapestry5.model.EmbeddedComponentModel#getInheritInformalParameters()} flag.
     *
     * @param loadingComponent     the container component that was loaded
     * @param model
     * @param newComponent
     * @param newComponentBindings
     */
    private void handleInformalParameters(ComponentPageElement loadingComponent, EmbeddedComponentModel model,
                                          ComponentPageElement newComponent, ComponentModel newComponentModel,
                                          Map<String, Binding> newComponentBindings)
    {

        Map<String, Binding> informals = loadingComponent.getInformalParameterBindings();


        for (String name : informals.keySet())
        {
            if (newComponentModel.getParameterModel(name) != null) continue;

            Binding binding = informals.get(name);

            newComponent.bindParameter(name, binding);
            newComponentBindings.put(name, binding);
        }
    }

    private void startElement(StartElementToken token)
    {
        RenderCommand element = new StartElementPageElement(token.getNamespaceURI(), token.getName());

        addComposableCommand(element);

        // Controls how attributes are interpretted.
        addAttributesAsComponentBindings = false;

        // Index will be matched by end:

        // Do NOT discard the end tag; add it to the body.

        configureEnd(false, NO_OP);
    }

    private void text(TextToken token)
    {
        RenderCommand element = new TextPageElement(token.getText());

        addComposableCommand(element);
    }

    private void dtd(DTDToken token)
    {
        // first DTD encountered wins.
        if (dtdAdded) return;

        RenderCommand element = new DTDPageElement(token.getName(), token.getPublicId(), token.getSystemId());
        // since rendering via the markup writer is to the document tree,
        // we don't really care where this gets placed in the tree; the
        // DTDPageElement will set the dtd of the document directly, rather than
        // writing anything to the markup writer
        page.getRootElement().addToTemplate(element);

        dtdAdded = true;
    }

    /**
     * Works the component queue, until exausted.
     */
    private void workComponentQueue()
    {
        while (!componentQueue.isEmpty())
        {
            ComponentPageElement componentElement = componentQueue.pop();

            loadTemplateForComponent(componentElement);
        }
    }

    private void addComposableCommand(RenderCommand command)
    {
        compositedRenderCommands.add(command);
    }

    private void flushComposedCommands()
    {
        int count = compositedRenderCommands.size();

        switch (count)
        {
            case 0:

                return;

            case 1:

                addToBody(compositedRenderCommands.get(0));

                break;

            default:

                RenderCommand[] commands = compositedRenderCommands.toArray(new RenderCommand[count]);

                addToBody(new CompositeRenderCommand(commands));

                break;
        }

        compositedRenderCommands.clear();
    }
}
