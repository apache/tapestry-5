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

package org.apache.tapestry.internal;

import org.apache.tapestry.*;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.Environment;

/**
 * Default implementation that writes an attribute into fields or labels that are in error.
 */
public final class DefaultValidationDecorator extends BaseValidationDecorator
{
    private final Environment environment;

    private final Asset iconAsset;

    private final Messages validationMessages;

    private final MarkupWriter markupWriter;

    /**
     * @param environment        used to locate objects and services during the render
     * @param validationMessages obtained from {@link org.apache.tapestry.services.ValidationMessagesSource}, used to
     *                           obtain the label for the icon
     * @param iconAsset          asset for an icon that will be displayed after each field (marked with the
     * @param markupWriter
     */
    public DefaultValidationDecorator(Environment environment, Messages validationMessages, Asset iconAsset,
                                      MarkupWriter markupWriter)
    {
        this.environment = environment;
        this.validationMessages = validationMessages;
        this.iconAsset = iconAsset;
        this.markupWriter = markupWriter;
    }

    @Override
    public void insideField(Field field)
    {
        if (inError(field)) addErrorClassToCurrentElement();
    }

    @Override
    public void insideLabel(Field field, Element element)
    {
        if (field == null) return;

        if (inError(field)) element.addClassName(TapestryConstants.ERROR_CLASS);
    }

    @Override
    public void afterField(Field field)
    {
        String iconId = field.getClientId() + ":icon";

        String cssClass = inError(field) ? "t-error-icon" : "t-error-icon t-invisible";

        markupWriter.element("img",

                             "src", iconAsset.toClientURL(),

                             "alt", validationMessages.get("icon-label"),

                             "class", cssClass,

                             "id", iconId);
        markupWriter.end();
    }

    private boolean inError(Field field)
    {
        ValidationTracker tracker = environment.peekRequired(ValidationTracker.class);

        return tracker.inError(field);
    }

    private void addErrorClassToCurrentElement()
    {
        markupWriter.getElement().addClassName(TapestryConstants.ERROR_CLASS);
    }
}
