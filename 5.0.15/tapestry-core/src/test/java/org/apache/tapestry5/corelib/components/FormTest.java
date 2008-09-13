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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class FormTest extends TapestryTestCase
{
    @Test
    public void record_error()
    {
        ValidationTracker tracker = mockValidationTracker();
        String message = "A recorded message.";

        tracker.recordError(message);

        replay();

        Form form = new Form();

        form.setTracker(tracker);

        form.recordError(message);

        verify();
    }

    @Test
    public void record_error_for_field()
    {
        ValidationTracker tracker = mockValidationTracker();
        String message = "A recorded message.";
        Field field = mockField();

        tracker.recordError(field, message);

        replay();

        Form form = new Form();

        form.setTracker(tracker);

        form.recordError(field, message);

        verify();
    }
}
