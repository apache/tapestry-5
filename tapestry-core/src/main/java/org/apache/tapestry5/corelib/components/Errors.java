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

import java.util.List;
import java.util.Set;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentOverride;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

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
//@Import(module = "bootstrap/alert")
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
     * If true, then only errors global to the form (unassociated with any specific field) are
     * presented. By default all errors (associated with fields, or not) are presented; with unassociated
     * errors presented first.
     *
     * @since 5.4
     */
    @Parameter
    private boolean globalOnly;

    /**
     * The CSS class for the div element rendered by the component.
     */
    @Parameter(name = "class", defaultPrefix = BindingConstants.LITERAL, value = "symbol:" + SymbolConstants.ERRORS_DEFAULT_CLASS_PARAMETER_VALUE)
    private String className;

    // Allow null so we can generate a better error message if missing
    @Environmental(false)
    private ValidationTracker tracker;
    
    @Inject
    @Symbol(SymbolConstants.ERRORS_BASE_CSS_CLASS)
    private String baseCssClass;

    @Inject
    @Symbol(SymbolConstants.ERRORS_CLOSE_BUTTON_CSS_CLASS)
    private String closeButtonCssClass;
    
    @Inject
    private JavaScriptSupport javaScriptSupport;

    boolean beginRender(MarkupWriter writer)
    {
        if (tracker == null)
            throw new RuntimeException("The Errors component must be enclosed by a Form component.");

        if (!tracker.getHasErrors())
        {
            return false;
        }

        List<String> errors =
                globalOnly ? tracker.getUnassociatedErrors() : tracker.getErrors();

        if (errors.isEmpty())
        {
            return false;
        }
        
        setUpJavaScript();

        Set<String> previousErrors = CollectionFactory.newSet();

        writer.element("div", "class", baseCssClass + " " + className, "role", "alert");
        writer.element("button",
                "type", "button",
                "class", closeButtonCssClass,
                "data-dismiss", "alert");
        writer.writeRaw("&times;");
        writer.end();

        writer.element("h4");
        writer.writeRaw(banner);
        writer.end();

        writer.element("ul");

        for (String message : errors)
        {
            if (previousErrors.contains(message))
            {
                continue;
            }

            writer.element("li");
            writer.write(message);
            writer.end();

            previousErrors.add(message);
        }

        writer.end(); // ul

        writer.end(); // div
        
        return false;
    }

    /**
     * Hook to set up JavaScript, specifically for handling the close button.
     * Method intended to be overriden when Bootstrap isn't being used.
     * This implementation calls <code>javaScriptSupport.require("bootstrap/alert");</code>.
     * @since 5.5
     * @see ComponentOverride
     */
    protected void setUpJavaScript() {
        javaScriptSupport.require("bootstrap/alert");
    }
    
}
