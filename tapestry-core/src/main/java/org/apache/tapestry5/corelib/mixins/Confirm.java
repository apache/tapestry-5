// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;

/**
 * A mixin that can be placed on a clickable component, such as {@link org.apache.tapestry5.corelib.components.LinkSubmit},
 * and will raise a confirmation dialog when the element is clicked.
 * <p/>
 * Due to conflicts between jQuery (as used by Bootstrap's JavaScript library) and Prototype, this mixin does not operate
 * when the {@linkplain org.apache.tapestry5.SymbolConstants#JAVASCRIPT_INFRASTRUCTURE_PROVIDER JavaScript infrastructure provider}
 * is "prototype".
 *
 * @tapestrydoc
 * @since 5.4
 */
@MixinAfter
@Import(module = "t5/core/confirm-click")
public class Confirm
{
    /**
     * The message to present to the user in the body of the modal dialog.
     */
    @Parameter(value = "message:default-confirm-message", defaultPrefix = BindingConstants.LITERAL)
    private String message;

    /**
     * The title for the modal dialog.
     */
    @Parameter(value = "message:default-confirm-title", defaultPrefix = BindingConstants.LITERAL)
    private String title;

    void beginRender(MarkupWriter writer)
    {
        writer.attributes("data-confirm-title", title,
                "data-confirm-message", message);
    }
}
