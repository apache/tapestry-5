// Copyright 2013 The Apache Software Foundation
//
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
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Renders a dropdown menu of useful options when developing. By default, the DevTool is disabled (invisible)
 * during production.
 * <p/>
 * Note that due to conflicts between Prototype and jQuery, the dev tool is hidden after selecting an item from the menu.
 *
 * @tapestrydoc
 * @since 5.4
 */
public class DevTool
{
    /**
     * If false, then the component does not render. Defaults to true unless production mode is enabled.
     */
    @Parameter
    private boolean enabled;

    /**
     * If true, then the DevTool modifies its markup so as to work within a Bootstrap 3 NavBar. This renders
     * the component as an {@code <li>} (instead of a {@code <div>}), and removes the "btn" CSS classes.
     */
    @Parameter
    private boolean navbar;

    /**
     * Additional CSS selectors, e.g., "pull-right" or "dropup".
     */
    @Parameter(name = "class", defaultPrefix = BindingConstants.LITERAL)
    private String className;


    @Property
    @Inject
    @Symbol(SymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    @Component(inheritInformalParameters = true, parameters = {
            "class=zoneClass",
            "elementName=${zoneElement}"
    })
    private Zone devmodezone;


    @Inject
    private AlertManager alertManager;

    @Inject
    private Request request;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @Inject
    private ComponentResources resources;

    @Inject
    private ComponentInstantiatorSource componentInstantiatorSource;

    public String getZoneElement()
    {
        return navbar ? "li" : "div";
    }

    public String getZoneClass()
    {
        return "dropdown" + (className == null ? "" : " " + className);
    }

    public String getTriggerClass()
    {
        return "dropdown-toggle" + (navbar ? "" : " btn btn-default btn-xs");
    }

    boolean defaultEnabled()
    {
        return !productionMode;
    }

    /**
     * When disabled, this prevents any part of the tool from rendering.
     */
    boolean beginRender()
    {
        if (enabled)
        {
            javaScriptSupport.importStack("core").require("bootstrap/dropdown");
        }

        return enabled;
    }

    Object onActionFromReset()
    {
        if (!productionMode)
        {
            resources.discardPersistentFieldChanges();

            alertManager.info("Page state discarded.");
        }

        return devmodezone.getBody();
    }

    Object onActionFromKill()
    {
        if (!productionMode)
        {
            Session session = request.getSession(false);

            if (session == null)
            {
                alertManager.info("No server-side session currently exist.");
            } else
            {
                session.invalidate();
                alertManager.info("Server-side session invalidated.");
            }
        }

        return devmodezone.getBody();
    }

    Object onActionFromReload()
    {
        if (productionMode)
        {
            alertManager.error("Forcing a class reload is only allowed when executing in development mode.");
            return null;
        }

        componentInstantiatorSource.forceComponentInvalidation();

        alertManager.info("Forced a component class reload.");

        return devmodezone.getBody();
    }
}
