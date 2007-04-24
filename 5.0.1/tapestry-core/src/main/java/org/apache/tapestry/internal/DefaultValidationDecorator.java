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

import org.apache.tapestry.BaseValidationDecorator;
import org.apache.tapestry.Field;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.services.Environment;

/**
 * Default implementation that writes an attribute into fields or labels that are in error.
 */
public final class DefaultValidationDecorator extends BaseValidationDecorator
{
    private final Environment _environment;

    public DefaultValidationDecorator(final Environment environment)
    {
        _environment = environment;
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
