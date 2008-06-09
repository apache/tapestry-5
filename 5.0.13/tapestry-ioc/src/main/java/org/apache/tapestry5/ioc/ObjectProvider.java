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

/**
 * Object providers represent an alternate way to locate an object provided somewhere in the {@link
 * org.apache.tapestry5.ioc.Registry}. Instead of using a just the service id to gain access to a service within the
 * Registry, object providers in different flavors are capable of vending, or even creating, objects of disparate types
 * from disparate sources.
 * <p/>
 * Object providers are consulted in a strict order, and the first non-null result is taken.
 * <p/>
 * In many cases, an object provider searches for additional annotations on the element (usually a parameter, or perhaps
 * a field) for which a value is required.
 */
public interface ObjectProvider
{
    /**
     * Provides an object based on an expression. The process of providing objects occurs within a particular
     * <em>context</em>, which will typically be a service builder method, service contributor method, or service
     * decorator method. The locator parameter provides access to the services visible <em>to that context</em>.
     *
     * @param objectType         the expected object type
     * @param annotationProvider provides access to annotations (typically, the field or parameter to which an
     *                           injection-related annotation is attached); annotations on the field or parameter may
     *                           also be used when resolving the desired object
     * @param locator            locator for the <em>context</em> in which the provider is being used
     * @param <T>
     * @return the requested object, or null if this object provider can not supply an object
     * @throws RuntimeException if the expression can not be evaluated, or the type of object identified is not
     *                          assignable to the type specified by the objectType parameter
     */
    <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator);
}
