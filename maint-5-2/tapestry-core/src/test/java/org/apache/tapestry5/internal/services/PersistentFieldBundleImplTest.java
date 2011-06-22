// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.PersistentFieldBundle;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

public class PersistentFieldBundleImplTest extends InternalBaseTestCase
{
    @Test
    public void get_root_component_value()
    {
        String value = "FIELD-VALUE";

        PersistentFieldChange change = new PersistentFieldChangeImpl("", "field", value);
        Collection<PersistentFieldChange> changes = Arrays.asList(change);

        PersistentFieldBundle bundle = new PersistentFieldBundleImpl(changes);

        assertTrue(bundle.containsValue("", "field"));
        assertTrue(bundle.containsValue(null, "field"));

        assertSame(bundle.getValue("", "field"), value);
        assertSame(bundle.getValue(null, "field"), value);

        assertFalse(bundle.containsValue("", "other"));
        assertFalse(bundle.containsValue(null, "other"));
    }

    @Test
    public void get_nested_component_value()
    {
        String value = "FIELD-VALUE";

        PersistentFieldChange change = new PersistentFieldChangeImpl("foo.bar", "field", value);
        Collection<PersistentFieldChange> changes = Arrays.asList(change);

        PersistentFieldBundle bundle = new PersistentFieldBundleImpl(changes);

        assertTrue(bundle.containsValue("foo.bar", "field"));

        assertSame(bundle.getValue("foo.bar", "field"), value);

        assertFalse(bundle.containsValue("foo.bar", "other"));
    }
}
