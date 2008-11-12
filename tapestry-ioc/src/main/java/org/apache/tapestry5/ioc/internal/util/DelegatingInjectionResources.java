//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import java.lang.reflect.Type;

/**
 * Chain of command for InjectionDefaultProvider.
 */
public class DelegatingInjectionResources implements InjectionResources
{
    private final InjectionResources first;
    private final InjectionResources next;

    public DelegatingInjectionResources(InjectionResources first,
                                        InjectionResources next)
    {
        this.first = first;
        this.next = next;
    }

    public <T> T findResource(Class<T> type, Type genericType)
    {
        T result = first.findResource(type, null);

        return result != null ? result : next.findResource(type, genericType);
    }
}
