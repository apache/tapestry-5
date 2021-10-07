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
import java.io.PrintWriter;

import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.jacksondatabind.services.ObjectMapperSource;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.rest.MappedEntityManager;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles mapped entity class instances using Jackson Databind
 * when they're returned by event handler methods. It uses the {@link ObjectMapper}
 * instance provided by {@link ObjectMapperSource}.
 * 
 * @see MappedEntityManager
 * @see ObjectMapperSource
 * @since 5.8.0
 */
public class JacksonComponentEventResultProcessor<T> implements ComponentEventResultProcessor<T> {

    private final Response response;

    private final ContentType contentType;

    private final ObjectMapperSource objectMapperSource;
    
    private final Class<T> entityClass;

    public JacksonComponentEventResultProcessor(Class<T> entityClass, Response response,
                @Symbol(TapestryHttpSymbolConstants.CHARSET) String outputEncoding,
                ObjectMapperSource objectMapperSource)
    {
        this.response = response;
        this.objectMapperSource = objectMapperSource;
        this.entityClass = entityClass;
        contentType = new ContentType(InternalConstants.JSON_MIME_TYPE).withCharset(outputEncoding);
    }

    public void processResultValue(T object) throws IOException {
        PrintWriter pw = response.getPrintWriter(contentType.toString());
        pw.write(objectMapperSource.get(entityClass).writeValueAsString(object));
        pw.close();
    }
}
