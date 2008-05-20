// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Extends {@link org.apache.tapestry5.ioc.ServiceResources} with additional methods needed only by the service builder
 * method, related to accessing a service's configuration. Services may have a <em>single</em> configuration in one of
 * three flavors: unordered, ordered or mapped.
 */
public interface ServiceBuilderResources extends ServiceResources, ModuleBuilderSource
{
    <T> Collection<T> getUnorderedConfiguration(Class<T> valueType);

    <T> List<T> getOrderedConfiguration(Class<T> valueType);

    <K, V> Map<K, V> getMappedConfiguration(Class<K> keyType, Class<V> valueType);
}
