// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.parser;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.util.List;
import java.util.Set;

import org.apache.tapestry.ioc.Resource;

public class ComponentTemplateImpl implements ComponentTemplate
{
    private final Resource _resource;

    private final List<TemplateToken> _tokens;

    private final Set<String> _componentIds;

    /**
     * @param resource
     *            the resource from which the template was parsed
     * @param tokens
     *            the tokens of the template, a copy of this list will be made
     * @param componentIds
     *            TODO
     */
    public ComponentTemplateImpl(Resource resource, List<TemplateToken> tokens,
            Set<String> componentIds)
    {
        _resource = resource;
        _tokens = newList(tokens);
        _componentIds = newSet(componentIds);
    }

    public Resource getResource()
    {
        return _resource;
    }

    public List<TemplateToken> getTokens()
    {
        return _tokens;
    }

    public Set<String> getComponentIds()
    {
        return _componentIds;
    }

    /** Returns false. */
    public boolean isMissing()
    {
        return false;
    }

}
