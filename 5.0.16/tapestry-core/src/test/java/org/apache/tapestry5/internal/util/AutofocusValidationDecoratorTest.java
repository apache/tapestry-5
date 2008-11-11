//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.*;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class AutofocusValidationDecoratorTest extends TapestryTestCase
{
    @Test
    public void field_is_disabled()
    {
        Field field = mockField();
        ValidationDecorator delegate = mockValidationDecorator();
        ValidationTracker tracker = mockValidationTracker();
        RenderSupport renderSupport = mockRenderSupport();

        delegate.insideField(field);

        train_isDisabled(field, true);

        replay();

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, renderSupport);

        decorator.insideField(field);

        verify();
    }

    @Test
    public void field_is_in_error()
    {
        Field field = mockField();
        ValidationDecorator delegate = mockValidationDecorator();
        ValidationTracker tracker = mockValidationTracker();
        RenderSupport renderSupport = mockRenderSupport();

        delegate.insideField(field);

        train_isDisabled(field, false);
        train_inError(tracker, field, true);

        train_getClientId(field, "foo");

        renderSupport.autofocus(FieldFocusPriority.IN_ERROR, "foo");

        replay();

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, renderSupport);

        decorator.insideField(field);

        verify();
    }

    @Test
    public void field_is_required()
    {
        Field field = mockField();
        ValidationDecorator delegate = mockValidationDecorator();
        ValidationTracker tracker = mockValidationTracker();
        RenderSupport renderSupport = mockRenderSupport();

        delegate.insideField(field);

        train_isDisabled(field, false);
        train_inError(tracker, field, false);

        train_isRequired(field, true);

        train_getClientId(field, "foo");

        renderSupport.autofocus(FieldFocusPriority.REQUIRED, "foo");

        replay();

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, renderSupport);

        decorator.insideField(field);

        verify();
    }

    @Test
    public void field_is_optional()
    {
        Field field = mockField();
        ValidationDecorator delegate = mockValidationDecorator();
        ValidationTracker tracker = mockValidationTracker();
        RenderSupport renderSupport = mockRenderSupport();

        delegate.insideField(field);

        train_isDisabled(field, false);
        train_inError(tracker, field, false);

        train_isRequired(field, false);

        train_getClientId(field, "foo");

        renderSupport.autofocus(FieldFocusPriority.OPTIONAL, "foo");

        replay();

        ValidationDecorator decorator = new AutofocusValidationDecorator(delegate, tracker, renderSupport);

        decorator.insideField(field);

        verify();
    }

}
