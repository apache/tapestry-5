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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.DateUtilities;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.Date;

/**
 * Used to present a date, formatted in the time zone of the client browser.
 * This is based on the <a href="http://momentjs.com/">Moment</a> JavaScript library.
 *
 * If the value parameter is non-null, then this component will render an element
 * wrapping the value (formatted in ISO-8601). The element will match the template element,
 * or a "span" if the template did not provide an element. Informal parameters will be rendered
 * into the element.
 *
 * When a date is rendered, it is rendered in an element, used to specify the client-side formatting.
 * The element's content will be the ISO-8601 format. The client-side will
 * immediately rewrite the content to the formatted value, in the client browser's time
 * zone.
 *
 * @tapestrydoc
 * @see TimeInterval
 * @since 5.4
 */
@SupportsInformalParameters
public class LocalDate
{
    /**
     * The format to use, as defined by <a href="http://momentjs.com/docs/#/displaying/format/">Moment.js</a>.
     * The factory default is "lll", which is a short form of the month, day, year, hour, minute and am/pm,
     * e.g. "Sep 4 1986 8:30 PM".
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL, allowNull = false, value = "message:private-default-localdate-format")
    String format;

    /**
     * The date value to render.  If this value is null, then nothing is rendered at all.
     */
    @Parameter
    Date value;

    @Inject
    ComponentResources resources;

    @Environmental
    JavaScriptSupport javaScriptSupport;

    @Inject
    DateUtilities dateUtilities;

    boolean beginRender(MarkupWriter writer)
    {
        if (value != null)
        {
            writer.element(resources.getElementName("span"),
                    "data-localdate-format", format);

            resources.renderInformalParameters(writer);

            writer.write(dateUtilities.formatISO8601(value));

            writer.end();

            javaScriptSupport.require("t5/core/localdate");
        }

        // Skip the body regardless.
        return false;
    }
}
