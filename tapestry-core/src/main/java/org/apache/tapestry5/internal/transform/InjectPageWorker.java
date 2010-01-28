// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.PageLifecycleAdapter;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Peforms transformations that allow pages to be injected into components.
 * 
 * @see org.apache.tapestry5.annotations.InjectPage
 */
public class InjectPageWorker implements ComponentClassTransformWorker
{
    private final class InjectedPageConduit extends ReadOnlyFieldValueConduit
    {
        private final String injectedPageName;

        private Object page;

        private InjectedPageConduit(ComponentResources resources, String fieldName,
                String injectedPageName)
        {
            super(resources, fieldName);

            this.injectedPageName = injectedPageName;

            resources.addPageLifecycleListener(new PageLifecycleAdapter()
            {
                @Override
                public void containingPageDidDetach()
                {
                    page = null;
                }
            });
        }

        public Object get()
        {
            if (page == null)
                page = componentSource.getPage(injectedPageName);

            return page;
        }
    }

    private final ComponentSource componentSource;

    private final ComponentClassResolver resolver;

    public InjectPageWorker(ComponentSource componentSource, ComponentClassResolver resolver)
    {
        this.componentSource = componentSource;
        this.resolver = resolver;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchFieldsWithAnnotation(InjectPage.class))
        {
            addInjectedPage(field);
        }
    }

    private void addInjectedPage(TransformField field)
    {
        InjectPage annotation = field.getAnnotation(InjectPage.class);

        field.claim(annotation);

        String pageName = annotation.value();

        final String fieldName = field.getName();

        final String injectedPageName = InternalUtils.isBlank(pageName) ? resolver
                .resolvePageClassNameToPageName(field.getType()) : pageName;

        ComponentValueProvider<FieldValueConduit> provider = new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(ComponentResources resources)
            {
                return new InjectedPageConduit(resources, fieldName, injectedPageName);
            }
        };

        field.replaceAccess(provider);
    }
}
