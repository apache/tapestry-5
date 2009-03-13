// Copyright 2006, 2008 The Apache Software Foundation
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

/**
 * Allows certain types of lifecycles to control exactly how services are instantiated.
 */
public interface ServiceLifecycle
{
    /**
     * Returns the same creator, or a new one, that encapsulates the creation of the core service implementation.
     *
     * @param resources source of information about the service to be created, and source of additional services or
     *                  other resources that may be needed when constructing the core service implementation
     * @param creator   object capable of creating the service implementation on demand. This is a wrapper around the
     *                  service's builder method.
     * @return the service or equivalent service proxy
     */
    Object createService(ServiceResources resources, ObjectCreator creator);

    /**
     * Returns true if the lifecycle is a singleton (a service that will only be created once).  Return false if the
     * underlying service instance may be created multiple times (for example, the {@link
     * org.apache.tapestry5.ioc.ScopeConstants#PERTHREAD} scope}. A future version of Tapestry IoC may optimize for the
     * later case.
     *
     * @return true for singletons, false   for services that can be repeatedly constructed
     */
    boolean isSingleton();
}
