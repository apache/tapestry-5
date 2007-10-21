// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.model;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.EmbeddedComponentModel;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.model.MutableEmbeddedComponentModel;
import org.apache.tapestry.model.ParameterModel;
import org.slf4j.Logger;

/**
 * Internal implementation of {@link org.apache.tapestry.model.MutableComponentModel}.
 */
public final class MutableComponentModelImpl implements MutableComponentModel
{
    private final ComponentModel _parentModel;

    private final Resource _baseResource;

    private final String _componentClassName;

    private final IdAllocator _persistentFieldNameAllocator = new IdAllocator();

    private final Logger _logger;

    private Map<String, ParameterModel> _parameters;

    private Map<String, EmbeddedComponentModel> _embeddedComponents;

    /** Maps from field name to strategy. */
    private Map<String, String> _persistentFields;

    private List<String> _mixinClassNames;

    private boolean _informalParametersSupported;

    private boolean _mixinAfter;

    private Map<String, String> _metaData;

    public MutableComponentModelImpl(String componentClassName, Logger logger, Resource baseResource,
            ComponentModel parentModel)
    {
        _componentClassName = componentClassName;
        _logger = logger;
        _baseResource = baseResource;
        _parentModel = parentModel;

        // Pre-allocate names from the parent, to avoid name collisions.

        if (_parentModel != null)
        {
            for (String name : _parentModel.getPersistentFieldNames())
            {
                _persistentFieldNameAllocator.allocateId(name);
            }
        }
    }

    @Override
    public String toString()
    {
        return String.format("ComponentModel[%s]", _componentClassName);
    }

    public Logger getLogger()
    {
        return _logger;
    }

    public Resource getBaseResource()
    {
        return _baseResource;
    }

    public String getComponentClassName()
    {
        return _componentClassName;
    }

    public void addParameter(String name, boolean required, String defaultBindingPrefix)
    {
        notBlank(name, "name");
        notBlank(defaultBindingPrefix, "defaultBindingPrefix");

        // TODO: Check for conflict with base model

        if (_parameters == null)
            _parameters = newCaseInsensitiveMap();
        else
        {
            if (_parameters.containsKey(name))
                throw new IllegalArgumentException(ModelMessages.duplicateParameter(
                        name,
                        _componentClassName));
        }

        _parameters.put(name, new ParameterModelImpl(name, required, defaultBindingPrefix));
    }

    public ParameterModel getParameterModel(String parameterName)
    {
        ParameterModel result = InternalUtils.get(_parameters, parameterName.toLowerCase());

        if (result == null && _parentModel != null)
            result = _parentModel.getParameterModel(parameterName);

        return result;
    }

    public List<String> getParameterNames()
    {
        List<String> names = newList();

        if (_parameters != null) names.addAll(_parameters.keySet());

        if (_parentModel != null) names.addAll(_parentModel.getParameterNames());

        Collections.sort(names);

        return names;
    }

    public List<String> getDeclaredParameterNames()
    {
        return InternalUtils.sortedKeys(_parameters);
    }

    public MutableEmbeddedComponentModel addEmbeddedComponent(String id, String type,
            String componentClassName, Location location)
    {
        // TODO: Parent compent model? Or would we simply override the parent?

        if (_embeddedComponents == null)
            _embeddedComponents = newCaseInsensitiveMap();
        else if (_embeddedComponents.containsKey(id))
            throw new IllegalArgumentException(ModelMessages.duplicateComponentId(
                    id,
                    _componentClassName));

        MutableEmbeddedComponentModel embedded = new MutableEmbeddedComponentModelImpl(id, type,
                componentClassName, _componentClassName, location);

        _embeddedComponents.put(id, embedded);

        return embedded; // So that parameters can be filled in
    }

    public List<String> getEmbeddedComponentIds()
    {
        List<String> result = newList();

        if (_embeddedComponents != null) result.addAll(_embeddedComponents.keySet());

        if (_parentModel != null) result.addAll(_parentModel.getEmbeddedComponentIds());

        Collections.sort(result);

        return result;
    }

    public EmbeddedComponentModel getEmbeddedComponentModel(String componentId)
    {
        EmbeddedComponentModel result = InternalUtils.get(_embeddedComponents, componentId);

        if (result == null && _parentModel != null)
            result = _parentModel.getEmbeddedComponentModel(componentId);

        return result;
    }

    public String getFieldPersistenceStrategy(String fieldName)
    {
        String result = InternalUtils.get(_persistentFields, fieldName);

        if (result == null && _parentModel != null)
            result = _parentModel.getFieldPersistenceStrategy(fieldName);

        if (result == null)
            throw new IllegalArgumentException(ModelMessages.missingPersistentField(fieldName));

        return result;
    }

    public List<String> getPersistentFieldNames()
    {
        return _persistentFieldNameAllocator.getAllocatedIds();
    }

    public String setFieldPersistenceStrategy(String fieldName, String strategy)
    {
        String stripped = InternalUtils.stripMemberPrefix(fieldName);

        String logicalFieldName = _persistentFieldNameAllocator.allocateId(stripped);

        if (_persistentFields == null) _persistentFields = newMap();

        _persistentFields.put(logicalFieldName, strategy);

        return logicalFieldName;
    }

    public boolean isRootClass()
    {
        return _parentModel == null;
    }

    public void addMixinClassName(String mixinClassName)
    {
        if (_mixinClassNames == null) _mixinClassNames = newList();

        _mixinClassNames.add(mixinClassName);
    }

    public List<String> getMixinClassNames()
    {
        List<String> result = newList();

        if (_mixinClassNames != null) result.addAll(_mixinClassNames);

        if (_parentModel != null) result.addAll(_parentModel.getMixinClassNames());

        Collections.sort(result);

        return result;
    }

    public void enableSupportsInformalParameters()
    {
        _informalParametersSupported = true;
    }

    public boolean getSupportsInformalParameters()
    {
        return _informalParametersSupported;
    }

    public ComponentModel getParentModel()
    {
        return _parentModel;
    }

    public boolean isMixinAfter()
    {
        return _mixinAfter;
    }

    public void setMixinAfter(boolean mixinAfter)
    {
        _mixinAfter = mixinAfter;
    }

    public void setMeta(String key, String value)
    {
        notBlank(key, "key");
        notBlank(value, "value");

        if (_metaData == null) _metaData = newCaseInsensitiveMap();

        // TODO: Error if duplicate?

        _metaData.put(key, value);
    }

    public String getMeta(String key)
    {
        String result = InternalUtils.get(_metaData, key);

        if (result == null && _parentModel != null) result = _parentModel.getMeta(key);

        return result;
    }

}
