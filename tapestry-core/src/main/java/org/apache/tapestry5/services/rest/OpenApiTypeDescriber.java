// Copyright 2021 The Apache Software Foundation
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
package org.apache.tapestry5.services.rest;

import java.lang.reflect.Parameter;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Interface that describes the type of a REST endpoint parameter, return type or request body.
 * It should add a <code>schema</code> (in most cases) or <code>content</code> property to the 
 * provided {@linkplain JSONObject} if the given type is supported. This can be be also
 * used to customize specific parameters or return type or request body of specific paths.
 * As a service, this is a chain of instances of itself. All instances will be called.
 */
@UsesOrderedConfiguration(OpenApiTypeDescriber.class)
public interface OpenApiTypeDescriber 
{

    /**
     * Describes a REST event handler method parameter.
     * @param description {@link JSONObject} containing the description of an event handler parameter.
     * @param parameter the event handler method parameter.
     */
    void describe(final JSONObject description, Parameter parameter);
    
    /**
     * Describes a REST event handler method return type.
     * @param description {@link JSONObject} containing the description of a path response.
     * @param method the event handler method itself.
     */
    void describeReturnType(final JSONObject description, Method method);

    /**
     * Describes the schema of a mapped entity class
     * @param entity an entity class.
     * @param schemas {@link JSONObject} where the entity description should be added.
     * @see MappedEntityManager
     */
    void describeSchema(Class<?> entity, JSONObject schemas);

}
