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
package org.apache.tapestry5.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.alerts.Alert;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.runtime.RenderCommand;

/**
 *
 * Service that provides methods that render {@link Block}s ({@code <t:block>} in the template),
 * component instances and {@link RenderCommand}s to a {@link String} or 
 * {@link Document org.apache.tapestry5.dom.Document} in a programatic way.
 *
 *
 * This service was created for situations in which a page or component needs to generate some markup
 * that wouldn't be rendered normally, but for external use, such as e-mails, returning
 * HTML for AJAX requests or passing HTML instead of plain string for an {@link Alert}.
 *
 *
 * The name of this interface comes from <a href="https://issues.apache.org/jira/browse/TAP5-938">TAP5-938</a>:
 * <em>Expose ability to render a portion of a page (a Block, Component, etc.) without using internal services</em>.
 *
 * @since 5.4
 */
public interface PartialTemplateRenderer
{
    /**
     *
     * Renders an object, probably a {@link Block} or component instance, to a string. 
     * This method supposes any kind of initialization needed
     * was already done. CSS and JavaScript inclusions or importings are ignored.
     * The object must implement {@link RenderCommand} or being able to be coerced to it
     * by {@link TypeCoercer}.
     *
     * @param object an object, probably a {@link Block} or component instance or {@link RenderCommand}.
     * @throws IllegalArgumentException if the object isn't a {@link RenderCommand} and cannot be coerced to it by {@link TypeCoercer}.
     */
    String render(Object object);
    
    /**
     * Renders an object to a {@link Document} following the same rules as {@link #render(Object)}
     * This method supposes any kind of initialization needed
     * was already done. CSS and JavaScript inclusions or importings are ignored.
     * 
     * @param object to render, a {@link RenderCommand}, or {@linkplain TypeCoercer coercible} to one
     * @return a {@link Document}.
     */
    Document renderAsDocument(Object object);

}