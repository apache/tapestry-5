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
import org.apache.tapestry.internal.services.ComponentClassCache;
import org.apache.tapestry.internal.services.Instantiator;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.RenderQueue;
import org.apache.tapestry.services.ComponentMessagesSource;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Map;

/**
 * The bridge between a component and its {@link ComponentPageElement}, that supplies all kinds of
 * resources to the component, including access to its parameters, parameter bindings, and
 * persistent field data.
 */
public class InternalComponentResourcesImpl implements InternalComponentResources
{
    private final ComponentModel _componentModel;

    private final ComponentPageElement _element;

    private final TypeCoercer _typeCoercer;

    private final Component _component;

    private final ComponentResources _containerResources;

    private final ComponentClassCache _componentClassCache;

    // Case insensitive
    private Map<String, Binding> _bindings;

    private final ComponentMessagesSource _messagesSource;

    private Messages _messages;

    public InternalComponentResourcesImpl(ComponentPageElement element, ComponentResources containerResources,
                                          Instantiator componentInstantiator, TypeCoercer typeCoercer,
                                          ComponentMessagesSource messagesSource,
                                          ComponentClassCache componentClassCache)
    {
        _element = element;
        _containerResources = containerResources;
        _componentClassCache = componentClassCache;
        _componentModel = componentInstantiator.getModel();
        _typeCoercer = typeCoercer;
        _messagesSource = messagesSource;

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
        return _element.getFieldChange(fieldName);
    }

    public String getId()
    {
        return _element.getId();
    }

    public boolean hasFieldChange(String fieldName)
    {
        return _element.hasFieldChange(fieldName);
    }

    public Link createActionLink(String action, boolean forForm, Object... context)
    {
        return _element.createActionLink(action, forForm, context);
    }

    public Link createPageLink(String pageName, boolean override, Object... context)
    {
        return _element.createPageLink(pageName, override, context);
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

    public boolean isRendering()
    {
        return _element.isRendering();
    }

    public boolean triggerEvent(String eventType, Object[] context, ComponentEventHandler handler)
    {
        return _element.triggerEvent(eventType, context, handler);
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
            _element.persistFieldChange(this, fieldName, newValue);
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

            return _typeCoercer.coerce(boundValue, expectedType);
        }
        catch (Exception ex)
        {
            throw new TapestryException(StructureMessages.getParameterFailure(parameterName, getCompleteId(), ex), b,
                                        ex);
        }
    }

    public Object readParameter(String parameterName, String desiredTypeName)
    {
        Class parameterType = _componentClassCache.forName(desiredTypeName);

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
            Object coerced = _typeCoercer.coerce(parameterValue, bindingType);

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

            String valueString = _typeCoercer.coerce(value, String.class);

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
        if (_messages == null) _messages = _messagesSource.getMessages(_componentModel, getLocale());

        return _messages;
    }

    public String getElementName()
    {
        return _element.getElementName();
    }

    public void queueRender(RenderQueue queue)
    {
        _element.queueRender(queue);
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
}
