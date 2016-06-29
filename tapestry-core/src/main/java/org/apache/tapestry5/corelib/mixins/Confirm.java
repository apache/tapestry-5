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
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * A mixin that can be placed on a clickable component, such as {@link org.apache.tapestry5.corelib.components.LinkSubmit},
 * and will raise a confirmation dialog when the element is clicked.
 *
 * @tapestrydoc
 * @since 5.4
 */
@MixinAfter
public class Confirm
{
    /**
     * The message to present to the user in the body of the modal dialog.
     */
    @Parameter(value = "message:private-default-confirm-message", defaultPrefix = BindingConstants.LITERAL)
    private String message;

    /**
     * The title for the modal dialog.
     */
    @Parameter(value = "message:private-default-confirm-title", defaultPrefix = BindingConstants.LITERAL)
    private String title;

    /**
     * If true, then the mixin does nothing (no attributes added, no module imported).
     */
    @Parameter("false")
    private boolean disabled;

    @Environmental
    private JavaScriptSupport javaScriptSupport;
    
    /*
     * The CSS class for the ok button
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String okClass;

    /**
     * The label for the ok button.
     */
    @Parameter(value = "message:private-default-confirm-ok", defaultPrefix = BindingConstants.LITERAL)
    private String ok;

    /**
     * The label for the ok button.
     */
    @Parameter(value = "message:private-default-confirm-cancel", defaultPrefix = BindingConstants.LITERAL)
    private String cancel;

    void beginRender(MarkupWriter writer)
    {
        if (!disabled)
        {
            javaScriptSupport.require("t5/core/confirm-click");

            writer.attributes("data-confirm-title", title,
                    "data-confirm-message", message,
                    "data-confirm-class-ok", okClass,
                    "data-confirm-label-ok", ok,
                    "data-confirm-label-cancel", cancel);
        }
    }
}
