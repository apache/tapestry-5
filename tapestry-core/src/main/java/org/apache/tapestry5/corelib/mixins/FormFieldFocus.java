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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * A mixin that instruments the outer {@link org.apache.tapestry5.corelib.components.Form} on which
 * component the focus should be activated.
 * 
 * This is meant to be used only with {@link org.apache.tapestry5.corelib.components.Form} component.
 *
 * @since 5.3
 * @deprecated As of release 5.4, replaced by {@link org.apache.tapestry5.corelib.mixins.OverrideFieldFocus}
 * @tapestrydoc
 */
@Deprecated
public class FormFieldFocus
{
    @Inject
    private Logger logger;

    /**
     * The outer Form
     */
    @InjectContainer
    private Form form;

    /**
     * The {@link org.apache.tapestry5.Field} instance that will receive the focus within
     * the {@link org.apache.tapestry5.corelib.components.Form}.
     */
    @Parameter(required = true, defaultPrefix = BindingConstants.COMPONENT, allowNull = false)
    private Field focusField;

    @Environmental
    private JavaScriptSupport javascriptSupport;


    @AfterRender
    void focusField()
    {
        javascriptSupport.autofocus(FieldFocusPriority.OVERRIDE, focusField.getClientId());

        logger.trace("Focus OVERRIDE done on field {}", focusField.getClientId());
    }

}
