// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal;

import org.apache.tapestry.*;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.ValidationMessagesSource;

/**
 * Default implementation that writes an attribute into fields or labels that are in error.
 */
public final class DefaultValidationDecorator extends BaseValidationDecorator
{
    private final Environment _environment;

    private Asset _iconAsset;

    private Messages _validationMessages;

    /**
     * @param environment        used to locate objects and services during the render
     * @param validationMessages obtained from {@link ValidationMessagesSource}, used to obtain the label for the
     *                           icon
     * @param iconAsset          asset for an icon that will be displayed after each field (marked with the
     *                           "t-invisible" CSS class, if the field is not in error)
     */
    public DefaultValidationDecorator(final Environment environment, Messages validationMessages,
                                      Asset iconAsset)
    {
        _environment = environment;
        _validationMessages = validationMessages;
        _iconAsset = iconAsset;
    }

    @Override
    public void insideField(Field field)
    {
        if (inError(field))
            addErrorClassToCurrentElement();
    }

    @Override
    public void insideLabel(Field field, Element element)
    {
        if (field == null)
            return;

        if (inError(field))
            addErrorClass(element);
    }

    @Override
    public void afterField(Field field)
    {
        String iconId = field.getClientId() + ":icon";

        MarkupWriter writer = _environment.peekRequired(MarkupWriter.class);

        String cssClass = inError(field) ? "t-error-icon" : "t-error-icon t-invisible";

        writer.element("img", "src", _iconAsset.toClientURL(), "alt", _validationMessages
                .get("icon-label"), "class", cssClass, "id", iconId);
        writer.end();
    }

    private boolean inError(Field field)
    {
        ValidationTracker tracker = _environment.peekRequired(ValidationTracker.class);

        return tracker.inError(field);
    }

    private void addErrorClassToCurrentElement()
    {
        MarkupWriter writer = _environment.peekRequired(MarkupWriter.class);

        Element element = writer.getElement();

        addErrorClass(element);
    }

    private void addErrorClass(Element element)
    {
        String current = element.getAttribute("class");

        String newValue = current == null ? InternalConstants.TAPESTRY_ERROR_CLASS : current + " "
                + InternalConstants.TAPESTRY_ERROR_CLASS;

        element.forceAttributes("class", newValue);
    }

}
