// Copyright 2006, 2007, 2008 The Apache Software Foundation
//
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.CSSClassConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.internal.InternalMessages;
import org.apache.tapestry5.services.FormSupport;

import java.util.List;

/**
 * Standard validation error presenter. Must be enclosed by a {@link org.apache.tapestry5.corelib.components.Form}
 * component. If errors are present, renders a div element around a banner message and around an unnumbered list of
 * error messages. Renders nothing if the {@link org.apache.tapestry5.ValidationTracker} shows no errors.
 */
public class Errors
{
    /**
     * The banner message displayed above the errors. The default value is "You must correct the following errors before
     * you may continue.".
     */
    @Parameter("message:default-banner")
    private String banner;

    /**
     * The CSS class for the div element rendered by the component. The default value is "t-error".
     */
    @Parameter(name = "class")
    private String className = CSSClassConstants.ERROR;

    // Allow null so we can generate a better error message if missing
    @Environmental(false)
    private ValidationTracker tracker;

    @Environmental
    private FormSupport formSupport;

    void beginRender(MarkupWriter writer)
    {
        if (tracker == null) throw new RuntimeException(InternalMessages.encloseErrorsInForm());

        if (!tracker.getHasErrors()) return;

        writer.element("div", "class", className);

        // Inner div for the banner text
        writer.element("div");
        writer.write(banner);
        writer.end();

        List<String> errors = tracker.getErrors();

        if (!errors.isEmpty())
        {
            // Only write out the <UL> if it will contain <LI> elements. An empty <UL> is not
            // valid XHTML.

            writer.element("ul");

            for (String message : errors)
            {
                writer.element("li");
                writer.write(message);
                writer.end();
            }

            writer.end(); // ul
        }

        writer.end(); // div

    }
}
