// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.dynamic;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.services.TemplateParser;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.dynamic.DynamicTemplate;
import org.apache.tapestry5.services.dynamic.DynamicTemplateParser;

import java.util.Map;

public class DynamicTemplateParserImpl implements DynamicTemplateParser, UpdateListener
{
    private final Map<Resource, DynamicTemplate> cache = CollectionFactory.newConcurrentMap();

    private final BindingSource bindingSource;

    private final PageSource pageSource;

    private final URLChangeTracker tracker;

    private final TemplateParser componentTemplateParser;

    public DynamicTemplateParserImpl(ClasspathURLConverter converter, BindingSource bindingSource, PageSource pageSource, TemplateParser componentTemplateParser)
    {
        this.bindingSource = bindingSource;
        this.pageSource = pageSource;
        this.componentTemplateParser = componentTemplateParser;

        tracker = new URLChangeTracker(converter);
    }

    @PostInjection
    public void registerAsUpdateListener(UpdateListenerHub hub)
    {
        hub.addUpdateListener(this);
    }

    public DynamicTemplate parseTemplate(Resource resource)
    {
        DynamicTemplate result = cache.get(resource);

        if (result == null)
        {
            result = doParse(resource);
            cache.put(resource, result);

            tracker.add(resource.toURL());
        }

        return result;
    }

    private DynamicTemplate doParse(Resource resource)
    {
        return new DynamicTemplateSaxParser(resource, bindingSource, componentTemplateParser.getDTDURLMappings()).parse();
    }

    public void checkForUpdates()
    {
        if (tracker.containsChanges())
        {
            tracker.clear();
            cache.clear();

            // A typical case is that a "context:" or "asset:" binding is used with the Dynamic component's template
            // parameter. This causes the Asset to be converted to a Resource and parsed. However, those are invariant
            // bindings, so even if it is discovered that the underlying file has changed, the parsed template
            // is still cached inside the component. Clearing the page pool forces the page instance to be
            // rebuilt, which is a crude way of clearing out that data. Other alternatives exist, such as
            // yielding up a proxy to the DynamicTemplate that is more change-aware.

            pageSource.clearCache();
        }
    }

}
