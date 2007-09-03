// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import java.util.List;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.services.FormSupport;

/**
 * Standard validation error presenter. Must be enclosed by a {@link Form} component. If errors are
 * present, renders a div element around a banner message and around an unnumbered list of error
 * messages.
 */
public class Errors
{
    /**
     * The banner message displayed above the errors. The default value is "You must correct the
     * following errors before you may continue.".
     */
    @Parameter("message:default-banner")
    private String _banner;

    /** The CSS class for the div element rendered by the component. The default value is "t-error". */
    @Parameter
    private String _class = InternalConstants.TAPESTRY_ERROR_CLASS;

    // Allow null so we can generate a better error message if missing
    @Environmental(false)
    private ValidationTracker _tracker;

    @Environmental
    private FormSupport _formSupport;

    void beginRender(MarkupWriter writer)
    {
        // TODO: Would be nice if there was a Location to report ... can we add a Location property
        // to ComponentResources?

        if (_tracker == null) throw new RuntimeException(ComponentMessages.encloseErrorsInForm());

        String cssClass = _tracker.getHasErrors() ? _class : _class + " t-invisible";

        writer.element("div", "class", cssClass, "id", _formSupport.getClientId() + ":errors");

        // Inner div for the banner text
        writer.element("div");
        writer.write(_banner);
        writer.end();

        List<String> errors = _tracker.getErrors();

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
