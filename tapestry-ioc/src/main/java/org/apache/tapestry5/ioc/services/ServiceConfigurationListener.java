// Copyright 2014 The Apache Software Foundation
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.def.ServiceDef;

/**
 * Interface that defines listeners to services getting their distributed configuration.
 */
@SuppressWarnings("rawtypes")
public interface ServiceConfigurationListener
{
    
    /**
     * Receives a notification of an ordered configuraton being passed to a service.
     * @param serviceDef a {@link ServiceDef} identifying the service receiving the configuration.
     * @param configuration a {@link List} containing the configuration itself.
     */
    void onOrderedConfiguration(ServiceDef serviceDef, List configuration);

    /**
     * Receives a notification of an unordered configuraton being passed to a service.
     * @param serviceDef a {@link ServiceDef} identifying the service receiving the configuration.
     * @param configuration a {@link Collection} containing the configuration itself.
     */
    void onUnorderedConfiguration(ServiceDef serviceDef, Collection configuration);

    /**
     * Receives a notification of a mapped configuraton being passed to a service.
     * @param serviceDef a {@link ServiceDef} identifying the service receiving the configuration.
     * @param configuration a {@link Map} containing the configuration itself.
     */
    void onMappedConfiguration(ServiceDef serviceDef, Map configuration);

}
