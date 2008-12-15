// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * A service that acts as a chain-of-command over a number of {@link org.apache.tapestry5.ioc.ObjectProvider}, but
 * allows for the case where no object may be provided.
 * <p/>
 * This service is itself a key part of Tapestry's general injection mechanism; it is used when instantiating a service
 * implementation instance, invoking module methods (service builder, decorator, or contribution methods), when
 * {@linkplain ObjectLocator#autobuild(Class) autobuilding} objects of any type.
 */
@UsesOrderedConfiguration(ObjectProvider.class)
public interface MasterObjectProvider
{
    /**
     * Provides an object based on an expression. The process of providing objects occurs within a particular
     * <em>context</em>, which will typically be a service builder method, service contributor method, or service
     * decorator method. The locator parameter provides access to the services visible <em>to that context</em>.
     * <p/>
     * When the value is required and no {@link ObjectProvider} provided a non-null value, then {@link
     * ObjectLocator#getService(Class)} is invoked, to provide a uniquely matching service, or throw a failure exception
     * if no <em>single</em> service can be found.
     *
     * @param objectType         the expected object type
     * @param annotationProvider provides access to annotations (typically, the field or parameter to which an
     *                           injection-related annotation is attached); annotations on the field or parameter may
     *                           also be used when resolving the desired object
     * @param locator            locator for the <em>context</em> in which the provider is being used
     * @param required           if true (normal case) a value must be provided; if false then it is allowed for no
     *                           ObjectProvider to provide a value, and this method may return null to indicate the
     *                           failure
     * @param <T>
     * @return the requested object, or null if this object provider can not supply an object
     * @throws RuntimeException if the expression can not be evaluated, or the type of object identified is not
     *                          assignable to the type specified by the objectType parameter
     */
    <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator, boolean required);
}
