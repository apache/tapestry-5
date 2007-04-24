// Copyright 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newLinkedList;
import static org.apache.tapestry.ioc.internal.util.InternalUtils.isBlank;
import static org.apache.tapestry.ioc.internal.util.InternalUtils.isNonBlank;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.bindings.LiteralBinding;
import org.apache.tapestry.internal.parser.AttributeToken;
import org.apache.tapestry.internal.parser.BlockToken;
import org.apache.tapestry.internal.parser.BodyToken;
import org.apache.tapestry.internal.parser.CommentToken;
import org.apache.tapestry.internal.parser.ComponentTemplate;
import org.apache.tapestry.internal.parser.EndElementToken;
import org.apache.tapestry.internal.parser.ExpansionToken;
import org.apache.tapestry.internal.parser.ParameterToken;
import org.apache.tapestry.internal.parser.StartComponentToken;
import org.apache.tapestry.internal.parser.StartElementToken;
import org.apache.tapestry.internal.parser.TemplateToken;
import org.apache.tapestry.internal.parser.TextToken;
import org.apache.tapestry.internal.structure.BlockImpl;
import org.apache.tapestry.internal.structure.BodyPageElement;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.structure.PageElement;
import org.apache.tapestry.internal.structure.PageImpl;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.EmbeddedComponentModel;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.PersistentFieldManager;

/**
 * Contains all the work-state related to the {@link PageLoaderImpl}.
 */
class PageLoaderProcessor
{
    private static Runnable NO_OP = new Runnable()
    {
        public void run()
        {
            // Do nothing.
        }
    };

    private LinkedList<ComponentPageElement> _activeElementStack = newLinkedList();

    private boolean _addAttributesAsComponentBindings = false;

    private final BindingSource _bindingSource;

    private final LinkedList<BodyPageElement> _bodyPageElementStack = newLinkedList();

    private final LinkedList<ComponentPageElement> _componentQueue = newLinkedList();

    private final LinkedList<Boolean> _discardEndTagStack = newLinkedList();

    private final LinkedList<Runnable> _endElementCommandStack = newLinkedList();

    private final IdAllocator _idAllocator = new IdAllocator();

    private final LinkFactory _linkFactory;

    private ComponentModel _loadingComponentModel;

    private ComponentPageElement _loadingElement;

    private Locale _locale;

    private final OneShotLock _lock = new OneShotLock();

    private Page _page;

    private final PageElementFactory _pageElementFactory;

    private final PersistentFieldManager _persistentFieldManager;

    private final ComponentTemplateSource _templateSource;

    public PageLoaderProcessor(ComponentTemplateSource templateSource,
            PageElementFactory pageElementFactory, BindingSource bindingSource,
            LinkFactory linkFactory, PersistentFieldManager persistentFieldManager)
    {
        _templateSource = templateSource;
        _pageElementFactory = pageElementFactory;
        _bindingSource = bindingSource;
        _linkFactory = linkFactory;
        _persistentFieldManager = persistentFieldManager;
    }

    private void bindParameterFromTemplate(ComponentPageElement component, AttributeToken token)
    {
        String name = token.getName();
        ComponentResources resources = component.getComponentResources();

        // If already bound (i.e., from the component class, via @Component), then
        // ignore the value in the template. This may need improving to just ignore
        // the value if it is an unprefixed literal string.

        if (resources.isBound(name))
            return;

        // Meta default of literal for the template.

        String defaultBindingPrefix = determineDefaultBindingPrefix(
                component,
                name,
                InternalConstants.LITERAL_BINDING_PREFIX);

        Binding binding = _bindingSource.newBinding(
                "parameter " + name,
                _loadingElement.getComponentResources(),
                component.getComponentResources(),
                defaultBindingPrefix,
                token.getValue(),
                token.getLocation());

        component.bindParameter(name, binding);
    }

    // As element, components, parameters or blocks are started, they push an element onto this
    // stack. Whenever an end element token is reached, the top value is popped off and executed,
    // to return state to where it should be.

    private void addMixinsToComponent(ComponentPageElement component, EmbeddedComponentModel model,
            String mixins)
    {
        if (model != null)
        {
            for (String mixinClassName : model.getMixinClassNames())
                _pageElementFactory.addMixinByClassName(component, mixinClassName);
        }

        if (mixins != null)
        {
            for (String type : mixins.split(","))
                _pageElementFactory.addMixinByTypeName(component, type);
        }
    }

    private void bindParametersFromModel(EmbeddedComponentModel model,
            ComponentPageElement loadingComponent, ComponentPageElement component)
    {
        for (String name : model.getParameterNames())
        {
            String value = model.getParameterValue(name);

            String defaultBindingPrefix = determineDefaultBindingPrefix(
                    component,
                    name,
                    InternalConstants.PROP_BINDING_PREFIX);

            // At some point we may add meta data to control what the default prefix is within a
            // component.

            Binding binding = _bindingSource.newBinding(
                    "parameter " + name,
                    loadingComponent.getComponentResources(),
                    component.getComponentResources(),
                    defaultBindingPrefix,
                    value,
                    null);

            component.bindParameter(name, binding);
        }
    }

    /**
     * Determines the default binding prefix for a particular parameter.
     * 
     * @param component
     *            the component which will have a parameter bound
     * @param parameterName
     *            the name of the parameter
     * @param informalParameterBindingPrefix
     *            the default to use for informal parameters
     * @return the binding prefix
     */
    private String determineDefaultBindingPrefix(ComponentPageElement component,
            String parameterName, String informalParameterBindingPrefix)
    {
        String defaultBindingPrefix = component.getDefaultBindingPrefix(parameterName);

        return defaultBindingPrefix != null ? defaultBindingPrefix : informalParameterBindingPrefix;
    }

    private void addRenderBodyElement()
    {
        PageElement element = _pageElementFactory.newRenderBodyElement(_loadingElement);

        _loadingElement.addToTemplate(element);
    }

    private void addToBody(PageElement element)
    {
        _bodyPageElementStack.peek().addToBody(element);
    }

    private void attribute(AttributeToken token)
    {
        // This kind of bookkeeping is ugly, we probably should have distinct (if very similar)
        // tokens for attributes and for parameter bindings.

        if (_addAttributesAsComponentBindings)
        {
            ComponentPageElement activeElement = _activeElementStack.peek();

            bindParameterFromTemplate(activeElement, token);
            return;
        }

        PageElement element = _pageElementFactory.newAttributeElement(token);

        addToBody(element);
    }

    private void body(BodyToken token)
    {
        addRenderBodyElement();

        // BODY tokens are *not* matched by END_ELEMENT tokens. Nor will there be
        // text or comment content "inside" the BODY.
    }

    private void comment(CommentToken token)
    {
        PageElement commentElement = _pageElementFactory.newCommentElement(token);

        addToBody(commentElement);
    }

    private void endElement(EndElementToken token)
    {
        // discard will be false if the matching start token was for a static element, and will be
        // true otherwise (component, block, parameter).

        boolean discard = _discardEndTagStack.removeFirst();

        if (!discard)
        {
            PageElement element = _pageElementFactory.newEndElement();

            addToBody(element);
        }

        Runnable runnable = _endElementCommandStack.removeFirst();

        // Used to return environment to prior state.

        runnable.run();
    }

    private void expansion(ExpansionToken token)
    {
        PageElement element = _pageElementFactory.newExpansionElement(_loadingElement
                .getComponentResources(), token);

        addToBody(element);
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
     * As currently implemented, this should be invoked just once and then the instance should be
     * discarded.
     */
    public Page loadPage(String pageClassName, Locale locale)
    {
        // Ensure that loadPage() may only be invoked once.

        _lock.lock();

        _locale = locale;

        _page = new PageImpl(pageClassName, _locale, _linkFactory, _persistentFieldManager);

        loadRootComponent(pageClassName);

        workComponentQueue();

        // The page is *loaded* before it is attached to the request.
        // This is to help ensure that no client-specific information leaks
        // into the page.

        _page.loaded();

        return _page;
    }

    private void loadRootComponent(String className)
    {
        ComponentPageElement rootComponent = _pageElementFactory.newRootComponentElement(
                _page,
                className);

        _page.setRootElement(rootComponent);

        _componentQueue.addFirst(rootComponent);
    }

    /**
     * Do you smell something? I'm smelling that this class needs to be redesigned to not need a
     * central method this large and hard to test. I think a lot of instance and local variables
     * need to be bundled up into some kind of process object. This code is effectively too big to
     * be tested except through integration testing.
     */
    private void loadTemplateForComponent(ComponentPageElement loadingElement)
    {
        _loadingElement = loadingElement;
        _loadingComponentModel = loadingElement.getComponentResources().getComponentModel();

        String componentClassName = _loadingComponentModel.getComponentClassName();
        ComponentTemplate template = _templateSource.getTemplate(_loadingComponentModel, _locale);

        // When the template for a component is missing, we pretend it consists of just a RenderBody
        // phase. Missing is not an error ... many component simply do not have a template.

        if (template.isMissing())
        {
            addRenderBodyElement();
            return;
        }

        // Pre-allocate ids to avoid later name collisions.

        Log log = _loadingComponentModel.getLog();

        Set<String> embeddedIds = CollectionFactory.newSet(_loadingComponentModel
                .getEmbeddedComponentIds());

        _idAllocator.clear();

        for (String id : template.getComponentIds())
        {
            _idAllocator.allocateId(id);
            embeddedIds.remove(id);
        }

        if (!embeddedIds.isEmpty())
            log.error(ServicesMessages.embeddedComponentsNotInTemplate(
                    embeddedIds,
                    componentClassName));

        _addAttributesAsComponentBindings = false;

        // The outermost elements of the template belong in the loading component's template list,
        // not its body list. This shunt allows everyone else to not have to make that decision,
        // they can add to the "body" and (if there isn't an active component), the shunt will
        // add the element to the component's template.

        BodyPageElement shunt = new BodyPageElement()
        {
            public void addToBody(PageElement element)
            {
                _loadingElement.addToTemplate(element);
            }
        };

        _bodyPageElementStack.addFirst(shunt);

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
                    body((BodyToken) token);
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
                    endElement((EndElementToken) token);
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

                default:
                    throw new IllegalStateException("Not implemented yet: " + token);
            }
        }

        // For neatness / symmetry:

        _bodyPageElementStack.removeFirst(); // the shunt

        // TODO: Check that all stacks are empty. That should never happen, as long
        // as the ComponentTemplate is valid.
    }

    private void parameter(ParameterToken token)
    {
        BlockImpl block = new BlockImpl(token.getLocation());
        String name = token.getName();

        Binding binding = new LiteralBinding("block parameter " + name, block, token.getLocation());

        // TODO: Check that the t:parameter doesn't appear outside of an embedded component.

        _activeElementStack.peek().bindParameter(name, binding);

        setupBlock(block);
    }

    private void setupBlock(BodyPageElement block)
    {
        _bodyPageElementStack.addFirst(block);
        _discardEndTagStack.addFirst(true);

        Runnable cleanup = new Runnable()
        {
            public void run()
            {
                _bodyPageElementStack.removeFirst();
            }
        };

        _endElementCommandStack.add(cleanup);
    }

    private void block(BlockToken token)
    {
        // Don't use the page element factory here becauses we need something that is both Block and
        // BodyPageElement
        // and don't want to use casts.

        BlockImpl block = new BlockImpl(token.getLocation());

        String id = token.getId();

        if (id != null)
            _loadingElement.addBlock(id, block);

        setupBlock(block);
    }

    private void startComponent(StartComponentToken token)
    {
        String elementName = token.getElementName();

        // Initial guess: the type from the token (but this may be null in many cases).
        String embeddedType = token.getType();

        String embeddedId = token.getId();

        String embeddedComponentClassName = null;

        // We know that if embeddedId is null, embeddedType is not.

        if (embeddedId == null)
            embeddedId = generateEmbeddedId(embeddedType, _idAllocator);

        EmbeddedComponentModel embeddedModel = _loadingComponentModel
                .getEmbeddedComponentModel(embeddedId);

        if (embeddedModel != null)
        {
            String modelType = embeddedModel.getComponentType();

            if (isNonBlank(modelType) && embeddedType != null)
            {
                Log log = _loadingComponentModel.getLog();
                log.error(ServicesMessages.compTypeConflict(embeddedId, embeddedType, modelType));
            }

            embeddedType = modelType;
            embeddedComponentClassName = embeddedModel.getComponentClassName();
        }

        if (isBlank(embeddedType) && isBlank(embeddedComponentClassName))
        {
            // non-null means its invisible instrumentation; the Any component
            // will mimic the actual element, w/ body and informal parameters.

            if (elementName != null)
                embeddedType = "Any";
            else
                throw new TapestryException(ServicesMessages.noTypeForEmbeddedComponent(
                        embeddedId,
                        _loadingComponentModel.getComponentClassName()), token, null);
        }

        ComponentPageElement newComponent = _pageElementFactory.newComponentElement(
                _page,
                _loadingElement,
                embeddedId,
                embeddedType,
                embeddedComponentClassName,
                elementName,
                token.getLocation());

        addMixinsToComponent(newComponent, embeddedModel, token.getMixins());

        if (embeddedModel != null)
            bindParametersFromModel(embeddedModel, _loadingElement, newComponent);

        addToBody(newComponent);

        _componentQueue.addFirst(newComponent);

        // Any attribute tokens that immediately follow should be
        // used to bind parameters.

        _addAttributesAsComponentBindings = true;

        // Any attributes or parameters that come up belong on this component.

        _activeElementStack.addFirst(newComponent);

        // Set things up so that content inside the component is added to the component's body.

        _bodyPageElementStack.addFirst(newComponent);

        // The start tag is not added to the body of the component, so neither should
        // the end tag.

        _discardEndTagStack.addFirst(true);

        // And clean that up when the end element is reached.

        Runnable cleanup = new Runnable()
        {
            public void run()
            {
                _activeElementStack.removeFirst();
                _bodyPageElementStack.removeFirst();
            }
        };

        _endElementCommandStack.addFirst(cleanup);
    }

    private void startElement(StartElementToken token)
    {
        PageElement element = _pageElementFactory.newStartElement(token);

        addToBody(element);

        // Controls how attributes are interpretted.
        _addAttributesAsComponentBindings = false;

        // Start will be matched by end:

        // Do NOT discard the end tag; add it to the body.

        _discardEndTagStack.addFirst(false);
        _endElementCommandStack.addFirst(NO_OP);
    }

    private void text(TextToken token)
    {
        PageElement element = _pageElementFactory.newTextElement(token);

        addToBody(element);
    }

    /** Works the component queue, until exausted. */
    private void workComponentQueue()
    {
        while (!_componentQueue.isEmpty())
        {
            ComponentPageElement componentElement = _componentQueue.removeFirst();

            loadTemplateForComponent(componentElement);
        }
    }
}
