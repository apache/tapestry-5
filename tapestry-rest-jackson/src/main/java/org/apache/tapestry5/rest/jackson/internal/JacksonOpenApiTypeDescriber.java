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
package org.apache.tapestry5.rest.jackson.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.rest.MappedEntityManager;
import org.apache.tapestry5.services.rest.OpenApiTypeDescriber;

import com.github.victools.jsonschema.generator.SchemaGenerator;

/**
 * {@link OpenApiTypeDescriber} implementation using 
 * <a href="https://victools.github.io/jsonschema-generator/#introduction">Java JSONSchema Generator</a>,
 * by default generating JSON Schema 2019-09.
 */
public class JacksonOpenApiTypeDescriber implements OpenApiTypeDescriber 
{
    
    final private SchemaGenerator schemaGenerator;
    final Set<Class<?>> entities;
    public JacksonOpenApiTypeDescriber(SchemaGenerator schemaGenerator, MappedEntityManager mappedEntityManager) {
        super();
        this.schemaGenerator = schemaGenerator;
        entities = mappedEntityManager.getEntities();
    }

    @Override
    public void describe(JSONObject description, Parameter parameter) 
    {
    }

    @Override
    public void describeReturnType(JSONObject description, Method method) 
    {
    }

    @Override
    public void describeSchema(Class<?> entity, JSONObject schemas) 
    {
        if (entities.contains(entity))
        {
            final JSONObject schema = new JSONObject(
                    schemaGenerator.generateSchema(entity).toString());
            schema.remove("$schema");
            schemas.put(getSchemaName(entity), schema);
        }
    }
    
    private String getSchemaName(final Class<?> returnType) {
        return returnType.getSimpleName();
    }

}
