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

import org.apache.tapestry5.*;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.internal.NullAnnotationProvider;
import org.apache.tapestry5.commons.internal.util.LockSupport;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.bindings.InternalPropBinding;
import org.apache.tapestry5.internal.bindings.PropBinding;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.transform.ParameterConduit;
import org.apache.tapestry5.internal.util.NamedSet;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleCallbackHub;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The bridge between a component and its {@link ComponentPageElement}, that supplies all kinds of
 * resources to the
 * component, including access to its parameters, parameter bindings, and persistent field data.
 */
@SuppressWarnings("all")
public class InternalComponentResourcesImpl extends LockSupport implements InternalComponentResources
{
    private final Page page;

    private final String completeId;

    private final String nestedId;

    private final ComponentModel componentModel;

    private final ComponentPageElement element;

    private final Component component;

    private final ComponentResources containerResources;

    private final ComponentPageElementResources elementResources;

    private final boolean mixin;

    private static final Object[] EMPTY = new Object[0];

    private static final AnnotationProvider NULL_ANNOTATION_PROVIDER = new NullAnnotationProvider();

    // Map from parameter name to binding. This is mutable but not guarded by the lazy creation lock, as it is only
    // written to during page load, not at runtime.
    private NamedSet<Binding> bindings;

    // Maps from parameter name to ParameterConduit, used to support mixins
    // which need access to the containing component's PC's
    // Guarded by: LockSupport
    private NamedSet<ParameterConduit> conduits;

    // Guarded by: LockSupport
    private Messages messages;

    // Guarded by: LockSupport
    private boolean informalsComputed;

    // Guarded by: LockSupport
    private PerThreadValue<Map<String, Object>> renderVariables;

    // Guarded by: LockSupport
    private Informal firstInformal;


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


    private static Worker<ParameterConduit> RESET_PARAMETER_CONDUIT = new Worker<ParameterConduit>()
    {
        public void work(ParameterConduit value)
        {
            value.reset();
        }
    };

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


    public Link createFormEventLink(String eventType, Object... context)
    {
        return element.createFormEventLink(eventType, context);
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
        } catch (Exception ex)
        {
            throw new TapestryException(StructureMessages.fieldPersistFailure(getCompleteId(), fieldName, ex),
                    getLocation(), ex);
        }
    }

    public void bindParameter(String parameterName, Binding binding)
    {
        if (bindings == null)
            bindings = NamedSet.create();

        bindings.put(parameterName, binding);
    }

    public Class getBoundType(String parameterName)
    {
        Binding binding = getBinding(parameterName);

        return binding == null ? null : binding.getBindingType();
    }
    
    public Type getBoundGenericType(String parameterName)
    {
        Binding binding = getBinding(parameterName);
        Type genericType;
        if (binding instanceof Binding2) {
            genericType = ((Binding2) binding).getBindingGenericType();
        } else {
            genericType = binding.getBindingType();
        }
        return genericType;
    }
    

    public Binding getBinding(String parameterName)
    {
        return NamedSet.get(bindings, parameterName);
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

        for (Informal i = firstInformal(); i != null; i = i.next)
            i.write(writer);
    }

    private Informal firstInformal()
    {
        try
        {
            acquireReadLock();

            if (!informalsComputed)
            {
                computeInformals();
            }

            return firstInformal;
        } finally
        {
            releaseReadLock();
        }
    }

    private void computeInformals()
    {
        try
        {
            upgradeReadLockToWriteLock();

            if (!informalsComputed)
            {
                for (Map.Entry<String, Binding> e : getInformalParameterBindings().entrySet())
                {
                    firstInformal = new Informal(e.getKey(), e.getValue(), firstInformal);
                }

                informalsComputed = true;
            }
        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }

    public Component getContainer()
    {
        if (containerResources == null)
        {
            return null;
        }

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

    public ComponentResourceSelector getResourceSelector()
    {
        return element.getResourceSelector();
    }

    public Messages getMessages()
    {
        if (messages == null)
        {
            // This kind of lazy loading pattern is acceptable without locking.
            // Changes to the messages field are atomic; in some race conditions, the call to
            // getMessages() may occur more than once (but it caches the value anyway).
            messages = elementResources.getMessages(componentModel);
        }

        return messages;
    }

    public String getElementName(String defaultElementName)
    {
        return element.getElementName(defaultElementName);
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

        for (String name : NamedSet.getNames(bindings))
        {
            if (componentModel.getParameterModel(name) != null)
                continue;

            result.put(name, bindings.get(name));
        }

        return result;
    }

    private Map<String, Object> getRenderVariables(boolean create)
    {
        try
        {
            acquireReadLock();

            if (renderVariables == null)
            {
                if (!create)
                {
                    return null;
                }

                createRenderVariablesPerThreadValue();
            }

            Map<String, Object> result = renderVariables.get();

            if (result == null && create)
                result = renderVariables.set(CollectionFactory.newCaseInsensitiveMap());

            return result;
        } finally
        {
            releaseReadLock();
        }
    }

    private void createRenderVariablesPerThreadValue()
    {
        try
        {
            upgradeReadLockToWriteLock();

            if (renderVariables == null)
            {
                renderVariables = elementResources.createPerThreadValue();
            }

        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }

    public Object getRenderVariable(String name)
    {
        Map<String, Object> variablesMap = getRenderVariables(false);

        Object result = InternalUtils.get(variablesMap, name);

        if (result == null)
        {
            throw new IllegalArgumentException(StructureMessages.missingRenderVariable(getCompleteId(), name,
                    variablesMap == null ? null : variablesMap.keySet()));
        }

        return result;
    }

    public void storeRenderVariable(String name, Object value)
    {
        assert InternalUtils.isNonBlank(name);
        assert value != null;

        Map<String, Object> renderVariables = getRenderVariables(true);

        renderVariables.put(name, value);
    }

    public void postRenderCleanup()
    {
        Map<String, Object> variablesMap = getRenderVariables(false);

        if (variablesMap != null)
            variablesMap.clear();

        resetParameterConduits();
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

    private void resetParameterConduits()
    {
        try
        {
            acquireReadLock();

            if (conduits != null)
            {
                conduits.eachValue(RESET_PARAMETER_CONDUIT);
            }
        } finally
        {
            releaseReadLock();
        }
    }

    public ParameterConduit getParameterConduit(String parameterName)
    {
        try
        {
            acquireReadLock();
            return NamedSet.get(conduits, parameterName);
        } finally
        {
            releaseReadLock();
        }
    }

    public void setParameterConduit(String parameterName, ParameterConduit conduit)
    {
        try
        {
            acquireReadLock();

            if (conduits == null)
            {
                createConduits();
            }

            conduits.put(parameterName, conduit);
        } finally
        {
            releaseReadLock();
        }
    }

    private void createConduits()
    {
        try
        {
            upgradeReadLockToWriteLock();
            if (conduits == null)
            {
                conduits = NamedSet.create();
            }
        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }


    public String getPropertyName(String parameterName)
    {
        Binding binding = getBinding(parameterName);

        if (binding == null)
        {
            return null;
        }

        // TAP5-1718: we need the full prop binding expression, not just the (final) property name
        if (binding instanceof PropBinding) 
        {
            return ((PropBinding) binding).getExpression();
        }
        
        if (binding instanceof InternalPropBinding)
        {
            return ((InternalPropBinding) binding).getPropertyName();
        }

        return null;
    }

    /**
     * @since 5.3
     */
    public void render(MarkupWriter writer, RenderQueue queue)
    {
        queue.push(element);
    }

    public PageLifecycleCallbackHub getPageLifecycleCallbackHub()
    {
        return page;
    }
}
