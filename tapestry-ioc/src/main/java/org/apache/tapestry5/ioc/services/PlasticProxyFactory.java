// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticClassTransformer;

/**
 * A service used to create proxies of varying types.
 * 
 * @since 5.3.0
 */
public interface PlasticProxyFactory
{
    /**
     * Returns the class loader used when creating new classes, this is a child class loader
     * of another class loader (usually, the thread's context class loader).
     */
    ClassLoader getClassLoader();

    /**
     * Creates a proxy object that implements the indicated interface, then invokes the callback to further
     * configure the proxy.
     * 
     * @param interfaceType
     *            interface implemented by proxy
     * @param callback
     *            configures the proxy
     * @return instantiator that can be used to create an instance of the proxy class
     */
    ClassInstantiator createProxy(Class interfaceType, PlasticClassTransformer callback);

    /**
     * Creates the underlying {@link PlasticClassTransformation} for an interface proxy. This should only be
     * used in the cases where encapsulating the PlasticClass construction into a {@linkplain PlasticClassTransformer
     * callback} is not feasible (which is the case for some of the older APIs inside Tapestry IoC).
     * 
     * @param interfaceType
     *            class proxy will extend from
     * @return transformation from which an instantiator may be created
     */
    PlasticClassTransformation createProxyTransformation(Class interfaceType);
}
