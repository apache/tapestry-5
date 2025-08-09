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
package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.javascript.AbstractInitialization;

abstract class BaseInitialization<T extends AbstractInitialization<?>> implements AbstractInitialization<T>
{
    final String moduleName;

    protected String functionName;

    BaseInitialization(String moduleName)
    {
        this.moduleName = moduleName;
    }

    @SuppressWarnings("unchecked")
    public T invoke(String functionName)
    {
        assert InternalUtils.isNonBlank(functionName);

        this.functionName = functionName;

        return (T) this;
    }
    
}