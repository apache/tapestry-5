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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.integration.app1.data.RegistrationData;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class BeanEditFormTest extends TapestryTestCase
{
    @Test
    public void object_created_as_needed()
    {
        ComponentResources resources = mockComponentResources();

        expect(resources.triggerEvent(Form.PREPARE, null, null)).andReturn(false);

        train_getBoundType(resources, "object", RegistrationData.class);

        replay();

        BeanEditForm component = new BeanEditForm();

        component.inject(resources);

        component.onPrepareFromForm();

        Object object = component.getObject();

        assertNotNull(object);
        assertSame(object.getClass(), RegistrationData.class);

        verify();
    }

    @Test
    public void object_can_not_be_instantiated()
    {
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        expect(resources.triggerEvent(Form.PREPARE, null, null)).andReturn(false);

        train_getBoundType(resources, "object", Runnable.class);

        train_getCompleteId(resources, "Foo.bar");

        train_getLocation(resources, l);

        replay();

        BeanEditForm component = new BeanEditForm();

        component.inject(resources);

        try
        {
            component.onPrepareFromForm();
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertMessageContains(
                    ex,
                    "Exception instantiating instance of java.lang.Runnable (for component \'Foo.bar\'):");
            assertSame(ex.getLocation(), l);
        }

        verify();
    }
}
