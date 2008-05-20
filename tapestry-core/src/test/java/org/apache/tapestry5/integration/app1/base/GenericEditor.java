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

package org.apache.tapestry5.integration.app1.base;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PropertyConduitSource;

/**
 * For testing TAPESTRY-1518.
 */
public class GenericEditor<T>
{
    @Persist
    private T bean;

    @Component(parameters = { "object=bean" })
    private BeanEditForm form;

    @Inject
    private PropertyConduitSource conduit;

    @Retain
    private String beanType;

    {
        // Use getClass(), not GenericEditor.class, to determine the correct type for the bean.
        // Otherwise, it would be Object.

        PropertyConduit conduit = this.conduit.create(getClass(), "bean");

        beanType = conduit.getPropertyType().getName();
    }

    public String getBeanType()
    {
        return beanType;
    }

    public T getBean()
    {
        return bean;
    }

    public void setBean(T bean)
    {
        this.bean = bean;
    }
}
