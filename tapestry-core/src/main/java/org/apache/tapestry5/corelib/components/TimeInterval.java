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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.DateUtilities;

import java.util.Date;

/**
 * Used to present an interval value using Moment.js's from() or fromNow() functions. The interval
 * is determined in terms of a start and end date; either (but not both) may be omitted, in which
 * case the client will dynamically update the element.  In that case, the value will live update, approximately every second.
 *
 * This component will render an empty element. The element will match the template element,
 * or a "span" if the template did not provide an element. Informal parameters will be rendered
 * into the element.
 *
 * When the end date is after the start date, the rendered text will be prefixed with "in".
 * When the end date precedes the start date, the rendered text will be suffixed with "ago".
 * The plain parameter is used to turn off the prefix or suffix.
 *
 * @tapestrydoc
 * @see LocalDate
 * @since 5.4
 */
@SupportsInformalParameters
@Import(module = "t5/core/time-interval")
public class TimeInterval
{
    /**
     * The start date for the interval. If omitted, the start time is considered "now" and will automatically
     * update in the browser.
     */
    @Parameter
    Date start;

    /**
     * The end date for the interval. If omitted, the end time is considered "now" and will update automatically
     * in the browser.
     */
    @Parameter()
    Date end;

    /**
     * If true, then output is plain: no "in" prefix or "ago" suffix.
     */
    @Parameter
    boolean plain;

    @Inject
    ComponentResources resources;

    @Inject
    DateUtilities dateUtilities;

    private String toISO(Date date)
    {
        return date == null ? null : dateUtilities.formatISO8601(date);
    }

    boolean beginRender(MarkupWriter writer)
    {
        writer.element(resources.getElementName("span"),
                // Trigger the client-side logic:
                "data-timeinterval", "true",
                "data-timeinterval-start", toISO(start),
                "data-timeinterval-end", toISO(end));

        if (plain)
        {
            writer.attributes("data-timeinterval-plain", true);
        }

        resources.renderInformalParameters(writer);

        writer.end();

        // Skip the body regardless.
        return false;
    }

}
