// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Performs transformations that allow pages to be injected into components.
 *
 * @see org.apache.tapestry5.annotations.InjectPage
 */
public class InjectPageWorker implements ComponentClassTransformWorker2
{
    private final class InjectedPageConduit extends ReadOnlyComponentFieldConduit
    {
        private final String injectedPageName;

        private final ObjectCreator<Object> pageValue = perThreadManager.createValue(new ObjectCreator<Object>() {
            @Override
            public Object createObject() {
                return componentSource.getPage(injectedPageName);
            }
        });

        private InjectedPageConduit(String className, String fieldName,
                                    String injectedPageName)
        {
            super(className, fieldName);

            this.injectedPageName = injectedPageName;
        }

        public Object get(Object instance, InstanceContext context)
        {
            return pageValue.createObject();
        }
    }

    private final ComponentSource componentSource;

    private final ComponentClassResolver resolver;

    private final PerthreadManager perThreadManager;

    public InjectPageWorker(ComponentSource componentSource, ComponentClassResolver resolver, PerthreadManager perThreadManager)
    {
        this.componentSource = componentSource;
        this.resolver = resolver;
        this.perThreadManager = perThreadManager;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(InjectPage.class))
        {
            addInjectedPage(field);
        }
    }

    private void addInjectedPage(PlasticField field)
    {
        InjectPage annotation = field.getAnnotation(InjectPage.class);

        field.claim(annotation);

        String pageName = annotation.value();

        String fieldName = field.getName();

        String injectedPageName = InternalUtils.isBlank(pageName) ? resolver
                .resolvePageClassNameToPageName(field.getTypeName()) : pageName;

        field.setConduit(new InjectedPageConduit(field.getPlasticClass().getClassName(), fieldName, injectedPageName));
    }
}
