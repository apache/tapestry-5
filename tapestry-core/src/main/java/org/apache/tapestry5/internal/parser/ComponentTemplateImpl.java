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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.List;
import java.util.Map;

public class ComponentTemplateImpl implements ComponentTemplate
{
    private final Resource resource;

    private final List<TemplateToken> tokens;

    private final Map<String, Location> componentIds;

    private final boolean extension;

    private final Map<String, List<TemplateToken>> overrides;

    /**
     * @param resource     the resource from which the template was parsed
     * @param tokens       the tokens of the template, a copy of this list will be made
     * @param componentIds ids of components defined in the template
     * @param extension
     * @param overrides    id to list of tokens for that override
     */
    public ComponentTemplateImpl(Resource resource, List<TemplateToken> tokens,
                                 Map<String, Location> componentIds, boolean extension,
                                 Map<String, List<TemplateToken>> overrides)
    {
        this.resource = resource;
        this.extension = extension;
        this.overrides = overrides;
        this.tokens = newList(tokens);
        this.componentIds = CollectionFactory.newMap(componentIds);
    }

    public Resource getResource()
    {
        return resource;
    }

    public List<TemplateToken> getTokens()
    {
        return tokens;
    }

    public Map<String, Location> getComponentIds()
    {
        return componentIds;
    }

    /**
     * Returns false.
     */
    public boolean isMissing()
    {
        return false;
    }

    public List<TemplateToken> getExtensionPointTokens(String extensionPointId)
    {
        return InternalUtils.get(overrides, extensionPointId);
    }

    public boolean isExtension()
    {
        return extension;
    }
}
