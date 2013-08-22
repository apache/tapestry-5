// Copyright 2006-2013 The Apache Software Foundation
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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;

import java.util.List;

/**
 * Standard validation error presenter. Must be enclosed by a
 * {@link org.apache.tapestry5.corelib.components.Form} component. If errors are present, renders a
 * {@code <div>} element around a banner message and around an unnumbered list of
 * error messages. Renders nothing if the {@link org.apache.tapestry5.ValidationTracker} shows no
 * errors.
 *
 * @tapestrydoc
 * @see Form
 */
@Import(module = "bootstrap-alert")
public class Errors
{
    /**
     * The banner message displayed above the errors. The default value is "You must correct the
     * following errors before
     * you may continue.".
     */
    @Parameter("message:core-default-error-banner")
    private String banner;

    /**
     * The CSS class for the div element rendered by the component. The default value is "alert alert-error alert-block".
     */
    @Parameter(name = "class")
    private String className = "alert alert-error";

    // Allow null so we can generate a better error message if missing
    @Environmental(false)
    private ValidationTracker tracker;

    void beginRender(MarkupWriter writer)
    {
        if (tracker == null)
            throw new RuntimeException("The Errors component must be enclosed by a Form component.");

        if (!tracker.getHasErrors())
        {
            return;
        }

        writer.element("div", "class", "alert-dismissable " + className);
        writer.element("button",
                "type", "button",
                "class", "close",
                "data-dismiss", "alert");
        writer.writeRaw("&times;");
        writer.end();

        writer.element("h4");
        writer.writeRaw(banner);
        writer.end();

        List<String> errors = tracker.getErrors();

        writer.element("ul");

        for (String message : errors)
        {
            writer.element("li");
            writer.write(message);
            writer.end();
        }

        writer.end(); // ul

        writer.end(); // div
    }
}
