// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class ValidationTrackerImplTest extends TapestryTestCase
{
    @Test
    public void empty_tracker_has_no_errors()
    {
        ValidationTracker tracker = new ValidationTrackerImpl();

        assertTrue(tracker.getErrors().isEmpty());
        assertFalse(tracker.getHasErrors());
    }

    @Test
    public void order_added_is_maintained()
    {
        Field fielda = newFieldWithControlName("fieldA");
        Field fieldb = newFieldWithControlName("fieldB");

        replay();

        ValidationTracker tracker = new ValidationTrackerImpl();

        tracker.recordError("one");
        tracker.recordError(fieldb, "fieldb: two");
        tracker.recordError("three");
        tracker.recordError(fielda, "fielda: four");

        assertEquals(tracker.getErrors(), Arrays.asList(
                "one",
                "three",
                "fieldb: two",
                "fielda: four"));

        verify();
    }

    @Test
    public void record_input()
    {
        Field field = newFieldWithControlName("field");

        replay();

        ValidationTracker tracker = new ValidationTrackerImpl();

        assertNull(tracker.getInput(field));

        tracker.recordInput(field, "one");

        assertEquals(tracker.getInput(field), "one");

        tracker.recordInput(field, "two");

        assertEquals(tracker.getInput(field), "two");

        verify();
    }

    @Test
    public void record_error_for_field()
    {
        Field field = newFieldWithControlName("field");

        replay();

        ValidationTracker tracker = new ValidationTrackerImpl();

        assertFalse(tracker.getHasErrors());
        assertFalse(tracker.inError(field));
        assertNull(tracker.getError(field));

        tracker.recordError(field, "one");

        assertTrue(tracker.getHasErrors());
        assertTrue(tracker.inError(field));
        assertEquals(tracker.getError(field), "one");

        tracker.recordError(field, "two");
        assertEquals(tracker.getError(field), "two");

        verify();
    }

    @Test
    public void record_error_for_form()
    {
        ValidationTracker tracker = new ValidationTrackerImpl();

        assertFalse(tracker.getHasErrors());

        assertTrue(tracker.getErrors().isEmpty());

        tracker.recordError("one");

        assertEquals(tracker.getErrors(), Arrays.asList("one"));

        tracker.recordError("two");

        assertEquals(tracker.getErrors(), Arrays.asList("one", "two"));
    }

    @Test
    public void data_survives_serialization() throws Exception
    {
        Field fielda = newFieldWithControlName("fieldA");
        Field fieldb = newFieldWithControlName("fieldB");
        Field fieldc = newFieldWithControlName("fieldC");

        replay();

        ValidationTracker tracker = new ValidationTrackerImpl();

        tracker.recordError("one");
        tracker.recordError(fieldb, "fieldb: two");
        tracker.recordError("three");
        tracker.recordError(fielda, "fielda: four");

        ValidationTracker copy = cloneBySerialiation(tracker);

        copy.recordError(fieldc, "fieldc: five");

        assertEquals(copy.getErrors(), Arrays.asList(
                "one",
                "three",
                "fieldb: two",
                "fielda: four",
                "fieldc: five"));

        verify();
    }

    @Test
    public void clear_removes_all()
    {
        Field fielda = newFieldWithControlName("fieldA");
        Field fieldb = newFieldWithControlName("fieldB");

        replay();

        ValidationTracker tracker = new ValidationTrackerImpl();

        tracker.recordError("one");
        tracker.recordInput(fieldb, "input b");
        tracker.recordError(fieldb, "fieldb: two");
        tracker.recordError("three");
        tracker.recordInput(fielda, "input a");
        tracker.recordError(fielda, "fielda: four");

        tracker.clear();

        assertFalse(tracker.getHasErrors());
        assertTrue(tracker.getErrors().isEmpty());
        assertNull(tracker.getInput(fielda));
        assertNull(tracker.getInput(fieldb));

        verify();
    }

    private final Field newFieldWithControlName(String controlName)
    {
        Field field = mockField();

        // Fields generated this way, for the purposes of this test, do not
        // ever change their controlName. In real life, elementNames can change.

        expect(field.getControlName()).andReturn(controlName).atLeastOnce();

        return field;
    }

    @SuppressWarnings("unchecked")
    protected final <T> T cloneBySerialiation(T input) throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(input);

        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        T result = (T) ois.readObject();

        ois.close();

        return result;
    }
}
