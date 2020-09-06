// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.util.LocalizedNameGenerator;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;

import java.util.Arrays;
import java.util.List;

public class DefaultComponentResourceLocator implements ComponentResourceLocator
{
    private ComponentTemplateLocator componentTemplateLocator;

    public DefaultComponentResourceLocator(ComponentTemplateLocator componentTemplateLocator)
    {
        this.componentTemplateLocator = componentTemplateLocator;
    }

    public Resource locateTemplate(ComponentModel model, ComponentResourceSelector selector)
    {
        // For 5.2 compatibility, this implementation delegates to the older
        // ComponentTemplateLocator command chain. That may be removed in 5.4.

        return componentTemplateLocator.locateTemplate(model, selector.locale);
    }

    public List<Resource> locateMessageCatalog(final Resource baseResource, ComponentResourceSelector selector)
    {
        String baseName = baseResource.getFile();

        // This is the case for some of the "virtual resources" introduced in 5.4
        if (baseName == null)
        {
            return Arrays.asList(baseResource.forLocale(selector.locale));
        }

        return F.flow(new LocalizedNameGenerator(baseName, selector.locale).iterator()).map(new Mapper<String, Resource>()
        {
            public Resource map(String element)
            {
                return baseResource.forFile(element);
            }
        }).toList();
    }

}
