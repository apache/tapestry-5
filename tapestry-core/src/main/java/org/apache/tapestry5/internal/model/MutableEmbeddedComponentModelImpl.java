// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.model;

import org.apache.tapestry5.ioc.BaseLocatable;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableEmbeddedComponentModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MutableEmbeddedComponentModelImpl extends BaseLocatable implements MutableEmbeddedComponentModel
{
    private final String id;

    private final String componentType;

    private final String componentClassName;

    private final String declaredClass;

    private final boolean inheritInformalParameters;

    private Map<String, String> parameters;

    private List<String> publishedParameters = Collections.emptyList();

    /**
     * List of mixin class names.
     */
    private List<String> mixinClassNames;

    public MutableEmbeddedComponentModelImpl(String id, String componentType, String componentClassName,
                                             String declaredClass, boolean inheritInformalParameters, Location location)
    {
        super(location);

        this.id = id;
        this.componentType = componentType;
        this.componentClassName = componentClassName;
        this.inheritInformalParameters = inheritInformalParameters;
        this.declaredClass = declaredClass;
    }

    public String getComponentClassName()
    {
        return componentClassName;
    }

    @Override
    public String toString()
    {
        return String.format("EmbeddedComponentModel[id=%s type=%s class=%s inheritInformals=%s]", id, componentType,
                             componentClassName, inheritInformalParameters);
    }

    public void addParameter(String name, String value)
    {
        if (parameters == null) parameters = CollectionFactory.newMap();
        else if (parameters.containsKey(name))
            throw new IllegalArgumentException(ModelMessages.duplicateParameterValue(name, id, declaredClass));

        parameters.put(name, value);
    }

    public String getId()
    {
        return id;
    }

    public String getComponentType()
    {
        return componentType;
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(parameters);
    }

    public String getParameterValue(String parameterName)
    {
        return InternalUtils.get(parameters, parameterName);
    }

    public List<String> getMixinClassNames()
    {
        if (mixinClassNames == null) return Collections.emptyList();

        return Collections.unmodifiableList(mixinClassNames);
    }

    public void addMixin(String mixinClassName)
    {
        if (mixinClassNames == null)
        {
            mixinClassNames = CollectionFactory.newList();
        }
        else
        {
            if (mixinClassNames.contains(mixinClassName)) throw new IllegalArgumentException(ModelMessages
                    .duplicateMixin(mixinClassName, id));
        }

        mixinClassNames.add(mixinClassName);
    }

    public boolean getInheritInformalParameters()
    {
        return inheritInformalParameters;
    }

    public void setPublishedParameters(List<String> parameterNames)
    {
        Defense.notNull(parameterNames, "parameterNames");

        publishedParameters = parameterNames;
    }

    public List<String> getPublishedParameters()
    {
        return publishedParameters;
    }
}
