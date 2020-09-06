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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.UseWith;
import org.apache.tapestry5.services.ValueEncoderSource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;

/**
 * Marks a field of a page (not a component) as persistent within the URL, as with a page activation context. The field
 * is mapped
 * to a query parameter. When component event or page render links are generated for the page,
 * additional values will be added to the {@link Link} (via the {@link EventConstants#DECORATE_COMPONENT_EVENT_LINK} or
 * {@link EventConstants#DECORATE_PAGE_RENDER_LINK} events).
 *
 * The field may be of any type; a {@link ValueEncoder} (from the {@link ValueEncoderSource}) will be used to convert
 * between client-side and server-side representations. Null values are not added as query parameters (just non-null).
 *
 * When a page is activated, the mapped fields will receive their values before an {@linkplain EventConstants#ACTIVATE
 * activate} event handler method is invoked.
 *
 * This annotation is an alternative to {@link Persist}.
 *
 * Fields annotated with ActivationRequestParameter are <em>not</em> considered persistent (its a process parallel to the one
 * related to the {@link Persist} annotation). Invoking {@link ComponentResources#discardPersistentFieldChanges()} will
 * <em>not</em> affect annotated fields, only assigning them back to null will.
 *
 * @see RequestParameter
 * @see ValueEncoder
 */
@Target(
{ ElementType.FIELD })
@Retention(RUNTIME)
@Documented
@UseWith(
{ PAGE })
public @interface ActivationRequestParameter
{
    /** The name of the query parameter, which defaults to the name of the field. */
    String value() default "";

    /**
     * If true then a null value is an error. If false, then a null value will result in no update to the field. Either way,
     * a null field value will result in no query parameter added to a  {@linkplain org.apache.tapestry5.http.Link generated link}.
     *
     * @since 5.4
     */
    boolean required() default false;
}
