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

import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.internal.transform.PageResetAnnotationWorker;
import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Marker annotation for a method that should be invoked when a page is reset. A page reset occurs
 * when a page is linked to from another page. This is an opportunity to re-initialize aspects of a
 * page when the user returns to a page after visiting other pages. A common example is to
 * reset the active page of a {@link Grid} component.
 *
 * Methods marked with this annotation are invoked <em>after</em> the page is sent the
 * <code>activate</code> event. This is to allow the page to reset itself as appropriate for
 * whatever persistent state was encoded in its page activation context.
 * 
 * @since 5.2.0
 * @see PageResetAnnotationWorker
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@UseWith(
{ COMPONENT, MIXIN, PAGE })
public @interface PageReset
{
}
