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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.parser.EndElementToken;
import org.apache.tapestry5.internal.parser.StartComponentToken;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Map;

public class PageLoaderImplTest extends InternalBaseTestCase
{
    private static final String LOGICAL_PAGE_NAME = "Bar";

    private static final String PAGE_CLASS_NAME = "foo.page.Bar";

    private static final String CHILD_CLASS_NAME = "foo.component.Baz";

    private static final Locale LOCALE = Locale.ENGLISH;

    @Test
    public void not_all_embedded_components_in_template()
    {
        ComponentTemplateSource templateSource = mockComponentTemplateSource();
        PageElementFactory elementFactory = mockPageElementFactory();
        ComponentPageElement rootElement = mockComponentPageElement();
        InternalComponentResources resources = mockInternalComponentResources();
        ComponentModel model = mockComponentModel();
        ComponentTemplate template = mockComponentTemplate();
        ComponentClassResolver resolver = mockComponentClassResolver();
        Location bazLocation = mockLocation();
        Location biffLocation = mockLocation();
        Resource r = mockResource();

        Map<String, Location> componentIds = CollectionFactory.newMap();

        componentIds.put("baz", bazLocation);
        componentIds.put("biff", biffLocation);

        train_resolvePageNameToClassName(resolver, LOGICAL_PAGE_NAME, PAGE_CLASS_NAME);

        train_newRootComponentElement(elementFactory, PAGE_CLASS_NAME, rootElement, LOCALE);

        train_getComponentResources(rootElement, resources);
        train_getComponentModel(resources, model);

        train_getComponentClassName(model, PAGE_CLASS_NAME);

        train_getTemplate(templateSource, model, LOCALE, template);

        train_isMissing(template, false);

        train_getEmbeddedIds(model, "foo", "bar", "baz");

        expect(template.getComponentIds()).andReturn(componentIds);

        expect(template.getResource()).andReturn(r);

        replay();

        PageLoader loader = new PageLoaderImpl(templateSource, elementFactory, null, null, resolver);

        try
        {
            loader.loadPage(LOGICAL_PAGE_NAME, LOCALE);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "bar, foo", PAGE_CLASS_NAME);
        }

        verify();
    }

    @Test
    public void type_conflict_between_template_and_class()
    {
        ComponentTemplateSource templateSource = mockComponentTemplateSource();
        PageElementFactory elementFactory = mockPageElementFactory();
        ComponentPageElement rootElement = mockComponentPageElement();
        InternalComponentResources resources = mockInternalComponentResources();
        ComponentModel model = mockComponentModel();
        ComponentTemplate template = mockComponentTemplate();
        EmbeddedComponentModel emodel = mockEmbeddedComponentModel();
        Location l = mockLocation();
        ComponentClassResolver resolver = mockComponentClassResolver();

        Map<String, Location> componentIds = CollectionFactory.newMap();

        componentIds.put("foo", l);

        train_resolvePageNameToClassName(resolver, LOGICAL_PAGE_NAME, PAGE_CLASS_NAME);
        train_newRootComponentElement(elementFactory, PAGE_CLASS_NAME, rootElement, LOCALE);

        train_getComponentResources(rootElement, resources);
        train_getComponentModel(resources, model);

        train_getComponentClassName(model, PAGE_CLASS_NAME);

        train_getTemplate(templateSource, model, LOCALE, template);

        train_isMissing(template, false);

        train_getEmbeddedIds(model, "foo");

        expect(template.getComponentIds()).andReturn(componentIds);

        train_getTokens(template, new StartComponentToken(null, "foo", "Fred", null, l), new EndElementToken(null));

        train_getEmbeddedComponentModel(model, "foo", emodel);

        train_getComponentType(emodel, "Barney");

        replay();

        PageLoader loader = new PageLoaderImpl(templateSource, elementFactory, null, null, resolver);

        try
        {
            loader.loadPage(LOGICAL_PAGE_NAME, LOCALE);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(ex.getMessage(),
                         "Embedded component 'foo' provides a type attribute in the template ('Fred') as well as in the component class ('Barney'). You should not provide a type attribute in the template when defining an embedded component within the component class.");
            assertSame(ex.getLocation(), l);
        }

        verify();
    }
}
