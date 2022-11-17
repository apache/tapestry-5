// Copyright 2023 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.internal.services.ComponentDependencyGraphvizGenerator;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Shows graph showing the dependencies of all already loaded pages and its compnoents, mixins and base classes.
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
public class PageDependencyGraph
{

    @Inject
    private ComponentDependencyGraphvizGenerator componentDependencyGraphvizGenerator;
    
    @Inject
    private ComponentDependencyRegistry componentDependencyRegistry;
    
    public String getGraphvizValue()
    {
        final Set<String> pages = componentDependencyRegistry.getClassNames().stream()
                .filter(c -> c.contains(".pages"))
                .collect(Collectors.toSet());
        return componentDependencyGraphvizGenerator.generate(pages.toArray(new String[pages.size()]));
    }
    
}
