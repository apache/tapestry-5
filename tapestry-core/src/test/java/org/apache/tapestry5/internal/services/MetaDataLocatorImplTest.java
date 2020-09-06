// Copyright 2007, 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.MetaDataLocator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.apache.tapestry5.commons.util.CollectionFactory.newMap;

import java.util.Collections;
import java.util.Map;

public class MetaDataLocatorImplTest extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

    @BeforeClass
    public void setup()
    {
        typeCoercer = getService(TypeCoercer.class);
    }

    @Test
    public void found_in_component()
    {
        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String completeId = "foo.Bar:baz";

        train_getCompleteId(resources, completeId);
        train_getComponentModel(resources, model);
        train_getMeta(model, key, value);
        train_expandSymbols(symbolSource, value, value);

        replay();

        Map<String, String> configuration = Collections.emptyMap();

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();

        // And check that it's cached:

        train_getCompleteId(resources, completeId);

        replay();

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();
    }

    @Test
    public void default_to_symbol_source()
    {
        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String completeId = "foo/Bar:baz";

        train_getCompleteId(resources, completeId);
        train_getComponentModel(resources, model);
        train_getMeta(model, key, null);
        train_getContainerResources(resources, null);
        train_getPageName(resources, "foo/Bar");
        train_valueForSymbol(symbolSource, key, value);

        replay();

        Map<String, String> configuration = Collections.emptyMap();

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();

        // And check that it's cached:

        train_getCompleteId(resources, completeId);

        replay();

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();
    }

    @Test
    public void find_by_page_name()
    {
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String pageName = "foo/Bar";

        expect(modelSource.getPageModel(pageName)).andReturn(model);

        train_getMeta(model, key, value);
        train_expandSymbols(symbolSource, value, "*expanded*");

        replay();

        Map<String, String> configuration = Collections.emptyMap();

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, pageName, String.class), "*expanded*");

        verify();
    }

    @Test
    public void not_found_by_page_name_but_found_in_configuration()
    {
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String pageName = "gnip/Gnop";

        expect(modelSource.getPageModel(pageName)).andReturn(model);

        train_getMeta(model, key, null);
        train_expandSymbols(symbolSource, value, "*expanded*");

        replay();

        Map<String, String> configuration = CollectionFactory.newMap();
        
        configuration.put("gnip:foo.bar", value);

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, pageName, String.class), "*expanded*");

        verify();
    }

    @Test
    public void found_in_container()
    {
        ComponentResources resources = mockComponentResources();
        ComponentResources containerResources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        ComponentModel containerModel = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String completeId = "foo.Bar:baz";

        train_getCompleteId(resources, completeId);
        train_getComponentModel(resources, model);
        train_getMeta(model, key, null);
        train_getContainerResources(resources, containerResources);
        train_getComponentModel(containerResources, containerModel);
        train_getMeta(containerModel, key, value);
        train_expandSymbols(symbolSource, value, value);

        replay();

        Map<String, String> configuration = Collections.emptyMap();

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();
    }

    @Test
    public void found_via_high_level_default()
    {
        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String completeId = "Bar";
        String logicalPageName = completeId;

        train_getCompleteId(resources, completeId);
        train_getComponentModel(resources, model);
        train_getMeta(model, key, null);
        train_getContainerResources(resources, null);

        train_getPageName(resources, logicalPageName);

        train_expandSymbols(symbolSource, value, value);

        replay();

        Map<String, String> configuration = newMap();
        configuration.put(key, value);

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();

        // And check that it's cached:

        train_getCompleteId(resources, completeId);

        replay();

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();
    }

    @Test
    public void default_matching_is_case_insensitive()
    {
        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String completeId = "foo.Bar";

        train_getCompleteId(resources, completeId);
        train_getComponentModel(resources, model);
        train_getMeta(model, key, null);
        train_getContainerResources(resources, null);

        train_getPageName(resources, "foo/Bar");

        train_expandSymbols(symbolSource, value, value);

        replay();

        Map<String, String> configuration = newMap();
        configuration.put(key.toUpperCase(), value);

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();

        // And check that it's cached:

        train_getCompleteId(resources, completeId);

        replay();

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();
    }

    @Test
    public void subfolder_default_overrides_high_level_default()
    {
        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "zaphod";
        String completeId = "foo.Bar";

        train_getCompleteId(resources, completeId);
        train_getComponentModel(resources, model);
        train_getMeta(model, key, null);
        train_getContainerResources(resources, null);

        train_getPageName(resources, "foo/Bar");

        train_expandSymbols(symbolSource, value, value);

        replay();

        Map<String, String> configuration = newMap();
        configuration.put(key, "xxx");
        configuration.put("foo:" + key, value);

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();

        // And check that it's cached:

        train_getCompleteId(resources, completeId);

        replay();

        assertSame(locator.findMeta(key, resources, String.class), value);

        verify();
    }

    /**
     * Makes sense to test together to ensure that the expanded value is what's fed to the type
     * coercer.
     */
    @Test
    public void train_symbols_expanded_and_types_coerced()
    {
        ComponentResources resources = mockComponentResources();
        ComponentModel model = mockComponentModel();
        SymbolSource symbolSource = mockSymbolSource();
        ComponentModelSource modelSource = mockComponentModelSource();

        String key = "foo.bar";
        String value = "${zaphod}";
        String expandedValue = "99";
        String completeId = "foo.Bar:baz";

        train_getCompleteId(resources, completeId);
        train_getComponentModel(resources, model);
        train_getMeta(model, key, value);
        train_expandSymbols(symbolSource, value, expandedValue);

        replay();

        Map<String, String> configuration = Collections.emptyMap();

        MetaDataLocator locator = new MetaDataLocatorImpl(symbolSource, typeCoercer, modelSource,
                configuration);

        assertEquals(locator.findMeta(key, resources, Integer.class), new Integer(99));

        verify();
    }
}
