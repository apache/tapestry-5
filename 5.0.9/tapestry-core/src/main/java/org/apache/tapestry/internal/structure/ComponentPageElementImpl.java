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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.*;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.services.ComponentEventImpl;
import org.apache.tapestry.internal.services.EventImpl;
import org.apache.tapestry.internal.services.Instantiator;
import org.apache.tapestry.internal.util.NotificationEventCallback;
import org.apache.tapestry.ioc.BaseLocatable;
import org.apache.tapestry.ioc.Location;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.ParameterModel;
import org.apache.tapestry.runtime.*;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implements {@link org.apache.tapestry.internal.structure.PageElement} and represents a component within an overall
 * page. Much of a component page element's behavior is delegated to user code, via a {@link
 * org.apache.tapestry.runtime.Component} instance.
 * <p/>
 * Once instantiated, a ComponentPageElement should be registered as a {@linkplain
 * org.apache.tapestry.internal.structure.Page#addLifecycleListener(org.apache.tapestry.runtime.PageLifecycleListener)
 * lifecycle listener}. This could be done inside the constructors, but that tends to complicate unit tests, so its done
 * by {@link org.apache.tapestry.internal.services.PageElementFactoryImpl}.
 * <p/>
 */
public class ComponentPageElementImpl extends BaseLocatable implements ComponentPageElement, PageLifecycleListener
{
    private static final ComponentCallback CONTAINING_PAGE_DID_ATTACH = new ComponentCallback()
    {
        public void run(Component component)
        {
            component.containingPageDidAttach();
        }
    };

    private static final ComponentCallback CONTAINING_PAGE_DID_DETACH = new ComponentCallback()
    {
        public void run(Component component)
        {
            component.containingPageDidDetach();
        }
    };

    private static final ComponentCallback CONTAINING_PAGE_DID_LOAD = new ComponentCallback()
    {
        public void run(Component component)
        {
            component.containingPageDidLoad();
        }
    };

    private static final ComponentCallback POST_RENDER_CLEANUP = new ComponentCallback()
    {
        public void run(Component component)
        {
            component.postRenderCleanup();
        }
    };

    // For the moment, every component will have a template, even if it consists of
    // just a page element to queue up a BeforeRenderBody phase.

    private static void pushElements(RenderQueue queue, List<PageElement> list)
    {
        int count = size(list);
        for (int i = count - 1; i >= 0; i--)
            queue.push(list.get(i));
    }

    private static int size(List<?> list)
    {
        return list == null ? 0 : list.size();
    }

    private static class RenderPhaseEventHandler implements ComponentEventCallback
    {
        private boolean _result = true;

        private List<RenderCommand> _commands;

        boolean getResult()
        {
            return _result;
        }

        public boolean handleResult(Object result, Component component, String methodDescription)
        {
            if (result instanceof Boolean)
            {
                _result = (Boolean) result;
                return true; // abort other handler methods
            }

            if (result instanceof RenderCommand)
            {
                RenderCommand command = (RenderCommand) result;

                add(command);

                return false; // do not abort!
            }

            if (result instanceof Renderable)
            {
                final Renderable renderable = (Renderable) result;

                RenderCommand wrapper = new RenderCommand()
                {
                    public void render(MarkupWriter writer, RenderQueue queue)
                    {
                        renderable.render(writer);
                    }
                };

                add(wrapper);

                return false;
            }

            throw new TapestryException(StructureMessages.wrongEventResultType(methodDescription, Boolean.class),
                                        component, null);
        }

        private void add(RenderCommand command)
        {
            if (_commands == null) _commands = newList();

            _commands.add(command);
        }

        public void queueCommands(RenderQueue queue)
        {
            if (_commands == null) return;

            for (RenderCommand command : _commands)
                queue.push(command);
        }
    }

    private final RenderCommand _afterRender = new RenderCommand()
    {
        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.afterRender(writer, event);
                }
            };

            invoke(true, callback);

            if (!handler.getResult()) queue.push(_beginRender);

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("AfterRender");
        }
    };

    private final RenderCommand _afterRenderBody = new RenderCommand()
    {
        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.afterRenderBody(writer, event);
                }
            };

            invoke(true, callback);

            if (!handler.getResult()) queue.push(_beforeRenderBody);

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("AfterRenderBody");
        }
    };

    private final RenderCommand _afterRenderTemplate = new RenderCommand()
    {
        public void render(final MarkupWriter writer, final RenderQueue queue)
        {
            RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.afterRenderTemplate(writer, event);
                }
            };

            invoke(true, callback);

            if (!handler.getResult()) queue.push(_beforeRenderTemplate);

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("AfterRenderTemplate");
        }
    };

    private final RenderCommand _beforeRenderBody = new RenderCommand()
    {
        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.beforeRenderBody(writer, event);
                }
            };

            invoke(false, callback);

            queue.push(_afterRenderBody);

            if (handler.getResult()) pushElements(queue, _body);

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("BeforeRenderBody");
        }
    };

    private final RenderCommand _beforeRenderTemplate = new RenderCommand()
    {
        public void render(final MarkupWriter writer, final RenderQueue queue)
        {
            final RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.beforeRenderTemplate(writer, event);
                }
            };

            invoke(false, callback);

            queue.push(_afterRenderTemplate);

            if (handler.getResult()) pushElements(queue, _template);

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("BeforeRenderTemplate");
        }
    };

    private final RenderCommand _beginRender = new RenderCommand()
    {
        public void render(final MarkupWriter writer, final RenderQueue queue)
        {
            RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.beginRender(writer, event);
                }
            };

            invoke(false, callback);

            queue.push(_afterRender);

            // If the component has no template whatsoever, then a
            // renderBody element is added as the lone element of the component's template.
            // So every component will have a non-empty template.

            if (handler.getResult()) queue.push(_beforeRenderTemplate);

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("BeginRender");
        }
    };

    private Map<String, Block> _blocks;

    private List<PageElement> _body;

    private Map<String, ComponentPageElement> _children;

    private final String _elementName;

    private final PageResources _pageResources;

    private final RenderCommand _cleanupRender = new RenderCommand()
    {
        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.cleanupRender(writer, event);
                }
            };

            invoke(true, callback);

            if (handler.getResult())
            {
                _rendering = false;

                Element current = writer.getElement();

                if (current != _elementAtSetup)
                    throw new TapestryException(StructureMessages.unbalancedElements(_completeId), getLocation(), null);

                _elementAtSetup = null;

                invoke(false, POST_RENDER_CLEANUP);

                // NOW and only now the component is done rendering and fully cleaned up. Decrement
                // the page's dirty count. If the entire render goes well, then the page will be
                // clean and can be stored into the pool for later reuse.

                _page.decrementDirtyCount();
            }
            else
            {
                queue.push(_setupRender);
            }

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("CleanupRender");
        }
    };

    private final String _completeId;

    // The user-provided class, with runtime code enhancements. In a component with mixins, this
    // is the component to which the mixins are attached.
    private final Component _coreComponent;

    /**
     * Component lifecycle instances for all mixins; the core component is added to this list during page load. This is
     * only used in the case that a component has mixins (in which case, the core component is listed last).
     */
    private List<Component> _components = null;

    private final ComponentPageElement _container;

    private final InternalComponentResources _coreResources;

    private final String _id;

    private boolean _loaded;

    /**
     * Map from mixin name to resources for the mixin. Created when first mixin is added.
     */
    private Map<String, InternalComponentResources> _mixinsByShortName;

    private final String _nestedId;

    private final Page _page;

    private boolean _rendering;

    /**
     * Used to detect mismatches calls to {@link MarkupWriter#element(String, Object[])} } and {@link
     * org.apache.tapestry.MarkupWriter#end()}.  The expectation is that any element(s) begun by this component during
     * rendering will be balanced by end() calls, resulting in the current element reverting to its initial value.
     */
    private Element _elementAtSetup;

    private final RenderCommand _setupRender = new RenderCommand()
    {
        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            // TODO: Check for recursive rendering.

            _rendering = true;

            _elementAtSetup = writer.getElement();

            RenderPhaseEventHandler handler = new RenderPhaseEventHandler();
            final Event event = new EventImpl(handler);

            ComponentCallback callback = new ComponentCallback()
            {
                public void run(Component component)
                {
                    component.setupRender(writer, event);
                }
            };

            invoke(false, callback);

            queue.push(_cleanupRender);

            if (handler.getResult()) queue.push(_beginRender);

            handler.queueCommands(queue);
        }

        @Override
        public String toString()
        {
            return phaseToString("SetupRender");
        }
    };

    // We know that, at the very least, there will be an element to force the component to render
    // its body, so there's no reason to wait to initialize the list.

    private final List<PageElement> _template = newList();


    public ComponentPageElement newChild(String id, String elementName, Instantiator instantiator, Location location)
    {
        ComponentPageElementImpl child = new ComponentPageElementImpl(_page, this, id, elementName, instantiator,
                                                                      location, _pageResources);

        addEmbeddedElement(child);

        return child;
    }

    /**
     * Constructor for other components embedded within the root component or at deeper levels of the hierarchy.
     *
     * @param page          ultimately containing this component
     * @param container     component immediately containing this component (may be null for a root component)
     * @param id            unique (within the container) id for this component (may be null for a root component)
     * @param elementName   the name of the element which represents this component in the template, or null for
     *                      &lt;comp&gt; element or a page component
     * @param instantiator  used to create the new component instance and access the component's model
     * @param location      location of the element (within a template), used as part of exception reporting
     * @param pageResources Provides access to common methods of various services
     */

    ComponentPageElementImpl(Page page, ComponentPageElement container, String id, String elementName,
                             Instantiator instantiator, Location location, PageResources pageResources)
    {
        super(location);

        _page = page;
        _container = container;
        _id = id;
        _elementName = elementName;
        _pageResources = pageResources;

        ComponentResources containerResources = container == null ? null : container
                .getComponentResources();

        String pageName = _page.getLogicalName();

        // A page (really, the root component of a page) does not have a container.

        if (container == null)
        {
            _completeId = pageName;
            _nestedId = null;
        }
        else
        {
            String caselessId = id.toLowerCase();

            String parentNestedId = container.getNestedId();

            // The root element has no nested id.
            // The children of the root element have an id.

            if (parentNestedId == null)
            {
                _nestedId = caselessId;
                _completeId = pageName + ":" + caselessId;
            }
            else
            {
                _nestedId = parentNestedId + "." + caselessId;
                _completeId = container.getCompleteId() + "." + caselessId;
            }
        }

        _coreResources = new InternalComponentResourcesImpl(_page, this, containerResources, instantiator,
                                                            _pageResources);

        _coreComponent = _coreResources.getComponent();
    }

    /**
     * Constructor for the root component of a page.
     */
    public ComponentPageElementImpl(Page page, Instantiator instantiator, PageResources pageResources)
    {
        this(page, null, null, null, instantiator, null, pageResources);
    }

    public void addEmbeddedElement(ComponentPageElement child)
    {
        if (_children == null) _children = newCaseInsensitiveMap();

        String childId = child.getId();

        ComponentPageElement existing = _children.get(childId);
        if (existing != null)
            throw new TapestryException(StructureMessages.duplicateChildComponent(this, childId), child, null);

        _children.put(childId, child);
    }

    public void addMixin(Instantiator instantiator)
    {
        if (_mixinsByShortName == null)
        {
            _mixinsByShortName = newCaseInsensitiveMap();
            _components = newList();
        }

        String mixinClassName = instantiator.getModel().getComponentClassName();
        String mixinName = TapestryInternalUtils.lastTerm(mixinClassName);

        InternalComponentResourcesImpl resources = new InternalComponentResourcesImpl(_page, this, _coreResources,
                                                                                      instantiator, _pageResources);

        // TODO: Check for name collision?

        _mixinsByShortName.put(mixinName, resources);

        _components.add(resources.getComponent());
    }

    public void bindParameter(String parameterName, Binding binding)
    {
        // Maybe should use colon here? Depends on what works best in the template,
        // don't want to lock this out as just
        int dotx = parameterName.lastIndexOf('.');

        if (dotx > 0)
        {
            String mixinName = parameterName.substring(0, dotx);
            InternalComponentResources mixinResources = InternalUtils.get(_mixinsByShortName, mixinName);

            if (mixinResources == null) throw new TapestryException(
                    StructureMessages.missingMixinForParameter(_completeId, mixinName, parameterName), binding, null);

            String simpleName = parameterName.substring(dotx + 1);

            mixinResources.bindParameter(simpleName, binding);
            return;
        }

        InternalComponentResources informalParameterResources = null;

        // Does it match a formal parameter name of the core component? That takes precedence

        if (_coreResources.getComponentModel().getParameterModel(parameterName) != null)
        {
            _coreResources.bindParameter(parameterName, binding);
            return;
        }

        for (String mixinName : InternalUtils.sortedKeys(_mixinsByShortName))
        {
            InternalComponentResources resources = _mixinsByShortName.get(mixinName);
            if (resources.getComponentModel().getParameterModel(parameterName) != null)
            {
                resources.bindParameter(parameterName, binding);
                return;
            }

            if (informalParameterResources == null && resources.getComponentModel().getSupportsInformalParameters())
                informalParameterResources = resources;
        }

        // An informal parameter

        if (informalParameterResources == null && _coreResources.getComponentModel().getSupportsInformalParameters())
            informalParameterResources = _coreResources;

        // For the moment, informal parameters accumulate in the core component's resources, but
        // that will likely change.

        if (informalParameterResources != null) informalParameterResources.bindParameter(parameterName, binding);
    }

    public void addToBody(PageElement element)
    {
        if (_body == null) _body = newList();

        _body.add(element);
    }

    public void addToTemplate(PageElement element)
    {
        _template.add(element);
    }

    private void addUnboundParameterNames(String prefix, List<String> unbound, InternalComponentResources resource)
    {
        ComponentModel model = resource.getComponentModel();

        for (String name : model.getParameterNames())
        {
            if (resource.isBound(name)) continue;

            ParameterModel parameterModel = model.getParameterModel(name);

            if (parameterModel.isRequired())
            {
                String fullName = prefix == null ? name : prefix + "." + name;

                unbound.add(fullName);
            }
        }
    }

    public void containingPageDidAttach()
    {
        invoke(false, CONTAINING_PAGE_DID_ATTACH);
    }

    public void containingPageDidDetach()
    {
        invoke(false, CONTAINING_PAGE_DID_DETACH);
    }

    public void containingPageDidLoad()
    {
        // If this component has mixins, add the core component to the end of the list, after the
        // mixins.

        if (_components != null)
        {
            List<Component> ordered = newList();

            Iterator<Component> i = _components.iterator();

            // Add all the normal components to the final list.

            while (i.hasNext())
            {
                Component mixin = i.next();

                if (mixin.getComponentResources().getComponentModel().isMixinAfter()) continue;

                ordered.add(mixin);

                // Remove from list, leaving just the late executing mixins

                i.remove();
            }

            ordered.add(_coreComponent);

            // Add the remaining, late executing mixins

            ordered.addAll(_components);

            _components = ordered;
        }

        _loaded = true;

        // For some parameters, bindings (from defaults) are provided inside the callback method, so
        // that is invoked first, before we check for unbound parameters.

        invoke(false, CONTAINING_PAGE_DID_LOAD);

        verifyRequiredParametersAreBound();
    }


    public void enqueueBeforeRenderBody(RenderQueue queue)
    {
        // If no body, then no beforeRenderBody or afterRenderBody

        if (_body != null) queue.push(_beforeRenderBody);
    }

    public String getCompleteId()
    {
        return _completeId;
    }

    public Component getComponent()
    {
        return _coreComponent;
    }

    public InternalComponentResources getComponentResources()
    {
        return _coreResources;
    }

    public ComponentPageElement getContainerElement()
    {
        return _container;
    }

    public Page getContainingPage()
    {
        return _page;
    }

    public ComponentPageElement getEmbeddedElement(String embeddedId)
    {
        ComponentPageElement embeddedElement = InternalUtils.get(_children, embeddedId
                .toLowerCase());

        if (embeddedElement == null)
            throw new TapestryException(StructureMessages.noSuchComponent(this, embeddedId), this, null);

        return embeddedElement;
    }


    public String getId()
    {
        return _id;
    }

    public Logger getLogger()
    {
        return _coreResources.getLogger();
    }

    public Component getMixinByClassName(String mixinClassName)
    {
        Component result = null;

        if (_mixinsByShortName != null)
        {
            for (InternalComponentResources resources : _mixinsByShortName.values())
            {
                if (resources.getComponentModel().getComponentClassName().equals(mixinClassName))
                {
                    result = resources.getComponent();
                    break;
                }
            }
        }

        if (result == null) throw new TapestryException(StructureMessages.unknownMixin(_completeId, mixinClassName),
                                                        getLocation(), null);

        return result;
    }

    public String getNestedId()
    {
        return _nestedId;
    }

    public boolean dispatchEvent(ComponentEvent event)
    {
        if (_components == null) return _coreComponent.dispatchComponentEvent(event);

        // Otherwise, iterate over mixins + core component

        boolean result = false;

        for (Component component : _components)
        {
            result |= component.dispatchComponentEvent(event);

            if (event.isAborted()) break;
        }

        return result;
    }

    /**
     * Invokes a callback on the component instances (the core component plus any mixins).
     *
     * @param reverse  if true, the callbacks are in the reverse of the normal order (this is associated with AfterXXX
     *                 phases)
     * @param callback the object to receive each component instance
     */
    private void invoke(boolean reverse, ComponentCallback callback)
    {
        try
        { // Optimization: In the most general case (just the one component, no mixins)
            // invoke the callback on the component and be done ... no iterators, no nothing.

            if (_components == null)
            {
                callback.run(_coreComponent);
                return;
            }

            Iterator<Component> i = reverse ? InternalUtils.reverseIterator(_components) : _components.iterator();

            while (i.hasNext()) callback.run(i.next());
        }
        catch (RuntimeException ex)
        {
            throw new TapestryException(ex.getMessage(), getLocation(), ex);
        }
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    public boolean isRendering()
    {
        return _rendering;
    }

    /**
     * Generate a toString() for the inner classes that represent render phases.
     */
    private String phaseToString(String phaseName)
    {
        return String.format("%s[%s]", phaseName, _completeId);
    }

    /**
     * Pushes the SetupRender phase state onto the queue.
     */
    public final void render(MarkupWriter writer, RenderQueue queue)
    {
        // TODO: An error if the _render flag is already set (recursive rendering not
        // allowed or advisable).

        // Once we start rendering, the page is considered dirty, until we cleanup post render.

        _page.incrementDirtyCount();

        queue.push(_setupRender);
    }

    @Override
    public String toString()
    {
        return String.format("ComponentPageElement[%s]", _completeId);
    }

    public boolean triggerEvent(String eventType, Object[] context, ComponentEventCallback callback)
    {
        boolean result = false;

        ComponentPageElement component = this;
        String componentId = "";

        // Provide a default handler for when the provided handler is null.
        final ComponentEventCallback providedHandler = callback == null ? new NotificationEventCallback(eventType,
                                                                                                        _completeId) : callback;

        ComponentEventCallback wrapped = new ComponentEventCallback()
        {
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                // Boolean value is not passed to the handler; it will be true (abort event)
                // or false (continue looking for event handlers).

                if (result instanceof Boolean) return (Boolean) result;

                return providedHandler.handleResult(result, component, methodDescription);
            }
        };

        RuntimeException rootException = null;

        // Because I don't like to reassign parameters.

        String currentEventType = eventType;
        Object[] currentContext = context;

        while (component != null)
        {
            try
            {
                ComponentEvent event = new ComponentEventImpl(currentEventType, componentId, currentContext, wrapped,
                                                              _pageResources);

                result |= component.dispatchEvent(event);

                if (event.isAborted()) return result;
            }
            catch (RuntimeException ex)
            {
                // An exception in an event handler method
                // while we're trying to handle a previous exception!

                if (rootException != null) throw rootException;

                // We know component is not null and therefore has a component resources that
                // should have a location.

                Location location = component.getComponentResources().getLocation();

                // Wrap it up to help ensure that a location is available to the event handler method or,
                // more likely, to the exception report page.

                rootException = new ComponentEventException(ex.getMessage(), eventType, context, location, ex);

                // Switch over to triggering an "exception" event, starting in the component that
                // threw the exception.

                currentEventType = "exception";
                currentContext = new Object[]{rootException};

                continue;
            }

            // On each bubble up, make the event appear to come from the previous component
            // in which the event was triggered.

            componentId = component.getId();

            component = component.getContainerElement();
        }

        // If there was a handler for the exception event, it is required to return a non-null (and non-boolean) value
        // to tell Tapestry what to do.  Since that didn't happen, we have no choice but to rethrow the (wrapped)
        // exception.

        if (rootException != null) throw rootException;

        return result;
    }

    private void verifyRequiredParametersAreBound()
    {
        List<String> unbound = newList();

        addUnboundParameterNames(null, unbound, _coreResources);

        for (String name : InternalUtils.sortedKeys(_mixinsByShortName))
            addUnboundParameterNames(name, unbound, _mixinsByShortName.get(name));

        if (unbound.isEmpty()) return;

        throw new TapestryException(StructureMessages.missingParameters(unbound, this), this, null);
    }

    public Locale getLocale()
    {
        return _page.getLocale();
    }

    public String getElementName()
    {
        return _elementName;
    }

    public Block getBlock(String id)
    {
        Block result = findBlock(id);

        if (result == null)
            throw new BlockNotFoundException(StructureMessages.blockNotFound(_completeId, id), getLocation());

        return result;
    }

    public Block findBlock(String id)
    {
        notBlank(id, "id");

        return InternalUtils.get(_blocks, id);
    }

    public void addBlock(String blockId, Block block)
    {
        if (_blocks == null) _blocks = newCaseInsensitiveMap();

        if (_blocks.containsKey(blockId))
            throw new TapestryException(StructureMessages.duplicateBlock(this, blockId), block, null);

        _blocks.put(blockId, block);
    }

    public String getDefaultBindingPrefix(String parameterName)
    {
        int dotx = parameterName.lastIndexOf('.');

        if (dotx > 0)
        {
            String mixinName = parameterName.substring(0, dotx);
            InternalComponentResources mixinResources = InternalUtils.get(_mixinsByShortName, mixinName);

            if (mixinResources == null) throw new TapestryException(
                    StructureMessages.missingMixinForParameter(_completeId, mixinName, parameterName), null, null);

            String simpleName = parameterName.substring(dotx + 1);

            ParameterModel pm = mixinResources.getComponentModel().getParameterModel(simpleName);

            return pm != null ? pm.getDefaultBindingPrefix() : null;
        }

        // A formal parameter of the core component?

        ParameterModel pm = _coreResources.getComponentModel().getParameterModel(parameterName);

        if (pm != null) return pm.getDefaultBindingPrefix();

        // Search for mixin that it is a formal parameter of

        for (String mixinName : InternalUtils.sortedKeys(_mixinsByShortName))
        {
            InternalComponentResources resources = _mixinsByShortName.get(mixinName);

            pm = resources.getComponentModel().getParameterModel(parameterName);

            if (pm != null) return pm.getDefaultBindingPrefix();
        }

        // Not a formal parameter of the core component or any mixin.

        return null;
    }

    public String getPageName()
    {
        return _page.getLogicalName();
    }

    public Map<String, Binding> getInformalParameterBindings()
    {
        return _coreResources.getInformalParameterBindings();
    }
}
