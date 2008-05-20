// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class AbstractPropertyOutputTest extends InternalBaseTestCase
{
    private final AbstractPropertyOutput propertyOutputFixture = new AbstractPropertyOutput()
    {
    };

    @Test
    // Tests TAPESTRY-2182.
    public void test_null_pointer_exception_message()
    {
        final PropertyConduit conduit = mockPropertyConduit();
        final PropertyModel model = mockPropertyModel();
        final Object object = new Object();

        propertyOutputFixture.inject(model, object);

        expect(model.getConduit()).andReturn(conduit);
        expect(conduit.get(object)).andThrow(new NullPointerException());
        expect(model.getPropertyName()).andReturn("wilma.occupation.address");

        replay();

        try
        {
            propertyOutputFixture.readPropertyForObject();

            fail("Expected a NullPointerException to be thrown.");
        }
        catch (final NullPointerException ex)
        {
            assertEquals(ex.getMessage(), "Property 'wilma.occupation.address' contains a null value in the path.");
        }

        verify();
    }
}
