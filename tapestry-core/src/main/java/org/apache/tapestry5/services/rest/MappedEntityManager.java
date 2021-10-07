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
package org.apache.tapestry5.services.rest;

import java.util.Set;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * Service which provides a list of mapped entities. They're usually classes which are mapped
 * to other formats like JSON and XML and used to represent data being received or sent
 * to or from external processes, for example REST endpoints.
 * Contributions are done by package and all classes inside the contributed ones are considered
 * mapped entities.
 */
@UsesConfiguration(String.class)
public interface MappedEntityManager {

    /**
     * Returns the set of entity classes.
     * @return a {@link Set} of {@link Class} instances.
     */
    Set<Class<?>> getEntities();
    
}
