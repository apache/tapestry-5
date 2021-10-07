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

package org.apache.tapestry5.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.services.HttpRequestBodyConverter;
import org.apache.tapestry5.internal.transform.OnEventWorker;
import org.apache.tapestry5.ioc.annotations.UseWith;

/**
 * Annotation that may be placed on parameters of event handler methods,
 * usually in page classes.
 * Annotated parameters will be extracted fro the request body and converted
 * to the parameter type using {@linkplain HttpRequestBodyConverter}, which uses 
 * {@linkplain TypeCoercer} as a fallback.
 * An event handler method having more than one {@linkplain RequestBody} 
 * parameter is considered an error.
 * 
 * @since 5.8.0
 * @see OnEventWorker
 */
@Target(
{ PARAMETER })
@Retention(RUNTIME)
@Documented
@UseWith(
{ PAGE })
public @interface RequestBody
{
    /**
     * If false (the default), then an exception is thrown when the request body is empty (i.e. zero bytes).
     * If true, then empty bodies are allowed and the parameter will receive a null value.
     */
    boolean allowEmpty() default false;
}
