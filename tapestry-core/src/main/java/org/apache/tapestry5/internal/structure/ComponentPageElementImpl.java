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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.ComponentEventImpl;
import org.apache.tapestry5.internal.services.EventImpl;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.util.NotificationEventCallback;
import org.apache.tapestry5.ioc.BaseLocatable;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.ParameterModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.*;
import org.slf4j.Logger;

import java.util.*;

/**
 * Implements {@link RenderCommand} and represents a component within an overall page. Much of a component page
 * element's behavior is delegated to user code, via a {@link org.apache.tapestry5.runtime.Component} instance.
 * <p/>
 * Once instantiated, a ComponentPageElement should be registered as a {@linkplain
 * org.apache.tapestry5.internal.structure.Page#addLifecycleListener(org.apache.tapestry5.runtime.PageLifecycleListener)
 * lifecycle listener}. This could be done inside the constructors, but that tends to complicate unit tests, so its done
 * by {@link org.apache.tapestry5.internal.services.PageElementFactoryImpl}.
 */
public class ComponentPageElementImpl extends BaseLocatable implements ComponentPageElement, PageLifecycleListener
{
    /**
     * Placeholder for the body used when the component has no real content.
     */
    private static class PlaceholderBlock implements Block, Renderable
    {
        public void render(MarkupWriter writer)
        {
        }

        @Override
        public String toString()
        {
            return "<PlaceholderBlock>";
        }
    }

    private static final Block PLACEHOLDER_BLOCK = new PlaceholderBlock();


    private static final ComponentCallback RESTORE_STATE_BEFORE_PAGE_ATTACH = new LifecycleNotificationComponentCallback()
    {
        public void run(Component component)
        {
            component.restoreStateBeforePageAttach();
        }
    };

    private static final ComponentCallback CONTAINING_PAGE_DID_ATTACH = new LifecycleNotificationComponentCallback()
    {
        public void run(Component component)
        {
            component.containingPageDidAttach();
        }
    };

    private static final ComponentCallback CONTAINING_PAGE_DID_DETACH = new LifecycleNotificationComponentCallback()
    {
        public void run(Component component)
        {
            component.containingPageDidDetach();
        }
    };

    private static final ComponentCallback CONTAINING_PAGE_DID_LOAD = new LifecycleNotificationComponentCallback()
    {
        public void run(Component component)
        {
            component.containingPageDidLoad();
        }
    };

    private static final ComponentCallback POST_RENDER_CLEANUP = new LifecycleNotificationComponentCallback()
    {
        public void run(Component component)
        {
            component.postRenderCleanup();
        }
    };

    // For the moment, every component will have a template, even if it consists of
    // just a page element to queue up a BeforeRenderBody phase.

    private static void pushElements(RenderQueue queue, List<RenderCommand> list)
    {
        int count = size(list);
        for (int i = count - 1; i >= 0; i--)
            queue.push(list.get(i));
    }

    private static int size(List<?> list)
    {
        return list == null ? 0 : list.size();
    }

    private abstract class AbstractPhase extends AbstractComponentCallback implements RenderCommand
    {
        private final String name;

        private MarkupWriter writer;

        public AbstractPhase(String name)
        {
            super(sharedEvent);

            this.name = name;
        }

        @Override
        public String toString()
        {
            return phaseToString(name);
        }

        void reset(RenderQueue queue)
        {
            sharedEventHandler.queueCommands(queue);

            sharedEventHandler.reset();

            sharedEvent.reset();

            writer = null;
        }

        void callback(boolean reverse, MarkupWriter writer)
        {
            this.writer = writer;

            invoke(reverse, this);
        }

        public void run(Component component)
        {
            invokeComponent(component, writer, sharedEvent);
        }

        protected boolean getResult()
        {
            return sharedEventHandler.getResult();
        }

        protected abstract void invokeComponent(Component component, MarkupWriter writer, Event event);
    }

    private class SetupRenderPhase extends AbstractPhase
    {
        public SetupRenderPhase()
        {
            super("SetupRender");
        }

        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.setupRender(writer, event);
        }

        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            callback(false, writer);

            push(queue, getResult(), beginRenderPhase, cleanupRenderPhase);

            reset(queue);
        }
    }

    private class BeginRenderPhase extends AbstractPhase
    {
        private BeginRenderPhase()
        {
            super("BeginRender");
        }

        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.beginRender(writer, event);
        }

        public void render(final MarkupWriter writer, final RenderQueue queue)
        {
            callback(false, writer);

            push(queue, afterRenderPhase);
            push(queue, getResult(), beforeRenderTemplatePhase, null);

            reset(queue);
        }
    }

    /**
     * Replaces {@link org.apache.tapestry5.internal.structure.ComponentPageElementImpl.BeginRenderPhase} when there is
     * a handler for AfterRender but not BeginRender.
     */
    private class OptimizedBeginRenderPhase implements RenderCommand
    {
        public void render(MarkupWriter writer, RenderQueue queue)
        {
            push(queue, afterRenderPhase);
            push(queue, beforeRenderTemplatePhase);
        }

        @Override
        public String toString()
        {
            return phaseToString("OptimizedBeginRenderPhase");
        }
    }

    /**
     * Reponsible for rendering the component's template.  Even a component that doesn't have a template goes through
     * this phase, as a synthetic template (used to trigger the rendering of the component's body) will be supplied.
     */
    private class BeforeRenderTemplatePhase extends AbstractPhase
    {
        private BeforeRenderTemplatePhase()
        {
            super("BeforeRenderTemplate");
        }


        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.beforeRenderTemplate(writer, event);
        }

        public void render(final MarkupWriter writer, final RenderQueue queue)
        {
            callback(false, writer);

            push(queue, afterRenderTemplatePhase);

            if (getResult())
                pushElements(queue, template);

            reset(queue);
        }
    }

    /**
     * Alternative version of BeforeRenderTemplatePhase used when the BeforeRenderTemplate render phase is not handled.
     */
    private class RenderTemplatePhase implements RenderCommand
    {
        public void render(MarkupWriter writer, RenderQueue queue)
        {
            push(queue, afterRenderTemplatePhase);

            pushElements(queue, template);
        }

        @Override
        public String toString()
        {
            return phaseToString("RenderTemplate");
        }
    }

    private class BeforeRenderBodyPhase extends AbstractPhase
    {
        private BeforeRenderBodyPhase()
        {
            super("BeforeRenderBody");
        }

        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.beforeRenderBody(writer, event);
        }

        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            callback(false, writer);

            push(queue, afterRenderBodyPhase);

            if (getResult() && bodyBlock != null)
                queue.push(bodyBlock);

            reset(queue);
        }
    }


    private class AfterRenderBodyPhase extends AbstractPhase
    {

        private AfterRenderBodyPhase()
        {
            super("AfterRenderBody");
        }


        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.afterRenderBody(writer, event);
        }

        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            callback(true, writer);

            push(queue, getResult(), null, beforeRenderBodyPhase);

            reset(queue);
        }
    }

    private class AfterRenderTemplatePhase extends AbstractPhase
    {
        private AfterRenderTemplatePhase()
        {
            super("AfterRenderTemplate");
        }

        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.afterRenderTemplate(writer, event);
        }

        public void render(final MarkupWriter writer, final RenderQueue queue)
        {
            callback(true, writer);

            push(queue, getResult(), null, beforeRenderTemplatePhase);

            reset(queue);
        }
    }

    private class AfterRenderPhase extends AbstractPhase
    {
        private AfterRenderPhase()
        {
            super("AfterRender");
        }

        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.afterRender(writer, event);
        }

        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            callback(true, writer);

            push(queue, getResult(), cleanupRenderPhase, beginRenderPhase);

            reset(queue);
        }
    }


    private final ComponentPageElementResources elementResources;

    private class CleanupRenderPhase extends AbstractPhase
    {
        private CleanupRenderPhase()
        {
            super("CleanupRender");
        }

        protected void invokeComponent(Component component, MarkupWriter writer, Event event)
        {
            component.cleanupRender(writer, event);
        }

        public void render(final MarkupWriter writer, RenderQueue queue)
        {
            callback(true, writer);

            push(queue, getResult(), null, setupRenderPhase);

            reset(queue);
        }
    }

    private class PostRenderCleanupPhase implements RenderCommand
    {
        /**
         * Used to detect mismatches calls to {@link MarkupWriter#element(String, Object[])} } and {@link
         * org.apache.tapestry5.MarkupWriter#end()}.  The expectation is that any element(s) begun by this component
         * during rendering will be balanced by end() calls, resulting in the current element reverting to its initial
         * value.
         */
        private final Element expectedElementAtCompletion;

        PostRenderCleanupPhase(Element expectedElementAtCompletion)
        {
            this.expectedElementAtCompletion = expectedElementAtCompletion;
        }

        public void render(MarkupWriter writer, RenderQueue queue)
        {
            rendering = false;

            Element current = writer.getElement();

            if (current != expectedElementAtCompletion)
                throw new TapestryException(StructureMessages.unbalancedElements(completeId), getLocation(), null);

            invoke(false, POST_RENDER_CLEANUP);

            queue.endComponent();

            // Now and only now the component is done rendering and fully cleaned up. Decrement
            // the page's dirty count. If the entire render goes well, then the page will be
            // clean and can be stored into the pool for later reuse.

            page.decrementDirtyCount();
        }

        @Override
        public String toString()
        {
            return phaseToString("PostRenderCleanup");
        }
    }

    private Map<String, Block> blocks;

    private BlockImpl bodyBlock;

    private Map<String, ComponentPageElement> children;

    private final String elementName;

    private final Logger eventLogger;

    private final String completeId;

    // The user-provided class, with runtime code enhancements. In a component with mixins, this
    // is the component to which the mixins are attached.
    private final Component coreComponent;

    /**
     * Component lifecycle instances for all mixins; the core component is added to this list during page load. This is
     * only used in the case that a component has mixins (in which case, the core component is listed last).
     */
    private List<Component> components = null;

    private final ComponentPageElement container;

    private final InternalComponentResources coreResources;

    private final String id;

    private boolean loaded;

    /**
     * Map from mixin id (the simple name of the mixin class) to resources for the mixin. Created when first mixin is
     * added.
     */
    private Map<String, InternalComponentResources> mixinIdToComponentResources;

    private final String nestedId;

    private final Page page;

    private boolean rendering;


    // We know that, at the very least, there will be an element to force the component to render
    // its body, so there's no reason to wait to initialize the list.

    private final List<RenderCommand> template = CollectionFactory.newList();

    private boolean renderPhasesInitalized;

    private RenderCommand setupRenderPhase, beginRenderPhase, beforeRenderTemplatePhase, beforeRenderBodyPhase,
            afterRenderBodyPhase, afterRenderTemplatePhase, afterRenderPhase, cleanupRenderPhase;


    private final RenderPhaseEventHandler sharedEventHandler = new RenderPhaseEventHandler();

    private final EventImpl sharedEvent;

    /**
     * Constructor for other components embedded within the root component or at deeper levels of the hierarchy.
     *
     * @param page             ultimately containing this component
     * @param container        component immediately containing this component (may be null for a root component)
     * @param id               unique (within the container) id for this component (may be null for a root component)
     * @param elementName      the name of the element which represents this component in the template, or null for
     *                         &lt;comp&gt; element or a page component
     * @param instantiator     used to create the new component instance and access the component's model
     * @param location         location of the element (within a template), used as part of exception reporting
     * @param elementResources Provides access to common methods of various services
     */

    ComponentPageElementImpl(Page page,
                             ComponentPageElement container,
                             String id,
                             String nestedId,
                             String completeId,
                             String elementName,
                             Instantiator instantiator,
                             Location location,
                             ComponentPageElementResources elementResources)
    {
        super(location);

        this.page = page;
        this.container = container;
        this.id = id;
        this.nestedId = nestedId;
        this.completeId = completeId;
        this.elementName = elementName;
        this.elementResources = elementResources;

        ComponentResources containerResources = container == null
                                                ? null
                                                : container.getComponentResources();


        coreResources = new InternalComponentResourcesImpl(this.page, this, containerResources,
                                                           this.elementResources,
                                                           completeId, nestedId, instantiator);

        coreComponent = coreResources.getComponent();

        Logger logger = coreResources.getLogger();
        eventLogger = elementResources.getEventLogger(logger);

        sharedEvent = new EventImpl(sharedEventHandler, eventLogger);
    }

    /**
     * Constructor for the root component of a page.
     */
    public ComponentPageElementImpl(Page page,
                                    Instantiator instantiator,
                                    ComponentPageElementResources elementResources)
    {
        this(page, null, null, null, page.getName(), null, instantiator, null,
             elementResources);
    }

    private void initializeRenderPhases()
    {
        setupRenderPhase = new SetupRenderPhase();
        beginRenderPhase = new BeginRenderPhase();
        beforeRenderTemplatePhase = new BeforeRenderTemplatePhase();
        beforeRenderBodyPhase = new BeforeRenderBodyPhase();
        afterRenderBodyPhase = new AfterRenderBodyPhase();
        afterRenderTemplatePhase = new AfterRenderTemplatePhase();
        afterRenderPhase = new AfterRenderPhase();
        cleanupRenderPhase = new CleanupRenderPhase();


        // Now the optimization, where we remove, replace and collapse unused phases. We use
        // the component models to determine which phases have handler methods for the
        // render phases.

        Set<Class> handled = coreResources.getComponentModel().getHandledRenderPhases();

        if (mixinIdToComponentResources != null)
        {
            for (ComponentResources r : mixinIdToComponentResources.values())
                handled.addAll(r.getComponentModel().getHandledRenderPhases());
        }

        if (!handled.contains(CleanupRender.class)) cleanupRenderPhase = null;

        // Now, work back to front.

        if (!handled.contains(AfterRender.class))
            afterRenderPhase = cleanupRenderPhase;

        if (!handled.contains(AfterRenderTemplate.class))
            afterRenderTemplatePhase = null;

        if (!handled.contains(AfterRenderBody.class))
            afterRenderBodyPhase = null;

        if (!handled.contains(BeforeRenderTemplate.class))
            beforeRenderTemplatePhase = new RenderTemplatePhase();

        if (!handled.contains(BeginRender.class))
        {
            RenderCommand replacement = afterRenderPhase != null
                                        ? new OptimizedBeginRenderPhase()
                                        : beforeRenderTemplatePhase;

            beginRenderPhase = replacement;
        }

        if (!handled.contains(SetupRender.class))
            setupRenderPhase = beginRenderPhase;

        renderPhasesInitalized = true;
    }

    public ComponentPageElement newChild(String id, String nestedId, String completeId, String elementName,
                                         Instantiator instantiator,
                                         Location location)
    {
        ComponentPageElementImpl child = new ComponentPageElementImpl(page,
                                                                      this,
                                                                      id,
                                                                      nestedId,
                                                                      completeId,
                                                                      elementName,
                                                                      instantiator,
                                                                      location,
                                                                      elementResources);

        addEmbeddedElement(child);

        return child;
    }

    void push(RenderQueue queue, boolean forward, RenderCommand forwardPhase, RenderCommand backwardPhase)
    {
        push(queue, forward ? forwardPhase : backwardPhase);
    }

    void push(RenderQueue queue, RenderCommand nextPhase)
    {
        if (nextPhase != null)
            queue.push(nextPhase);
    }

    void addEmbeddedElement(ComponentPageElement child)
    {
        if (children == null) children = CollectionFactory.newCaseInsensitiveMap();

        String childId = child.getId();

        ComponentPageElement existing = children.get(childId);

        if (existing != null)
            throw new TapestryException(StructureMessages.duplicateChildComponent(this, childId), child,
                                        new TapestryException(StructureMessages.originalChildComponent(this, childId,
                                                                                                       existing.getLocation()),
                                                              existing, null));

        children.put(childId, child);
    }

    public void addMixin(String mixinId, Instantiator instantiator)
    {
        if (mixinIdToComponentResources == null)
        {
            mixinIdToComponentResources = CollectionFactory.newCaseInsensitiveMap();
            components = CollectionFactory.newList();
        }

        String mixinExtension = "$" + mixinId.toLowerCase();

        InternalComponentResourcesImpl resources = new InternalComponentResourcesImpl(page, this, coreResources,
                                                                                      elementResources,
                                                                                      completeId + mixinExtension,
                                                                                      nestedId + mixinExtension,
                                                                                      instantiator);

        mixinIdToComponentResources.put(mixinId, resources);

        components.add(resources.getComponent());
    }

    public void bindMixinParameter(String mixinId, String parameterName, Binding binding)
    {
        InternalComponentResources mixinResources = InternalUtils.get(mixinIdToComponentResources, mixinId);

        mixinResources.bindParameter(parameterName, binding);
    }

    public Binding getBinding(String parameterName)
    {
        return coreResources.getBinding(parameterName);
    }

    public void bindParameter(String parameterName, Binding binding)
    {
        coreResources.bindParameter(parameterName, binding);
    }

    public void addToBody(RenderCommand element)
    {
        if (bodyBlock == null) bodyBlock = new BlockImpl(getLocation(), "Body of " + getCompleteId());

        bodyBlock.addToBody(element);
    }

    public void addToTemplate(RenderCommand element)
    {
        template.add(element);
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

    public void restoreStateBeforePageAttach()
    {
        invoke(false, RESTORE_STATE_BEFORE_PAGE_ATTACH);
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

        if (components != null)
        {
            List<Component> ordered = CollectionFactory.newList();

            Iterator<Component> i = components.iterator();

            // Add all the normal components to the final list.

            while (i.hasNext())
            {
                Component mixin = i.next();

                if (mixin.getComponentResources().getComponentModel().isMixinAfter()) continue;

                ordered.add(mixin);

                // Remove from list, leaving just the late executing mixins

                i.remove();
            }

            ordered.add(coreComponent);

            // Add the remaining, late executing mixins

            ordered.addAll(components);

            components = ordered;
        }

        loaded = true;

        // For some parameters, bindings (from defaults) are provided inside the callback method, so
        // that is invoked first, before we check for unbound parameters.

        invoke(false, CONTAINING_PAGE_DID_LOAD);

        verifyRequiredParametersAreBound();
    }


    public void enqueueBeforeRenderBody(RenderQueue queue)
    {
        // If no body, then no beforeRenderBody or afterRenderBody

        if (bodyBlock != null)
            push(queue, beforeRenderBodyPhase);
    }

    public String getCompleteId()
    {
        return completeId;
    }

    public Component getComponent()
    {
        return coreComponent;
    }

    public InternalComponentResources getComponentResources()
    {
        return coreResources;
    }

    public ComponentPageElement getContainerElement()
    {
        return container;
    }

    public Page getContainingPage()
    {
        return page;
    }

    public ComponentPageElement getEmbeddedElement(String embeddedId)
    {
        ComponentPageElement embeddedElement = InternalUtils.get(children, embeddedId);

        if (embeddedElement == null)
        {
            Set<String> ids = InternalUtils.keys(children);

            throw new TapestryException(StructureMessages.noSuchComponent(this, embeddedId, ids), this, null);
        }

        return embeddedElement;
    }

    public String getId()
    {
        return id;
    }


    public Logger getLogger()
    {
        return coreResources.getLogger();
    }

    public Component getMixinByClassName(String mixinClassName)
    {
        Component result = null;

        if (mixinIdToComponentResources != null)
        {
            for (InternalComponentResources resources : mixinIdToComponentResources.values())
            {
                if (resources.getComponentModel().getComponentClassName().equals(mixinClassName))
                {
                    result = resources.getComponent();
                    break;
                }
            }
        }

        if (result == null) throw new TapestryException(StructureMessages.unknownMixin(completeId, mixinClassName),
                                                        getLocation(), null);

        return result;
    }

    public ComponentResources getMixinResources(String mixinId)
    {
        ComponentResources result = null;

        if (mixinIdToComponentResources != null)
            result = mixinIdToComponentResources.get(mixinId);

        if (result == null)
            throw new IllegalArgumentException(
                    String.format("Unable to locate mixin '%s' for component '%s'.", mixinId, completeId));

        return result;
    }

    public String getNestedId()
    {
        return nestedId;
    }

    public boolean dispatchEvent(ComponentEvent event)
    {
        if (components == null)
            return coreComponent.dispatchComponentEvent(event);

        // Otherwise, iterate over mixins + core component

        boolean result = false;

        for (Component component : components)
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

            if (components == null)
            {
                callback.run(coreComponent);
                return;
            }

            Iterator<Component> i = reverse ? InternalUtils.reverseIterator(components) : components.iterator();

            while (i.hasNext())
            {
                callback.run(i.next());

                if (callback.isEventAborted()) return;
            }
        }
        catch (RuntimeException ex)
        {
            throw new TapestryException(ex.getMessage(), getLocation(), ex);
        }
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    public boolean isRendering()
    {
        return rendering;
    }

    /**
     * Generate a toString() for the inner classes that represent render phases.
     */
    private String phaseToString(String phaseName)
    {
        return String.format("%s[%s]", phaseName, completeId);
    }


    /**
     * Pushes the SetupRender phase state onto the queue.
     */
    public final void render(MarkupWriter writer, RenderQueue queue)
    {
        // We assume that by the time we start to render, the structure (i.e., mixins) is nailed down.
        // We could add a lock, but that seems wasteful.

        if (!renderPhasesInitalized)
            initializeRenderPhases();

        // TODO: An error if the render flag is already set (recursive rendering not
        // allowed or advisable).

        // Once we start rendering, the page is considered dirty, until we cleanup post render.

        page.incrementDirtyCount();

        // TODO: Check for recursive rendering.

        rendering = true;

        queue.startComponent(coreResources);

        queue.push(new PostRenderCleanupPhase(writer.getElement()));

        push(queue, setupRenderPhase);
    }

    @Override
    public String toString()
    {
        return String.format("ComponentPageElement[%s]", completeId);
    }

    public boolean triggerEvent(String eventType, Object[] contextValues, ComponentEventCallback callback)
    {
        return triggerContextEvent(eventType,
                                   createParameterContext(contextValues == null ? new Object[0] : contextValues),
                                   callback);
    }

    private EventContext createParameterContext(final Object... values)
    {
        return new EventContext()
        {
            public int getCount()
            {
                return values.length;
            }

            public <T> T get(Class<T> desiredType, int index)
            {
                return elementResources.coerce(values[index], desiredType);
            }
        };
    }


    public boolean triggerContextEvent(String eventType, EventContext context, ComponentEventCallback callback)
    {
        Defense.notBlank(eventType, "eventType");
        Defense.notNull(context, "context");

        boolean result = false;

        ComponentPageElement component = this;
        String componentId = "";

        // Provide a default handler for when the provided handler is null.
        final ComponentEventCallback providedHandler = callback == null ? new NotificationEventCallback(eventType,
                                                                                                        completeId) : callback;

        ComponentEventCallback wrapped = new ComponentEventCallback()
        {
            public boolean handleResult(Object result)
            {
                // Boolean value is not passed to the handler; it will be true (abort event)
                // or false (continue looking for event handlers).

                if (result instanceof Boolean) return (Boolean) result;

                return providedHandler.handleResult(result);
            }
        };

        RuntimeException rootException = null;

        // Because I don't like to reassign parameters.

        String currentEventType = eventType;
        EventContext currentContext = context;

        // Track the location of the original component for the event, even as we work our way up
        // the hierarchy. This may not be ideal if we trigger an "exception" event ... or maybe
        // it's right (it's the location of the originally thrown exception).

        Location location = component.getComponentResources().getLocation();

        while (component != null)
        {
            try
            {
                Logger logger = component.getEventLogger();

                ComponentEvent event = new ComponentEventImpl(currentEventType, componentId, currentContext, wrapped,
                                                              elementResources, logger);

                logger.debug(TapestryMarkers.EVENT_DISPATCH, "Dispatch event: {}", event);

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

                // Wrap it up to help ensure that a location is available to the event handler method or,
                // more likely, to the exception report page.

                rootException = new ComponentEventException(ex.getMessage(), eventType, context, location, ex);

                // Switch over to triggering an "exception" event, starting in the component that
                // threw the exception.

                currentEventType = "exception";
                currentContext = createParameterContext(rootException);

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
        List<String> unbound = CollectionFactory.newList();

        addUnboundParameterNames(null, unbound, coreResources);

        for (String name : InternalUtils.sortedKeys(mixinIdToComponentResources))
            addUnboundParameterNames(name, unbound, mixinIdToComponentResources.get(name));

        if (unbound.isEmpty()) return;

        throw new TapestryException(StructureMessages.missingParameters(unbound, this), this, null);
    }

    public Locale getLocale()
    {
        return page.getLocale();
    }

    public String getElementName(String defaultElementName)
    {
        return elementName != null ? elementName : defaultElementName;
    }

    public Block getBlock(String id)
    {
        Block result = findBlock(id);

        if (result == null)
            throw new BlockNotFoundException(StructureMessages.blockNotFound(completeId, id), getLocation());

        return result;
    }

    public Block findBlock(String id)
    {
        notBlank(id, "id");

        return InternalUtils.get(blocks, id);
    }

    public void addBlock(String blockId, Block block)
    {
        if (blocks == null) blocks = CollectionFactory.newCaseInsensitiveMap();

        if (blocks.containsKey(blockId))
            throw new TapestryException(StructureMessages.duplicateBlock(this, blockId), block, null);

        blocks.put(blockId, block);
    }

    public String getPageName()
    {
        return page.getName();
    }

    public boolean hasBody()
    {
        return bodyBlock != null;
    }

    public Block getBody()
    {
        return bodyBlock == null ? PLACEHOLDER_BLOCK : bodyBlock;
    }

    public Map<String, Binding> getInformalParameterBindings()
    {
        return coreResources.getInformalParameterBindings();
    }

    public Logger getEventLogger()
    {
        return eventLogger;
    }

    public Link createEventLink(String eventType, Object... context)
    {
        return elementResources.createComponentEventLink(coreResources, eventType, false, context);
    }

    public Link createActionLink(String eventType, boolean forForm, Object... context)
    {
        return elementResources.createComponentEventLink(coreResources, eventType, forForm, context);
    }

    public Link createFormEventLink(String eventType, Object... context)
    {
        return elementResources.createComponentEventLink(coreResources, eventType, true, context);
    }

    public Link createPageLink(String pageName, boolean override, Object... context)
    {
        return elementResources.createPageRenderLink(pageName, override, context);
    }

    public Link createPageLink(Class pageClass, boolean override, Object... context)
    {
        return elementResources.createPageRenderLink(pageClass, override, context);
    }


}
