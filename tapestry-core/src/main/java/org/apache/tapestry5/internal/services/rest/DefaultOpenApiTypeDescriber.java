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
package org.apache.tapestry5.internal.services.rest;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.RestInfo;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.rest.MappedEntityManager;
import org.apache.tapestry5.services.rest.OpenApiTypeDescriber;

/**
 * {@link OpenApiTypeDescriber} implementation that handles some basic types, mostly primitives and String.
 * Since this is the fallback, if the parameter doesn't have any handled type, it defaults
 * to give the <code>object</code> to it without providing properties.
 */
public class DefaultOpenApiTypeDescriber implements OpenApiTypeDescriber 
{
    final Set<Class<?>> mappedEntities;
    private static final String ARRAY_TYPE = "array";
    private static final String OBJECT_TYPE = "object";
    private static final String STRING_TYPE = "string";
    private static final Function<Class<?>, String> TO_INTEGER = (c) -> "integer";
    private static final Function<Class<?>, String> TO_BOOLEAN = (c) -> "boolean";
    private static final Function<Class<?>, String> TO_NUMBER = (c) -> "number";
    private static final List<Handler> MAPPERS = Arrays.asList(
            new Handler(int.class, TO_INTEGER),
            new Handler(Integer.class, TO_INTEGER),
            new Handler(byte.class, TO_INTEGER),
            new Handler(Byte.class, TO_INTEGER),
            new Handler(short.class, TO_INTEGER),
            new Handler(Short.class, TO_INTEGER),
            new Handler(long.class, TO_INTEGER),
            new Handler(Long.class, TO_INTEGER),
            new Handler(float.class, TO_NUMBER),
            new Handler(Float.class, TO_NUMBER),
            new Handler(double.class, TO_NUMBER),
            new Handler(Double.class, TO_NUMBER),
            new Handler(boolean.class, TO_BOOLEAN),
            new Handler(Boolean.class, TO_BOOLEAN),
            new Handler(String.class, (c) -> STRING_TYPE),
            new Handler(char.class, (c) -> STRING_TYPE),
            new Handler(Character.class, (c) -> STRING_TYPE),
            new Handler(JSONObject.class, (c) -> OBJECT_TYPE),
            new Handler(JSONArray.class, (c) -> ARRAY_TYPE)
    );
    
    public DefaultOpenApiTypeDescriber(final MappedEntityManager mappedEntityManager)
    {
        mappedEntities = mappedEntityManager.getEntities();
    }
    
    @Override
    public void describe(JSONObject description, Parameter parameter) 
    {
        describeType(description, parameter.getType());
        
        // According to the OpenAPI 3 documentation, path parameters are always required.
        final RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
        if (requestParameter == null || requestParameter != null && !requestParameter.allowBlank())
        {
            description.put("required", true);
        }
        
    }

    @Override
    public void describeReturnType(JSONObject description, Method method) 
    {
        Class<?> returnedType;
        final RestInfo restInfo = method.getAnnotation(RestInfo.class);
        if (restInfo != null)
        {
            returnedType = restInfo.returnType();
        }
        else 
        {
            returnedType = method.getReturnType();
        }
        describeType(description, returnedType);
    }

    private JSONObject describeType(JSONObject description, Class<?> type)
    {
        // If a schema is already provided, we leave it unchanged.
        JSONObject schema = description.getJSONObjectOrDefault("schema", null);
        if (schema == null)
        {
            final Optional<String> schemaType = getOpenApiType(type);
            if (schemaType.isPresent())
            {
                schema = description.put("schema", new JSONObject("type", schemaType.get()));
            }
            else if (mappedEntities.contains(type))
            {
                schema = description.put("schema", 
                        new JSONObject("$ref", getSchemaReference(type)));
            }
        }
        return schema;
    }

    private Optional<String> getOpenApiType(Class<?> type) {
        final Optional<String> schemaType = MAPPERS.stream()
                .filter(h -> h.type.equals(type))
                .map(h -> h.getMapper().apply(type))
                .findFirst();
        return schemaType;
    }
    
    private static final class Handler
    {
        final private Class<?> type;
        
        final private Function<Class<?>, String> mapper;

        public Handler(Class<?> type, Function<Class<?>, String> mapper) 
        {
            super();
            this.type = type;
            this.mapper = mapper;
        }
        
        public Function<Class<?>, String> getMapper() {
            return mapper;
        }
        
    }

    @Override
    public void describeSchema(Class<?> entity, JSONObject schemas) 
    {
        
        final String name = getSchemaName(entity);
        
        // Don't overwrite already provided schemas
        if (!schemas.containsKey(name))
        {
            JSONObject schema = new JSONObject();
            JSONObject properties = new JSONObject();
            final BeanInfo beanInfo;
            
            try 
            {
                beanInfo = Introspector.getBeanInfo(entity, Object.class);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
            
            final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) 
            {
                final String propertyName = propertyDescriptor.getName();
                final Class<?> type = propertyDescriptor.getPropertyType();
                Optional<String> schemaType = getOpenApiType(type);
                if (schemaType.isPresent())
                {
                    JSONObject propertyDescription = new JSONObject();
                    propertyDescription.put("type", schemaType.get());
                    properties.put(propertyName, propertyDescription);
                }
//                else if (mappedEntities.contains(entity))
//                {
//                    JSONObject propertyDescription = new JSONObject();
//                    propertyDescription.put("schema", 
//                            new JSONObject("$ref", getSchemaReference(type)));
//                    properties.put(propertyName, propertyDescription);
//                }
            }
            
            schema.put("properties", properties);
            schemas.put(name, schema);
        }
    }

    private String getSchemaName(final Class<?> entity) {
        return entity.getSimpleName();
    }

    private String getSchemaReference(final Class<?> entity) {
        return "#/components/schemas/" + getSchemaName(entity);
    }

}
