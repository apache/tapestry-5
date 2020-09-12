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

/**
 * Used by {@link ApplicationStateManager} and {@link ApplicationStatePersistenceStrategy} to create
 * a Session State Object (SSO) on demand.
 * <p>
 * <em>NOTE: The term "Application" here is a hold-over from Tapestry 5.0, which used
 * the @ApplicationState (deprecated and deleted) annotation, and called them "ASOs"
 * (Application State Objects). This service would be better named "SessionStateCreator"
 * (but renaming it would cause backwards compatibility issues).</em>
 * 
 * @param <T> the type of the created objects
 */
public interface ApplicationStateCreator<T>
{
    /**
     * Create a new instance of a session state object.
     */
    T create();
}
