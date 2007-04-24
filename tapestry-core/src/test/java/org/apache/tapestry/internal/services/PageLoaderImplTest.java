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

package org.apache.tapestry.internal.services;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.parser.ComponentTemplate;
import org.apache.tapestry.internal.parser.EndElementToken;
import org.apache.tapestry.internal.parser.StartComponentToken;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.PageElement;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.EmbeddedComponentModel;
import org.apache.tapestry.services.BindingSource;
import org.testng.annotations.Test;

public class PageLoaderImplTest extends InternalBaseTestCase
{
    private static final String PAGE_CLASS_NAME = "foo.page.Bar";

    private static final String CHILD_CLASS_NAME = "foo.component.Baz";

    private static final Locale LOCALE = Locale.ENGLISH;

    @Test
    public void not_all_embedded_components_in_template()
    {
        ComponentTemplateSource templateSource = mockComponentTemplateSource();
        PageElementFactory elementFactory = mockPageElementFactory();
        BindingSource bindingSource = mockBindingSource();
        ComponentPageElement rootElement = mockComponentPageElement();
        InternalComponentResources resources = mockInternalComponentResources();
        ComponentModel model = mockComponentModel();
        ComponentTemplate template = mockComponentTemplate();
        Log log = mockLog();

        train_newRootComponentElement(elementFactory, PAGE_CLASS_NAME, rootElement);

        train_getComponentResources(rootElement, resources);
        train_getComponentModel(resources, model);

        train_getComponentClassName(model, PAGE_CLASS_NAME);

        train_getTemplate(templateSource, model, LOCALE, template);

        train_isMissing(template, false);

        train_getLog(model, log);

        train_getEmbeddedIds(model, "foo", "bar", "baz");

        train_getComponentIds(template, "baz", "biff");

        log.error(ServicesMessages.embeddedComponentsNotInTemplate(
                Arrays.asList("foo", "bar"),
                PAGE_CLASS_NAME));

        train_getTokens(template);

        replay();

        PageLoader loader = new PageLoaderImpl(templateSource, elementFactory, bindingSource, null,
                null);

        loader.loadPage(PAGE_CLASS_NAME, LOCALE);

        verify();
    }

    @Test
    public void type_conflict_between_template_and_class()
    {
        ComponentTemplateSource templateSource = mockComponentTemplateSource();
        PageElementFactory elementFactory = mockPageElementFactory();
        BindingSource bindingSource = mockBindingSource();
        ComponentPageElement rootElement = mockComponentPageElement();
        InternalComponentResources resources = mockInternalComponentResources();
        ComponentModel model = mockComponentModel();
        ComponentModel childModel = mockComponentModel();
        ComponentTemplate template = mockComponentTemplate();
        Log log = mockLog();
        EmbeddedComponentModel emodel = mockEmbeddedComponentModel();
        ComponentPageElement childElement = mockComponentPageElement();
        InternalComponentResources childResources = mockInternalComponentResources();
        Location l = mockLocation();
        PageElement body = mockPageElement();
        ComponentTemplate childTemplate = mockComponentTemplate();

        train_newRootComponentElement(elementFactory, PAGE_CLASS_NAME, rootElement);

        train_getComponentResources(rootElement, resources);
        train_getComponentModel(resources, model);

        train_getComponentClassName(model, PAGE_CLASS_NAME);

        train_getTemplate(templateSource, model, LOCALE, template);

        train_isMissing(template, false);

        train_getLog(model, log);

        train_getEmbeddedIds(model, "foo");

        train_getComponentIds(template, "foo");

        train_getTokens(
                template,
                new StartComponentToken(null, "foo", "Fred", null, l),
                new EndElementToken(null));

        train_getEmbeddedComponentModel(model, "foo", emodel);

        train_getComponentType(emodel, "Barney");

        train_getMixinClassNames(emodel);

        log.error(ServicesMessages.compTypeConflict("foo", "Fred", "Barney"));

        train_getComponentClassName(emodel, "foo.components.Barney");

        train_newComponentElement(
                elementFactory,
                rootElement,
                "foo",
                "Barney",
                "foo.components.Barney",
                null,
                l, childElement);

        rootElement.addToTemplate(childElement);

        train_getParameterNames(emodel);

        // Alas, what comes next is the recursive call to load the child element

        train_getComponentResources(childElement, childResources);
        train_getComponentModel(childResources, childModel);
        train_getComponentClassName(childModel, CHILD_CLASS_NAME);
        train_getTemplate(templateSource, childModel, LOCALE, childTemplate);
        train_isMissing(childTemplate, true);
        train_newRenderBodyElement(elementFactory, childElement, body);
        childElement.addToTemplate(body);

        replay();

        PageLoader loader = new PageLoaderImpl(templateSource, elementFactory, bindingSource, null,
                null);

        loader.loadPage(PAGE_CLASS_NAME, LOCALE);

        verify();
    }
}
