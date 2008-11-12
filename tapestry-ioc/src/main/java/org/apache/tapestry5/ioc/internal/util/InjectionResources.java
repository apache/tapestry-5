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
 * Provides for the injection of specific types of values as <em>resources</em> as opposed to services or objects
 * obtained from {@link org.apache.tapestry5.ioc.services.MasterObjectProvider}. This includes values such as a
 * service's id, logger or service interface class.
 */
public interface InjectionResources
{
    /**
     * Given the field type, provide the matching resource value, or null.
     *
     * @param type        type of field or parameter
     * @param genericType generic type information associated with field or parameter
     * @return the  corresponding value, or null
     */
    <T> T findResource(Class<T> type, Type genericType);
}
