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

import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import static org.apache.tapestry5.commons.util.CollectionFactory.newList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentTemplateImpl implements ComponentTemplate
{
    private final Resource resource;

    private final List<TemplateToken> tokens;

    private final Map<String, Location> componentIds;

    private final boolean extension, strictMixinParameters;

    private final Map<String, List<TemplateToken>> overrides;

    /**
     * @param resource
     *         the resource from which the template was parsed
     * @param tokens
     *         the tokens of the template, a copy of this list will be made
     * @param componentIds
     *         ids of components defined in the template
     * @param extension
     *         if this template is an extension of a parent-class template
     * @param strictMixinParameters
     *         if the template was parsed with the 5.4 DTD and is strict
     *         about mixin parameters being fully qualified
     * @param overrides
     *         id to list of tokens for that override
     */
    public ComponentTemplateImpl(Resource resource, List<TemplateToken> tokens,
                                 Map<String, Location> componentIds, boolean extension,
                                 boolean strictMixinParameters, Map<String, List<TemplateToken>> overrides)
    {
        this.resource = resource;
        this.extension = extension;
        this.strictMixinParameters = strictMixinParameters;
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

    public boolean usesStrictMixinParameters()
    {
        return strictMixinParameters;
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
    
    public Set<String> getExtensionPointIds()
    {
        return overrides != null ? overrides.keySet() : Collections.emptySet();
    }

    public boolean isExtension()
    {
        return extension;
    }
}
