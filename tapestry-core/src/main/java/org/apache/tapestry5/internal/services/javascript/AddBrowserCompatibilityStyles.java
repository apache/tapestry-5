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

package org.apache.tapestry5.internal.services.javascript;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.services.MarkupRenderer;
import org.apache.tapestry5.services.MarkupRendererFilter;

/**
 * Responsible for adding additional style tags that contain directives for non-standards compatible browsers
 *
 * @since 5.4
 */
public class AddBrowserCompatibilityStyles implements MarkupRendererFilter
{
    public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
    {
        renderer.renderMarkup(writer);

        Element head = writer.getDocument().find("html/head");

        // Only add the respective style documents if we've rendered an HTML document
        if (head != null)
        {
            // IE9 does not support CSS animations, so we make the loading mask translucent
            head.raw("<!--[if IE 9]><style type=\"text/css\">.pageloading-mask{opacity:.25;}</style><![endif]-->");
            // Older IE versions do not even support opacity, we'll have to resort to a filter
            head.raw("<!--[if IE lt 9]><style type=\"text/css\">.pageloading-mask{filter:alpha(opacity=25);}</style><![endif]-->");

        }
    }
}
