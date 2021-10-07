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

import org.apache.tapestry5.internal.services.rest.DefaultOpenApiDescriptionGenerator;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.json.JSONObject;

/**
 * Service used to generate OpenAPI 3.0 description in JSON format for an application 
 * REST endpoints (i.e. REST endpoint event handler methods). A base implementation, 
 * {@linkplain DefaultOpenApiDescriptionGenerator}, is automatically added as the first
 * contribution to the service's distributed configuration. Other implementations of this 
 * interface can be contributed to further customize the description.
 */
@UsesOrderedConfiguration(OpenApiDescriptionGenerator.class)
public interface OpenApiDescriptionGenerator
{
    /**
     * Generates or customizes the OpenAPI 3.0 documentation for this webapp's REST endpoints.
     * 
     * @param documentation a {@link JSONObject} object.
     * @return the generated or customized OpenAPI 3.0 documentation as a JSON object.
     */
    JSONObject generate(JSONObject documentation);
}
