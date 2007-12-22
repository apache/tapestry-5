// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

/**
 * Used to provide access to stategies via a logical name for the stategy, such as "session".
 * <p/>
 * Note: Ideally, this would just be internal to {@link ApplicationStateManager}, but Tapestry
 * IOC's one-configuration-per-service makes that awkward, so this part of that functionality
 * becomes its own service.
 */
public interface ApplicationStatePersistenceStrategySource
{
    /**
     * Returns the named strategy.
     *
     * @param name of strategy to access
     * @return the strategy
     * @throws RuntimeException if the name does not match a configured strategy
     */
    ApplicationStatePersistenceStrategy get(String name);
}
