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
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Renders a {@code <span>} tag with the CSS class to select a <a href="http://getbootstrap.com/components/#glyphicons">Bootstrap Glyphicon</a>.
 * 
 * If Twitter Bootstrap 3 isn't enabled (i.e @{link Trait#BOOTSTRAP_3 is set to false),
 * this usages of this component are automatically and transparently replaced by {@link FontAwesomeIcon}.
 *
 * @see org.apache.tapestry5.services.compatibility.Trait#BOOTSTRAP_3
 *
 * @tapestrydoc
 * @since 5.4
 */
@SupportsInformalParameters
public class Glyphicon
{
    /**
     * The name of the icon, e.g., "arrow-up", "flag", "fire" etc.
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    String name;

    @Inject
    ComponentResources resources;

    boolean beginRender(MarkupWriter writer)
    {
        writer.element("span",
                "class", "glyphicon glyphicon-" + name);
        resources.renderInformalParameters(writer);
        writer.end();

        return false;
    }
}
