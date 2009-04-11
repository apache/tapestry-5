// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.*;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;

/**
 * Default implementation that writes an attribute into fields or labels that are in error.
 */
public final class DefaultValidationDecorator extends BaseValidationDecorator
{
    private final Environment environment;

    private final Asset spacerAsset;

    private final MarkupWriter markupWriter;

    /**
     * @param environment  used to locate objects and services during the render
     * @param spacerAsset  asset for a one-pixel spacer image used as a placeholder for the error marker icon
     * @param markupWriter
     */
    public DefaultValidationDecorator(Environment environment, Asset spacerAsset, MarkupWriter markupWriter)
    {
        this.environment = environment;
        this.spacerAsset = spacerAsset;
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

        if (inError(field)) element.addClassName(CSSClassConstants.ERROR);
    }

    /**
     * Writes an icon for field after the field.  The icon has the same id as the field, with ":icon" appended. This is
     * expected by the default client-side JavaScript.  The icon's src is a blank spacer image (this is to allow the
     * image displayed to be overridden via CSS).     The icon's CSS class is "t-error-icon", with "t-invisible" added
     * if the field is not in error when rendered.  If client validation is not enabled for the form containing the
     * field and the field is not in error, then the error icon itself is not rendered.
     *
     * @param field which just completed rendering itself
     */
    @Override
    public void afterField(Field field)
    {
        boolean inError = inError(field);

        boolean clientValidationEnabled = getFormSupport().isClientValidationEnabled();

        if (inError || clientValidationEnabled)
        {
            String iconId = field.getClientId() + "-icon";

            String cssClass = inError ? "t-error-icon" : "t-error-icon t-invisible";

            markupWriter.element("img",
                                 "src", spacerAsset.toClientURL(),
                                 "alt", "",
                                 "class", cssClass,
                                 "id", iconId);
            markupWriter.end();
        }

    }

    private FormSupport getFormSupport()
    {
        return environment.peekRequired(FormSupport.class);
    }

    private boolean inError(Field field)
    {
        ValidationTracker tracker = environment.peekRequired(ValidationTracker.class);

        return tracker.inError(field);
    }

    private void addErrorClassToCurrentElement()
    {
        markupWriter.getElement().addClassName(CSSClassConstants.ERROR);
    }
}
