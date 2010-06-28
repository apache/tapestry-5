// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.annotations.UseWith;
import org.apache.tapestry5.services.ValueEncoderSource;

/**
 * Marks a field of a page (not a component) as persistent within the URL, as with a page activation context. The field
 * is mapped
 * to a query parameter. When component event or page render links are generated for the page,
 * additional values will be added to the {@link Link} (via the {@link EventConstants#DECORATE_COMPONENT_EVENT_LINK} or
 * {@link EventConstants#DECORATE_PAGE_RENDER_LINK} events).
 * <p>
 * The field may be of any type; a {@link ValueEncoder} (from the {@link ValueEncoderSource}) will be used to convert
 * between client-side and server-side representations. Null values are not added as query parameters (just non-null).
 * <p>
 * When a page is activated, the mapped fields will receive their values before an {@linkplain EventConstants#ACTIVATE
 * activate} event handler method is invoked.
 * <p>
 * This annotation is an alternative to {@link Persist}.
 * <p>
 * Fields annotated with ActivationRequestParameter are <em>not</em> considered persistent (its a process parallel to the one
 * related to the {@link Persist} annotation). Invoking {@link ComponentResources#discardPersistentFieldChanges()} will
 * <em>not</em> affect annotated fields, only assigning them back to null will.
 * 
 * @see RequestParameter
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

    // TODO: Attributes to limit it to just render links, or just component event links?
}
