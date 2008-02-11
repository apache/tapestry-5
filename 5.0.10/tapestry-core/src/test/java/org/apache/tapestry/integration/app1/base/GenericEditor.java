// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.base;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.annotations.Retain;
import org.apache.tapestry.corelib.components.BeanEditForm;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.PropertyConduitSource;

/**
 * For testing TAPESTRY-1518.
 */
public class GenericEditor<T>
{
    @Persist
    private T _bean;

    @Component(parameters = {"object=bean"})
    private BeanEditForm _form;

    @Inject
    private PropertyConduitSource _conduit;

    @Retain
    private String _beanType;

    {
        // Use getClass(), not GenericEditor.class, to determine the correct type for the bean.
        // Otherwise, it would be Object.

        PropertyConduit conduit = _conduit.create(getClass(), "bean");

        _beanType = conduit.getPropertyType().getName();
    }

    public String getBeanType()
    {
        return _beanType;
    }

    public T getBean()
    {
        return _bean;
    }

    public void setBean(T bean)
    {
        _bean = bean;
    }
}
