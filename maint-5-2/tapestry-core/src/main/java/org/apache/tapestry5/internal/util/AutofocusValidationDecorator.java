// Copyright 2008, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Used by {@link org.apache.tapestry5.corelib.components.Form} to determine which fields will be focused and a what
 * priority.
 */
public class AutofocusValidationDecorator extends ValidationDecoratorWrapper
{
    private final ValidationTracker tracker;

    private final JavaScriptSupport jsSupport;

    public AutofocusValidationDecorator(ValidationDecorator delegate, ValidationTracker tracker,
            JavaScriptSupport javascriptSupport)
    {
        super(delegate);
        this.tracker = tracker;
        this.jsSupport = javascriptSupport;
    }

    @Override
    public void insideField(Field field)
    {
        super.insideField(field);

        if (!field.isDisabled())
        {
            jsSupport.autofocus(getPriority(field), field.getClientId());
        }
    }

    private FieldFocusPriority getPriority(Field field)
    {
        if (tracker.inError(field))
            return FieldFocusPriority.IN_ERROR;

        if (field.isRequired())
            return FieldFocusPriority.REQUIRED;

        return FieldFocusPriority.OPTIONAL;
    }
}
