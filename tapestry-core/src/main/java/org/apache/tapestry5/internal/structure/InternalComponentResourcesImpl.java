// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.bindings.InternalPropBinding;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.transform.ParameterConduit;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.NullAnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.runtime.RenderQueue;
import org.slf4j.Logger;

/**
 * The bridge between a component and its {@link ComponentPageElement}, that supplies all kinds of
 * resources to the
 * component, including access to its parameters, parameter bindings, and persistent field data.
 */
public class InternalComponentResourcesImpl implements InternalComponentResources
{
    private final Page page;

    private final String completeId;

    private final String nestedId;

    private final ComponentModel componentModel;

    private final ComponentPageElement element;

    private final Component component;

    private final ComponentResources containerResources;

    private final ComponentPageElementResources elementResources;

    // Case insensitive map from parameter name to binding
    private Map<String, Binding> bindings;

    // Case insensitive map from parameter name to ParameterConduit, used to support mixins
    // which need access to the containing component's PC's
    private Map<String, ParameterConduit> conduits;

    private Messages messages;

    // Case insensitive
    private Map<String, Object> renderVariables;

    private static final Object[] EMPTY = new Object[0];

    private static final AnnotationProvider NULL_ANNOTATION_PROVIDER = new NullAnnotationProvider();

    private boolean informalsComputed;

    private final boolean mixin;

    /**
     * We keep a linked list of informal parameters, which saves us the expense of determining which
     * bindings are formal
     * and which are informal. Each Informal points to the next.
     */
    private class Informal
    {
        private final String name;

        private final Binding binding;

        final Informal next;

        private Informal(String name, Binding binding, Informal next)
        {
            this.name = name;
            this.binding = binding;
            this.next = next;
        }

        void write(MarkupWriter writer)
        {
            Object value = binding.get();

            if (value == null)
                return;

            if (value instanceof Block)
                return;

            // If it's already a String, don't use the TypeCoercer (renderInformalParameters is
            // a CPU hotspot, as is TypeCoercer.coerce).

            String valueString = value instanceof String ? (String) value : elementResources
                    .coerce(value, String.class);

            writer.attributes(name, valueString);
        }
    }

    private Informal firstInformal;

    public InternalComponentResourcesImpl(Page page, ComponentPageElement element,
            ComponentResources containerResources, ComponentPageElementResources elementResources, String completeId,
            String nestedId, Instantiator componentInstantiator, boolean mixin)
    {
        this.page = page;
        this.element = element;
        this.containerResources = containerResources;
        this.elementResources = elementResources;
        this.completeId = completeId;
        this.nestedId = nestedId;
        this.mixin = mixin;

        componentModel = componentInstantiator.getModel();
        component = componentInstantiator.newInstance(this);
    }

    public boolean isMixin()
    {
        return mixin;
    }

    public Location getLocation()
    {
        return element.getLocation();
    }

    public String toString()
    {
        return String.format("InternalComponentResources[%s]", getCompleteId());
    }

    public ComponentModel getComponentModel()
    {
        return componentModel;
    }

    public Component getEmbeddedComponent(String embeddedId)
    {
        return element.getEmbeddedElement(embeddedId).getComponent();
    }

    public Object getFieldChange(String fieldName)
    {
        return page.getFieldChange(nestedId, fieldName);
    }

    public String getId()
    {
        return element.getId();
    }

    public boolean hasFieldChange(String fieldName)
    {
        return getFieldChange(fieldName) != null;
    }

    public Link createEventLink(String eventType, Object... context)
    {
        return element.createEventLink(eventType, context);
    }

    public Link createActionLink(String eventType, boolean forForm, Object... context)
    {
        return element.createActionLink(eventType, forForm, context);
    }

    public Link createFormEventLink(String eventType, Object... context)
    {
        return element.createFormEventLink(eventType, context);
    }

    public Link createPageLink(String pageName, boolean override, Object... context)
    {
        return element.createPageLink(pageName, override, context);
    }

    public Link createPageLink(Class pageClass, boolean override, Object... context)
    {
        return element.createPageLink(pageClass, override, context);
    }

    public void discardPersistentFieldChanges()
    {
        page.discardPersistentFieldChanges();
    }

    public String getElementName()
    {
        return getElementName(null);
    }

    public List<String> getInformalParameterNames()
    {
        return InternalUtils.sortedKeys(getInformalParameterBindings());
    }

    public <T> T getInformalParameter(String name, Class<T> type)
    {
        Binding binding = getBinding(name);

        Object value = binding == null ? null : binding.get();

        return elementResources.coerce(value, type);
    }

    public Block getBody()
    {
        return element.getBody();
    }

    public boolean hasBody()
    {
        return element.hasBody();
    }

    public String getCompleteId()
    {
        return completeId;
    }

    public Component getComponent()
    {
        return component;
    }

    public boolean isBound(String parameterName)
    {
        return getBinding(parameterName) != null;
    }

    public <T extends Annotation> T getParameterAnnotation(String parameterName, Class<T> annotationType)
    {
        Binding binding = getBinding(parameterName);

        return binding == null ? null : binding.getAnnotation(annotationType);
    }

    public boolean isRendering()
    {
        return element.isRendering();
    }

    public boolean triggerEvent(String eventType, Object[] context, ComponentEventCallback handler)
    {
        return element.triggerEvent(eventType, defaulted(context), handler);
    }

    private static Object[] defaulted(Object[] input)
    {
        return input == null ? EMPTY : input;
    }

    public boolean triggerContextEvent(String eventType, EventContext context, ComponentEventCallback callback)
    {
        return element.triggerContextEvent(eventType, context, callback);
    }

    public String getNestedId()
    {
        return nestedId;
    }

    public Component getPage()
    {
        return element.getContainingPage().getRootComponent();
    }

    public boolean isLoaded()
    {
        return element.isLoaded();
    }

    public void persistFieldChange(String fieldName, Object newValue)
    {
        try
        {
            page.persistFieldChange(this, fieldName, newValue);
        }
        catch (Exception ex)
        {
            throw new TapestryException(StructureMessages.fieldPersistFailure(getCompleteId(), fieldName, ex),
                    getLocation(), ex);
        }
    }

    public void bindParameter(String parameterName, Binding binding)
    {
        if (bindings == null)
            bindings = CollectionFactory.newCaseInsensitiveMap();

        bindings.put(parameterName, binding);
    }

    public Class getBoundType(String parameterName)
    {
        Binding binding = getBinding(parameterName);

        return binding == null ? null : binding.getBindingType();
    }

    public Binding getBinding(String parameterName)
    {
        return InternalUtils.get(bindings, parameterName);
    }

    public AnnotationProvider getAnnotationProvider(String parameterName)
    {
        Binding binding = getBinding(parameterName);

        return binding == null ? NULL_ANNOTATION_PROVIDER : binding;
    }

    public Logger getLogger()
    {
        return componentModel.getLogger();
    }

    public Component getMixinByClassName(String mixinClassName)
    {
        return element.getMixinByClassName(mixinClassName);
    }

    public void renderInformalParameters(MarkupWriter writer)
    {
        if (bindings == null)
            return;

        if (!informalsComputed)
        {
            for (Map.Entry<String, Binding> e : getInformalParameterBindings().entrySet())
            {
                firstInformal = new Informal(e.getKey(), e.getValue(), firstInformal);
            }

            informalsComputed = true;
        }

        for (Informal i = firstInformal; i != null; i = i.next)
            i.write(writer);
    }

    public Component getContainer()
    {
        if (containerResources == null)
            return null;

        return containerResources.getComponent();
    }

    public ComponentResources getContainerResources()
    {
        return containerResources;
    }

    public Messages getContainerMessages()
    {
        return containerResources != null ? containerResources.getMessages() : null;
    }

    public Locale getLocale()
    {
        return element.getLocale();
    }

    public Messages getMessages()
    {
        if (messages == null)
            messages = elementResources.getMessages(componentModel);

        return messages;
    }

    public String getElementName(String defaultElementName)
    {
        return element.getElementName(defaultElementName);
    }

    public void queueRender(RenderQueue queue)
    {
        queue.push(element);
    }

    public Block getBlock(String blockId)
    {
        return element.getBlock(blockId);
    }

    public Block getBlockParameter(String parameterName)
    {
        return getInformalParameter(parameterName, Block.class);
    }

    public Block findBlock(String blockId)
    {
        return element.findBlock(blockId);
    }

    public Resource getBaseResource()
    {
        return componentModel.getBaseResource();
    }

    public String getPageName()
    {
        return element.getPageName();
    }

    public Map<String, Binding> getInformalParameterBindings()
    {
        Map<String, Binding> result = CollectionFactory.newMap();

        if (bindings != null)
        {
            for (String name : bindings.keySet())
            {

                if (componentModel.getParameterModel(name) != null)
                    continue;

                result.put(name, bindings.get(name));
            }
        }

        return result;
    }

    public Object getRenderVariable(String name)
    {
        Object result = InternalUtils.get(renderVariables, name);

        if (result == null)
            throw new IllegalArgumentException(StructureMessages.missingRenderVariable(getCompleteId(), name,
                    renderVariables == null ? null : renderVariables.keySet()));

        return result;
    }

    public void storeRenderVariable(String name, Object value)
    {
        Defense.notBlank(name, "name");
        Defense.notNull(value, "value");

        if (!element.isRendering())
            throw new IllegalStateException(StructureMessages.renderVariableSetWhenNotRendering(getCompleteId(), name));

        if (renderVariables == null)
            renderVariables = CollectionFactory.newCaseInsensitiveMap();

        renderVariables.put(name, value);
    }

    public void postRenderCleanup()
    {
        if (renderVariables != null)
            renderVariables.clear();
    }

    public void addPageLifecycleListener(PageLifecycleListener listener)
    {
        page.addLifecycleListener(listener);
    }

    public void removePageLifecycleListener(PageLifecycleListener listener)
    {
        page.removeLifecycleListener(listener);
    }

    public void addPageResetListener(PageResetListener listener)
    {
        page.addResetListener(listener);
    }

    public ParameterConduit getParameterConduit(String parameterName)
    {
        return InternalUtils.get(conduits, parameterName);
    }

    public void setParameterConduit(String parameterName, ParameterConduit conduit)
    {
        if (conduits == null)
            conduits = CollectionFactory.newCaseInsensitiveMap();

        conduits.put(parameterName, conduit);
    }

    public String getPropertyName(String parameterName)
    {
        Binding binding = getBinding(parameterName);

        if (binding == null)
            return null;

        if (binding instanceof InternalPropBinding) { return ((InternalPropBinding) binding).getPropertyName(); }

        return null;
    }

}
