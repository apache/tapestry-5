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
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.services.Instantiator;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.PageLifecycleListener;
import org.apache.tapestry.runtime.RenderQueue;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

/**
 * The bridge between a component and its {@link ComponentPageElement}, that supplies all kinds of resources to the
 * component, including access to its parameters, parameter bindings, and persistent field data.
 */
public class InternalComponentResourcesImpl implements InternalComponentResources
{
    private final Page _page;

    private final String _nestedId;

    private final ComponentModel _componentModel;

    private final ComponentPageElement _element;

    private final Component _component;

    private final ComponentResources _containerResources;

    // Case insensitive
    private Map<String, Binding> _bindings;

    private final PageResources _pageResources;

    private Messages _messages;

    // Case insensitive
    private Map<String, Object> _renderVariables;

    public InternalComponentResourcesImpl(Page page, ComponentPageElement element,
                                          ComponentResources containerResources, Instantiator componentInstantiator,
                                          PageResources elementResources)
    {
        _page = page;
        _element = element;
        _containerResources = containerResources;
        _pageResources = elementResources;
        _componentModel = componentInstantiator.getModel();

        _nestedId = _element.getNestedId();

        _component = componentInstantiator.newInstance(this);
    }

    public Location getLocation()
    {
        return _element.getLocation();
    }

    @Override
    public String toString()
    {
        return String.format("InternalComponentResources[%s]", getCompleteId());
    }

    public ComponentModel getComponentModel()
    {
        return _componentModel;
    }

    public Component getEmbeddedComponent(String embeddedId)
    {
        return _element.getEmbeddedElement(embeddedId).getComponent();
    }

    public Object getFieldChange(String fieldName)
    {
        return _page.getFieldChange(_nestedId, fieldName);
    }

    public String getId()
    {
        return _element.getId();
    }

    public boolean hasFieldChange(String fieldName)
    {
        return getFieldChange(fieldName) != null;
    }

    /**
     * Delegates to the {@link Page#createActionLink(String, String, boolean, Object[])} on the containing page. Why the
     * extra layer? Trying to avoid some unwanted injection (of LinkFactory, into every component page element).
     */
    public Link createActionLink(String action, boolean forForm, Object... context)
    {
        return _page.createActionLink(_nestedId, action, forForm, context);
    }

    public Link createPageLink(String pageName, boolean override, Object... context)
    {
        return _page.createPageLink(pageName, override, context);
    }

    public void discardPersistentFieldChanges()
    {
        _page.discardPersistentFieldChanges();
    }

    public String getElementName()
    {
        return getElementName(null);
    }

    public String getCompleteId()
    {
        return _element.getCompleteId();
    }

    public Component getComponent()
    {
        return _component;
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
        return _element.isRendering();
    }

    public boolean triggerEvent(String eventType, Object[] context, ComponentEventCallback handler)
    {
        return _element.triggerEvent(eventType, context, handler);
    }

    public boolean triggerContextEvent(String eventType, EventContext context, ComponentEventCallback callback)
    {
        return _element.triggerContextEvent(eventType, context, callback);
    }

    public String getNestedId()
    {
        return _element.getNestedId();
    }

    public Component getPage()
    {
        return _element.getContainingPage().getRootComponent();
    }

    public boolean isInvariant(String parameterName)
    {
        Binding b = getBinding(parameterName);

        return b != null && b.isInvariant();
    }

    public boolean isLoaded()
    {
        return _element.isLoaded();
    }

    public void persistFieldChange(String fieldName, Object newValue)
    {
        try
        {
            _page.persistFieldChange(this, fieldName, newValue);
        }
        catch (Exception ex)
        {
            throw new TapestryException(StructureMessages.fieldPersistFailure(getCompleteId(), fieldName, ex),
                                        getLocation(), ex);
        }
    }

    public void bindParameter(String parameterName, Binding binding)
    {
        if (_bindings == null) _bindings = newCaseInsensitiveMap();

        _bindings.put(parameterName, binding);
    }

    @SuppressWarnings("unchecked")
    public <T> T readParameter(String parameterName, Class<T> expectedType)
    {
        Binding b = getBinding(parameterName);

        try
        {
            // Will throw NPE if binding is null, but this should never be called if the
            // parameter is not bound.

            Object boundValue = b.get();

            return _pageResources.coerce(boundValue, expectedType);
        }
        catch (Exception ex)
        {
            throw new TapestryException(StructureMessages.getParameterFailure(parameterName, getCompleteId(), ex), b,
                                        ex);
        }
    }

    public Object readParameter(String parameterName, String desiredTypeName)
    {
        Class parameterType = _pageResources.toClass(desiredTypeName);

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
            Object coerced = _pageResources.coerce(parameterValue, bindingType);

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
        return _bindings == null ? null : _bindings.get(parameterName);
    }

    public AnnotationProvider getAnnotationProvider(String parameterName)
    {
        return getBinding(parameterName);
    }

    public Logger getLogger()
    {
        return _componentModel.getLogger();
    }

    public Component getMixinByClassName(String mixinClassName)
    {
        return _element.getMixinByClassName(mixinClassName);
    }

    public void renderInformalParameters(MarkupWriter writer)
    {
        if (_bindings == null) return;

        for (String name : _bindings.keySet())
        {
            // Skip all formal parameters.

            if (_componentModel.getParameterModel(name) != null) continue;

            Binding b = _bindings.get(name);

            Object value = b.get();

            if (value == null) continue;

            // Because Blocks can be passed in (right from the template, using <t:parameter>,
            // we want to skip those when rending informal parameters.

            if (value instanceof Block) continue;

            String valueString = _pageResources.coerce(value, String.class);

            writer.attributes(name, valueString);
        }
    }

    public Component getContainer()
    {
        if (_containerResources == null) return null;

        return _containerResources.getComponent();
    }

    public ComponentResources getContainerResources()
    {
        return _containerResources;
    }

    public Messages getContainerMessages()
    {
        return _containerResources != null ? _containerResources.getMessages() : null;
    }

    public Locale getLocale()
    {
        return _element.getLocale();
    }

    public Messages getMessages()
    {
        if (_messages == null) _messages = _pageResources.getMessages(_componentModel);

        return _messages;
    }

    public String getElementName(String defaultElementName)
    {
        return _element.getElementName(defaultElementName);
    }

    public void queueRender(RenderQueue queue)
    {
        queue.push(_element);
    }

    public Block getBlock(String blockId)
    {
        return _element.getBlock(blockId);
    }

    public Block getBlockParameter(String parameterName)
    {
        // Is allowed explicitly to not exist and be informal, otherwise the
        // component in question would just use @Parameter.

        if (getBinding(parameterName) != null) return readParameter(parameterName, Block.class);

        return null;
    }

    public Block findBlock(String blockId)
    {
        return _element.findBlock(blockId);
    }

    public Resource getBaseResource()
    {
        return _componentModel.getBaseResource();
    }

    public String getPageName()
    {
        return _element.getPageName();
    }

    public Map<String, Binding> getInformalParameterBindings()
    {
        Map<String, Binding> result = CollectionFactory.newMap();

        if (_bindings != null)
        {
            for (String name : _bindings.keySet())
            {

                if (_componentModel.getParameterModel(name) != null) continue;

                result.put(name, _bindings.get(name));
            }
        }

        return result;
    }

    public Object getRenderVariable(String name)
    {
        Object result = InternalUtils.get(_renderVariables, name);

        if (result == null) throw new IllegalArgumentException(StructureMessages.missingRenderVariable(getCompleteId(),
                                                                                                       name,
                                                                                                       _renderVariables == null ? null : _renderVariables.keySet()));

        return result;
    }

    public void storeRenderVariable(String name, Object value)
    {
        Defense.notBlank(name, "name");
        Defense.notNull(value, "value");

        if (!_element.isRendering())
            throw new IllegalStateException(StructureMessages.renderVariableSetWhenNotRendering(getCompleteId(), name));

        if (_renderVariables == null) _renderVariables = CollectionFactory.newCaseInsensitiveMap();

        _renderVariables.put(name, value);
    }

    public void postRenderCleanup()
    {
        if (_renderVariables != null) _renderVariables.clear();
    }

    public void addPageLifecycleListener(PageLifecycleListener listener)
    {
        _page.addLifecycleListener(listener);
    }
}
