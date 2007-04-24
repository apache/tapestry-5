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

package org.apache.tapestry.ioc;

/**
 * Object providers represent an alternate way to locate an object provided somewhere in the
 * {@link org.apache.tapestry.ioc.Registry}. Instead of using a just the service id to gain access
 * to a service within the Registry, object providers in different flavors are capable of vending,
 * or even creating, objects of disparate types from disparate sources.
 * <p>
 * Objects are located via an <em>object reference</em>, consisting of two parts:
 * <ul>
 * <li>An provider prefix, used to identify the specific implementation of ObjectProvider</li>
 * <li>An <em>expression</em>, interpreted by the provider.
 * </ul>
 * <p>
 * The two values are separated by a colon.
 * <p>
 * For example, "service:tapestry.ioc.ClassFactory" would consist of a prefix, "service" and an
 * expression "tapestry.ioc.ClassFactory". The expression would be mapped to the built in
 * "ClassFactory" service.
 * <p>
 * ObjectProviders exist to provide abstractions on top of the raw IoC services. Examples to follow,
 * especially "infrastructure:" which provides the ability to easily override services within a
 * Tapestry application.
 */
public interface ObjectProvider
{
    /**
     * Provides an object based on an expression. The process of providing objects occurs within a
     * particular <em>context</em>, which will typically be a service builder method, service
     * contributor method, or service decorator method. The locator parameter provides access to the
     * services visible <em>to that context</em>.
     * 
     * @param <T>
     * @param expression
     *            to be evaluated, to identify the object to return
     * @param objectType
     *            the expected object type
     * @param locator
     *            locator for the <em>context</em> in which the provider is being used
     * @return the requested object
     * @throws RuntimeException
     *             if the expression can not be evaluated, or the type of object identified is not
     *             assignable to the type specified by the objectType parameter
     */
    <T> T provide(String expression, Class<T> objectType, ServiceLocator locator);
}
