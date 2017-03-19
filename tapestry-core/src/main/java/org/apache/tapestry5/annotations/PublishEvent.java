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
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.COMPONENT;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry5.ioc.annotations.UseWith;

/**
 * Marks an event handler method to be published as an event to be called in JavaScript
 * through the <code>t5/core/triggerServerEvent</code> function.
 * 
 * The event information is stored in JSON format inside the 
 * {@value org.apache.tapestry5.TapestryConstants#COMPONENT_EVENTS_ATTRIBUTE_NAME} attribute.
 * 
 * When used in a component method, the component must render at least one element,
 * and that's what get the 
 * {@value org.apache.tapestry5.TapestryConstants#COMPONENT_EVENTS_ATTRIBUTE_NAME} attribute above. 
 * If it doesn't, an exception will be thrown.
 * 
 * When used in a page method, the page must render an &lt;body&gt; element,
 * {@value org.apache.tapestry5.TapestryConstants#COMPONENT_EVENTS_ATTRIBUTE_NAME}. If it doesn't,
 * an exception will be thrown.
 * 
 * @since 5.4.2
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
@UseWith({ COMPONENT, PAGE })
public @interface PublishEvent
{
}
