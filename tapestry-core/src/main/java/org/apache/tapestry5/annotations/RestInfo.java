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

package org.apache.tapestry5.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry5.ioc.annotations.UseWith;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.rest.OpenApiDescriptionGenerator;
import org.apache.tapestry5.services.rest.OpenApiTypeDescriber;

/**
 * Annotation that provides some information about REST event handler methods for OpenAPI 
 * description generation. It can be used in methods and also in pages to define defaults
 * used by all methods without the annotation, except for {{@link #returnType()}.
 * 
 * @see OpenApiDescriptionGenerator
 * @see OpenApiTypeDescriber
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@UseWith({PAGE})
public @interface RestInfo
{
    /**
     * Defines the request body media types supported by the annotated REST event handler method.
     */
    String[] consumes() default "";

    /**
     * Defines the media types of the responses provided by the annotated REST event handler method in 
     * successful requests.
     */
    String[] produces() default "";

    /**
     * Defines the return type of this REST event handler method in successful requests.
     * This is particularly useful for methods that have Object return type because
     * they may return non-response objects for error reasons, such as {@link HttpError}.
     */
    Class<?> returnType() default Object.class;
    
}
