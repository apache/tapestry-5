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
package org.apache.tapestry5.rest.jackson.internal;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.http.services.HttpRequestBodyConverter;
import org.apache.tapestry5.jacksondatabind.services.ObjectMapperSource;
import org.apache.tapestry5.services.rest.MappedEntityManager;

public class JacksonHttpRequestBodyConverter implements HttpRequestBodyConverter {
    
    final private Set<Class<?>> entities;
    
    final private ObjectMapperSource objectMapperSource;
    
    public JacksonHttpRequestBodyConverter(MappedEntityManager mappedEntityManager,
            ObjectMapperSource objectMapperSource)
    {
        this.entities = mappedEntityManager.getEntities();
        this.objectMapperSource = objectMapperSource;
    }

    @Override
    public <T> T convert(HttpServletRequest request, Class<T> type) {
        
        T value = null;
        if (entities.contains(type))
        {
            try {
                value = objectMapperSource.get(type).readValue(request.getReader(), type);
            } catch (IOException e) {
                throw new TapestryException("Exception while converting HTTP request body into " + type.getName(), e);
            }
        }
        
        return value;
    }

}
