// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry.ioc.BaseLocatable;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.MutableEmbeddedComponentModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MutableEmbeddedComponentModelImpl extends BaseLocatable implements
                                                                     MutableEmbeddedComponentModel
{
    private final String _id;

    private final String _componentType;

    private final String _componentClassName;

    private final String _declaredClass;

    private Map<String, String> _parameters;

    /**
     * List of mixin class names.
     */
    private List<String> _mixinClassNames;

    public MutableEmbeddedComponentModelImpl(String id, String componentType,
                                             String componentClassName, String declaredClass, Location location)
    {
        super(location);

        _id = id;
        _componentType = componentType;
        _componentClassName = componentClassName;
        _declaredClass = declaredClass;
    }

    public String getComponentClassName()
    {
        return _componentClassName;
    }

    @Override
    public String toString()
    {
        return String.format(
                "EmbeddedComponentModel[id=%s type=%s class=%s]",
                _id,
                _componentType,
                _componentClassName);
    }

    public void addParameter(String name, String value)
    {
        if (_parameters == null)
            _parameters = newMap();
        else if (_parameters.containsKey(name))
            throw new IllegalArgumentException(ModelMessages.duplicateParameterValue(
                    name,
                    _id,
                    _declaredClass));

        _parameters.put(name, value);
    }

    public String getId()
    {
        return _id;
    }

    public String getComponentType()
    {
        return _componentType;
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(_parameters);
    }

    public String getParameterValue(String parameterName)
    {
        return InternalUtils.get(_parameters, parameterName);
    }

    public List<String> getMixinClassNames()
    {
        if (_mixinClassNames == null) return Collections.emptyList();

        return Collections.unmodifiableList(_mixinClassNames);
    }

    public void addMixin(String mixinClassName)
    {
        if (_mixinClassNames == null)
        {
            _mixinClassNames = CollectionFactory.newList();
        }
        else
        {
            if (_mixinClassNames.contains(mixinClassName))
                throw new IllegalArgumentException(ModelMessages
                        .duplicateMixin(mixinClassName, _id));
        }

        _mixinClassNames.add(mixinClassName);
    }
}
