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
 * <p>
 * Marks an event handler method to be published as an event to be called in JavaScript
 * through the <code>t5/core/ajax</code> function when the <code>options</code>
 * parameter has an <code>element</code> attribute.
 * </p>
 * 
 * <p>
 * The logic for obtaining the URL is actually located implemented in the
 * <code>t5/core/dom.getEventUrl(eventName, element)</code> function.
 * </p>
 * 
 * <p>
 * The event information is stored in JSON format inside the 
 * {@value org.apache.tapestry5.TapestryConstants#COMPONENT_EVENTS_ATTRIBUTE_NAME} attribute.
 * </p>
 * 
 * <p>
 * When used in a component method, the component must render at least one element,
 * and that's what gets the
 * {@value org.apache.tapestry5.TapestryConstants#COMPONENT_EVENTS_ATTRIBUTE_NAME} attribute above. 
 * If it doesn't, an exception will be thrown.
 * </p>
 * 
 * <p>
 * When used in a page method, the page must render an &lt;body&gt; element. If it doesn't,
 * an exception will be thrown.
 * </p>
 * 
 * @since 5.4.2
 * @see <a href="https://tapestry.apache.org/ajax-and-zones.html#AjaxandZones-Invokingserver-sideeventhandlermethodsfromJavaScript">Invoking server-side event handler methods from JavaScript</a>
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
@UseWith({ COMPONENT, PAGE })
public @interface PublishEvent
{
}
