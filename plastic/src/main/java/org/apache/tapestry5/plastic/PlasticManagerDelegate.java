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

package org.apache.tapestry5.plastic;

/**
 * Delegate to the {@link PlasticManager} that performs the actual transformations of the class.
 * Transformations only occur on main classes, not on inner classes.
 */
public interface PlasticManagerDelegate extends PlasticClassTransformer
{
    /**
     * Configures the instantiator for a transformed PlasticClass.
     * 
     * @param className
     *            fully qualified class name that was transformed
     * @param instantiator
     *            default instantiator, which has an empty {@link InstanceContext}
     * @return the same instantiator, or a new one configured with additional {@link InstanceContext} values
     */
    <T> ClassInstantiator<T> configureInstantiator(String className, ClassInstantiator<T> instantiator);
}
