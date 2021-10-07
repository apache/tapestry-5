// Copyright 2021 The Apache Software Foundation
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

package org.apache.tapestry5.rest.jackson.modules;

import java.util.List;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.http.services.HttpRequestBodyConverter;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.jacksondatabind.services.ObjectMapperSource;
import org.apache.tapestry5.rest.jackson.internal.FallbackObjectMapperSource;
import org.apache.tapestry5.rest.jackson.internal.JacksonComponentEventResultProcessor;
import org.apache.tapestry5.rest.jackson.internal.JacksonHttpRequestBodyConverter;
import org.apache.tapestry5.rest.jackson.internal.JacksonOpenApiTypeDescriber;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.rest.MappedEntityManager;
import org.apache.tapestry5.services.rest.OpenApiTypeDescriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;

/**
 * Defines services and and contributions for the Tapestry integration with Jackson Databind.
 * Besides contributing a fallback {@link ObjectMapperSource}, it also creates a
 * {@link ComponentEventResultProcessor} for all classes returned by 
 * {@link MappedEntityManager#getEntities()}.
 * @since 5.8.0
 */
public class RestJacksonModule
{
    
    /**
     * Contributes {@link FallbackObjectMapperSource} (contribution id <code>Fallback</code>) 
     * so we guarantee there's always an {@link ObjectMapper} provided for any type.
     */
    public static void contributeObjectMapperSource(OrderedConfiguration<ObjectMapperSource> configuration)
    {
        configuration.addInstance("Fallback", FallbackObjectMapperSource.class);
    }
    
    /**
     * Adds a (entity class, JacksonComponentEventResultProcessor) for each entity class
     * returned by {@link MappedEntityManager#getEntities()}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void contributeComponentEventResultProcessor(
            MappedConfiguration<Class, ComponentEventResultProcessor> configuration,
            MappedEntityManager mappedEntityManager,
            Response response,
            ObjectMapperSource objectMapperSource,
            @Symbol(TapestryHttpSymbolConstants.CHARSET) String outputEncoding)
    {
        for (Class entityClass : mappedEntityManager.getEntities())
        {
            configuration.add(entityClass, 
                    new JacksonComponentEventResultProcessor(entityClass, response, outputEncoding, objectMapperSource));
        }
    }
    
    /**
     * Contributes the package "&lt;root&gt;.rest.entities" to the configuration, 
     * so that it will be scanned for mapped entity classes.
     */
    public static void contributeMappedEntityManager(Configuration<String> configuration,
            @Symbol(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM) String appRootPackage)
    {
        configuration.add(appRootPackage + ".rest.entities");
    }

    /**
     * Contributes {@link JacksonHttpRequestBodyConverter} to the {@link HttpRequestBodyConverter} service.
     */
    public static void contributeHttpRequestBodyConverter(
            OrderedConfiguration<HttpRequestBodyConverter> configuration) {
        configuration.addInstance("Jackson", JacksonHttpRequestBodyConverter.class);
    }

    /**
     * Builds the {@link ObjectMapperSource} service.
     */
    public static ObjectMapperSource buildObjectMapperSource(
            List<ObjectMapperSource> configuration,
            ChainBuilder chainBuilder)
    {
        return chainBuilder.build(ObjectMapperSource.class, configuration);
    }
    
    /**
     * Provides the default {@link SchemaGenerator} instance with a default configuration.
     */
    public static SchemaGenerator buildSchemaGenerator()
    {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.build();
        return new SchemaGenerator(config);
    }
    
    /**
     * Contributes {@link JacksonOpenApiTypeDescriber} to the {@link OpenApiTypeDescriber} service
     * to generate J.
     */
    public static void contributeOpenApiTypeDescriber(OrderedConfiguration<OpenApiTypeDescriber> configuration)
    {
        configuration.addInstance("Jackson", JacksonOpenApiTypeDescriber.class);
    }
    
}
