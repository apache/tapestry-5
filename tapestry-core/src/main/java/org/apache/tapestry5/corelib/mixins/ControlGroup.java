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

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.dom.Element;

/**
 * Applied to a {@link org.apache.tapestry5.Field}, this provides the outer layers of Bootstrap:
 * an outer {@code <div class="control-group">}, a {@code <label class="control-label">}, and a
 * {@code <div class="controls">} around the field itself. This control is not appropriate
 * for radio buttons or check boxes, as those want to have the label element directly around the control.
 * As with the {@link org.apache.tapestry5.corelib.components.Label} component, the {@code for} attribute is set (after the field itself
 * renders).
 *
 * @tapestrydoc
 * @since 5.4
 */
public class ControlGroup
{

    @InjectContainer
    private Field field;

    private Element label;

    void beginRender(MarkupWriter writer)
    {
        writer.element("div", "class", "form-group");
        label = writer.element("label");
        writer.end();
        fillInLabelAttributes();
    }

    @HeartbeatDeferred
    void fillInLabelAttributes()
    {
        label.attribute("for", field.getClientId());
        label.text(field.getLabel());
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end(); // div.form-group
    }
}
