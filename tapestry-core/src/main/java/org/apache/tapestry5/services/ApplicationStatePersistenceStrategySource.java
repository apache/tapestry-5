// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used to provide access to
 * {@link org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource} instances
 * via a logical name for the strategy, such as "session".
 * <p>
 * <em>NOTE: The term "Application" here is a hold-over from Tapestry 5.0, which used
 * the @ApplicationState (deprecated and deleted) annotation, and called them "ASOs"
 * (Application State Objects). This service would be better named
 * "SessionStatePersistenceStrategySource" (but renaming it would cause backwards
 * compatibility issues).</em>
 */
@UsesMappedConfiguration(ApplicationStatePersistenceStrategy.class)
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
