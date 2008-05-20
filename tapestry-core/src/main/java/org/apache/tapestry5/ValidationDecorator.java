// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.dom.Element;

/**
 * An object responsible for performing decorations around fields and field labels. The decorator is notified at
 * intervals by the fields and labels.
 * <p/>
 * In most western languages (written left to right) the label will render before the field, so the properties of the
 * Field may not be set yet (or may reflect a previous looping's rendering). It may be necessary to {@linkplain
 * org.apache.tapestry5.services.Heartbeat#defer(Runnable)} defer any rendering} until after the Label and the Field have
 * both had their change to initialize and render.
 */
public interface ValidationDecorator
{
    /**
     * Invoked by a {@link org.apache.tapestry5.corelib.components.Label} before rendering itself.
     *
     * @param field for this label
     */
    void beforeLabel(Field field);

    /**
     * Invoked after the label has rendered its tag, but before it has rendered content inside the tag, to allow the
     * decorator to write additional attributes.
     *
     * @param field        the field corresponding to the label
     * @param labelElement the element for this label
     */
    void insideLabel(Field field, Element labelElement);


    /**
     * Invoked by {@link org.apache.tapestry5.corelib.components.Label} after rendering itself.
     *
     * @param field
     */
    void afterLabel(Field field);

    /**
     * Renders immediately before the field itself. The field will typically render a single element, though a complex
     * field may render multiple elements or even some JavaScript.
     *
     * @param field
     */
    void beforeField(Field field);

    /**
     * Invoked at a point where the decorator may write additional attributes into the field. Generally speaking, you
     * will want to {@linkplain ComponentResources#renderInformalParameters(MarkupWriter) render informal parameters}
     * <strong>before</strong> invoking this method.
     *
     * @param field
     */
    void insideField(Field field);

    /**
     * Invoked after the field has completed rendering itself.
     */
    void afterField(Field field);
}
