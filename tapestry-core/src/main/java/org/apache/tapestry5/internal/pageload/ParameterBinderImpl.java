// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.internal.structure.ComponentPageElement;

/**
 * Default implementation; invokes {@link org.apache.tapestry5.internal.structure.ComponentPageElement#bindParameter(String,
 * org.apache.tapestry5.Binding)} or {@link org.apache.tapestry5.internal.structure.ComponentPageElement#bindMixinParameter(String,
 * String, org.apache.tapestry5.Binding)}.
 */
class ParameterBinderImpl implements ParameterBinder
{
    private final String mixinId;

    private final String parameterName;

    private final String defaultBindingPrefix;

    ParameterBinderImpl(String mixinId, String parameterName, String defaultBindingPrefix)
    {
        this.mixinId = mixinId;
        this.parameterName = parameterName;
        this.defaultBindingPrefix = defaultBindingPrefix;
    }

    public void bind(ComponentPageElement element, Binding binding)
    {
        if (mixinId == null)
        {
            element.bindParameter(parameterName, binding);
            return;
        }

        element.bindMixinParameter(mixinId, parameterName, binding);
    }

    public String getDefaultBindingPrefix(String metaDefault)
    {
        return defaultBindingPrefix != null ? defaultBindingPrefix : metaDefault;
    }
}
