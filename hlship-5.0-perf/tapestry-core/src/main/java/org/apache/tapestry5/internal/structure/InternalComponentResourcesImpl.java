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

    private final PageResources pageResources;

    // Case insensitive
    private Map<String, Binding> bindings;

    private Messages messages;

    // Case insensitive
    private Map<String, Object> renderVariables;

    private static final Object[] EMPTY = new Object[0];

    public InternalComponentResourcesImpl(Page page, ComponentPageElement element,
                                          ComponentResources containerResources, PageResources pageResources,
                                          String completeId, String nestedId, Instantiator componentInstantiator
    )
    {
        this.page = page;
        this.element = element;
        this.containerResources = containerResources;
        this.pageResources = pageResources;
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

    /**
     * Delegates to the {@link Page#createComponentEventLink(String, String, boolean, Object[])} on the containing page.
     * Uses the element's nested id (i.e., a mixin can generate a link, but the link targets the component, not the
     * mixin itself). Why the extra layer? Trying to avoid some unwanted injection (of LinkFactory, into every component
     * page element).
     */
    public Link createActionLink(String eventType, boolean forForm, Object... context)
    {
        return page.createComponentEventLink(element.getNestedId(), eventType, forForm, defaulted(context));
    }

    public Link createEventLink(String eventType, Object... context)
    {
        return page.createComponentEventLink(element.getNestedId(), eventType, false, defaulted(context));
    }

    public Link createFormEventLink(String eventType, Object... context)
    {
        return page.createComponentEventLink(element.getNestedId(), eventType, true, defaulted(context));
    }

    public Link createPageLink(String pageName, boolean override, Object... context)
    {
        return page.createPageRenderLink(pageName, override, defaulted(context));
    }

    public Link createPageLink(Class pageClass, boolean override, Object... context)
    {
        return page.createPageRenderLink(pageClass, override, defaulted(context));
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
        if (getBinding(name) != null) return readParameter(name, type);

        return null;
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
        Binding b = getBinding(parameterName);

        if (b == null) return null;

        return b.getAnnotation(annotationType);
    }

    public boolean isRendering()
    {
        return element.isRendering();
    }

    public boolean triggerEvent(String eventType, Object[] context, ComponentEventCallback handler)
    {
        return element.triggerEvent(eventType, defaulted(context), handler);
    }

    private Object[] defaulted(Object[] input)
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

    public boolean isInvariant(String parameterName)
    {
        Binding b = getBinding(parameterName);

        return b != null && b.isInvariant();
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

    @SuppressWarnings("unchecked")
    public <T> T readParameter(String parameterName, Class<T> expectedType)
    {
        Binding b = getBinding(parameterName);
        T result;

        try
        {
            // Will throw NPE if binding is null, but this should never be called if the
            // parameter is not bound.

            Object boundValue = b.get();

            result = pageResources.coerce(boundValue, expectedType);
        }
        catch (Exception ex)
        {
            throw new TapestryException(StructureMessages.getParameterFailure(parameterName, getCompleteId(), ex), b,
                                        ex);
        }

        if (result == null && !isAllowNull(parameterName))
            throw new TapestryException(String.format(
                    "Parameter '%s' of component %s is bound to null. This parameter is not allowed to be null.",
                    parameterName,
                    getCompleteId()), b, null);

        return result;
    }

    private boolean isAllowNull(String parameterName)
    {
        ParameterModel parameterModel = getComponentModel().getParameterModel(parameterName);

        return parameterModel == null ? true : parameterModel.isAllowNull();
    }


    public Object readParameter(String parameterName, String desiredTypeName)
    {
        Class parameterType = pageResources.toClass(desiredTypeName);

        return readParameter(parameterName, parameterType);
    }

    public Class getBoundType(String parameterName)
    {
        Binding b = getBinding(parameterName);

        return b != null ? b.getBindingType() : null;
    }

    @SuppressWarnings("unchecked")
    public <T> void writeParameter(String parameterName, T parameterValue)
    {
        Binding b = getBinding(parameterName);

        Class bindingType = b.getBindingType();

        try
        {
            Object coerced = pageResources.coerce(parameterValue, bindingType);

            b.set(coerced);
        }
        catch (Exception ex)
        {
            throw new TapestryException(StructureMessages.writeParameterFailure(parameterName, getCompleteId(), ex), b,
                                        ex);
        }
    }

    private Binding getBinding(String parameterName)
    {
        return bindings == null ? null : bindings.get(parameterName);
    }

    public AnnotationProvider getAnnotationProvider(String parameterName)
    {
        return getBinding(parameterName);
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

        for (String name : bindings.keySet())
        {
            // Skip all formal parameters.

            if (componentModel.getParameterModel(name) != null) continue;

            Binding b = bindings.get(name);

            Object value = b.get();

            if (value == null) continue;

            // Because Blocks can be passed in (right from the template, using <t:parameter>),
            // we want to skip those when rending informal parameters.

            if (value instanceof Block) continue;

            String valueString = pageResources.coerce(value, String.class);

            writer.attributes(name, valueString);
        }
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
        if (messages == null) messages = pageResources.getMessages(componentModel);

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

        if (result == null) throw new IllegalArgumentException(StructureMessages.missingRenderVariable(getCompleteId(),
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
}
