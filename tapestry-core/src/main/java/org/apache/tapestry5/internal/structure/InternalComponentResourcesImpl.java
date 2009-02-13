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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.*;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.ParameterAccess;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.ParameterModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.runtime.RenderQueue;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The bridge between a component and its {@link ComponentPageElement}, that supplies all kinds of resources to the
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

    // Case insensitive map from parameter name to ParameterAccess
    private Map<String, ParameterAccess> access;

    private Messages messages;

    // Case insensitive
    private Map<String, Object> renderVariables;

    private static final Object[] EMPTY = new Object[0];

    private boolean informalsComputed;

    /**
     * We keep a linked list of informal parameters, which saves us the expense of determining which bindings are formal
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

            if (value == null) return;

            if (value instanceof Block) return;

            // If it's already a String, don't use the TypeCoercer (renderInformalParameters is
            // a CPU hotspot, as is TypeCoercer.coerce).

            String valueString = value instanceof String
                                 ? (String) value
                                 : elementResources.coerce(value, String.class);

            writer.attributes(name, valueString);
        }
    }

    private Informal firstInformal;

    public InternalComponentResourcesImpl(Page page, ComponentPageElement element,
                                          ComponentResources containerResources,
                                          ComponentPageElementResources elementResources,
                                          String completeId, String nestedId, Instantiator componentInstantiator
    )
    {
        this.page = page;
        this.element = element;
        this.containerResources = containerResources;
        this.elementResources = elementResources;
        this.completeId = completeId;
        this.nestedId = nestedId;

        componentModel = componentInstantiator.getModel();
        component = componentInstantiator.newInstance(this);
    }

    public Location getLocation()
    {
        return element.getLocation();
    }

    @Override
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
        return getParameterAccess(name).read(type);
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
        return getParameterAccess(parameterName).getAnnotation(annotationType);
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
        if (bindings == null) bindings = CollectionFactory.newCaseInsensitiveMap();

        bindings.put(parameterName, binding);
    }


    public Class getBoundType(String parameterName)
    {
        return getParameterAccess(parameterName).getBoundType();
    }

    public Binding getBinding(String parameterName)
    {
        return InternalUtils.get(bindings, parameterName);
    }

    public AnnotationProvider getAnnotationProvider(String parameterName)
    {
        return getParameterAccess(parameterName);
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
        if (bindings == null) return;

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
        if (containerResources == null) return null;

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
        if (messages == null) messages = elementResources.getMessages(componentModel);

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

                if (componentModel.getParameterModel(name) != null) continue;

                result.put(name, bindings.get(name));
            }
        }

        return result;
    }

    public Object getRenderVariable(String name)
    {
        Object result = InternalUtils.get(renderVariables, name);

        if (result == null)
            throw new IllegalArgumentException(StructureMessages.missingRenderVariable(getCompleteId(),
                                                                                       name,
                                                                                       renderVariables == null ? null : renderVariables.keySet()));

        return result;
    }

    public void storeRenderVariable(String name, Object value)
    {
        Defense.notBlank(name, "name");
        Defense.notNull(value, "value");

        if (!element.isRendering())
            throw new IllegalStateException(StructureMessages.renderVariableSetWhenNotRendering(getCompleteId(), name));

        if (renderVariables == null) renderVariables = CollectionFactory.newCaseInsensitiveMap();

        renderVariables.put(name, value);
    }

    public void postRenderCleanup()
    {
        if (renderVariables != null) renderVariables.clear();
    }

    public void addPageLifecycleListener(PageLifecycleListener listener)
    {
        page.addLifecycleListener(listener);
    }

    public ParameterAccess getParameterAccess(final String parameterName)
    {
        if (access == null) access = CollectionFactory.newCaseInsensitiveMap();

        ParameterAccess result = access.get(parameterName);

        if (result == null)
        {
            result = createParameterAccess(parameterName);
            access.put(parameterName, result);
        }

        return result;
    }

    private ParameterAccess createParameterAccess(final String parameterName)
    {
        final Binding binding = getBinding(parameterName);

        ParameterModel parameterModel = getComponentModel().getParameterModel(parameterName);

        final boolean allowNull = parameterModel == null ? true : parameterModel.isAllowNull();

        return new ParameterAccess()
        {
            public boolean isBound()
            {
                return binding != null;
            }

            public Object read(String desiredTypeName)
            {
                Class desiredType = elementResources.toClass(desiredTypeName);

                return read(desiredType);
            }

            public <T> T read(Class<T> desiredType)
            {
                if (binding == null) return null;

                T result;

                try
                {
                    // Will throw NPE if binding is null, but this should never be called if the
                    // parameter is not bound.

                    Object boundValue = binding.get();

                    result = elementResources.coerce(boundValue, desiredType);
                }
                catch (Exception ex)
                {
                    throw new TapestryException(
                            StructureMessages.getParameterFailure(parameterName, getCompleteId(), ex), binding,
                            ex);
                }

                if (result == null && !allowNull)
                    throw new TapestryException(String.format(
                            "Parameter '%s' of component %s is bound to null. This parameter is not allowed to be null.",
                            parameterName,
                            getCompleteId()), binding, null);

                return result;
            }

            public <T> void write(T parameterValue)
            {
                if (binding == null) return;

                Class bindingType = binding.getBindingType();

                try
                {
                    Object coerced = elementResources.coerce(parameterValue, bindingType);

                    binding.set(coerced);
                }
                catch (Exception ex)
                {
                    throw new TapestryException(
                            StructureMessages.writeParameterFailure(parameterName, getCompleteId(), ex), binding,
                            ex);
                }
            }

            public boolean isInvariant()
            {
                return binding != null && binding.isInvariant();
            }

            public Class getBoundType()
            {
                return binding == null ? null : binding.getBindingType();
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return binding == null ? null : binding.getAnnotation(annotationClass);
            }
        };
    }
}
