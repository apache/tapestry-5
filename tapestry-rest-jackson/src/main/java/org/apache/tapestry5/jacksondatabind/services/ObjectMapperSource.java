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
package org.apache.tapestry5.jacksondatabind.services;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.services.rest.MappedEntityManager;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * Service that provides {@link ObjectMapper} instances for specific mapped entity classes.
 * <p>
 * <p>
 * It's defined as an ordered configuration of {@link ObjectMapperSource} instances. 
 * Calls to {@link #get(Class)} will call the same method in contributed instances until
 * one returns a non-null {@link ObjectMapper} instance.
 * </p>
 * @see ObjectMapper
 * @see MappedEntityManager
 * @since 5.8.0
 */
@UsesOrderedConfiguration(ObjectMapperSource.class)
public interface ObjectMapperSource 
{
    
    /**
     * Provides the {@linkplain ObjectMapper} to be used for a given mapped entity class.
     */
    ObjectMapper get(Class<?> clasz);

}
