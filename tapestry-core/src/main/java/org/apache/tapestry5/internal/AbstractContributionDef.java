// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.def.ContributionDef;

/**
 * Partially implements {@link org.apache.tapestry5.ioc.def.ContributionDef}, providing empty implementations of the
 * three contribute() methods.
 */
public abstract class AbstractContributionDef implements ContributionDef
{
    public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                           Configuration configuration)
    {
    }

    public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                           OrderedConfiguration configuration)
    {
    }

    public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                           MappedConfiguration configuration)
    {
    }
}
