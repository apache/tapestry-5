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

import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * A mixin that let a {@link org.apache.tapestry5.Field} gain focus.
 *
 * This supersede {@link org.apache.tapestry5.corelib.mixins.FormFieldFocus} in 5.4
 *
 * @since 5.4
 * @tapestrydoc
 */
public class OverrideFieldFocus
{
    @Inject
    private Logger logger;

    /**
     * The outer Form
     */
    @InjectContainer
    private Field container;

    @Environmental
    private JavaScriptSupport javascriptSupport;


    @AfterRender
    void focusField()
    {
        javascriptSupport.autofocus(FieldFocusPriority.OVERRIDE, container.getClientId());

        logger.trace("Focus OVERRIDE done on field {}", container.getClientId());
    }

}
