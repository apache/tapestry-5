// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldComponent;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.internal.services.StringInterner;
import org.apache.tapestry5.internal.services.StringInternerImpl;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.FieldValidatorSource;
import org.testng.annotations.Test;

public class ValidateBindingFactoryTest extends InternalBaseTestCase
{
    private StringInterner interner = new StringInternerImpl();

    @Test
    public void not_a_field()
    {
        FieldValidatorSource source = mockFieldValidatorSource();
        ComponentResources container = mockComponentResources();
        ComponentResources component = mockComponentResources();
        Component instance = mockComponent();
        Location l = mockLocation();

        train_getComponent(component, instance);
        train_getCompleteId(component, "foo.Bar:baz");

        replay();

        BindingFactory factory = new ValidateBindingFactory(source, interner);

        try
        {
            factory.newBinding("descrip", container, component, "zip,zoom", l);
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Component 'foo.Bar:baz' is not a field (it does not implement the Field interface) and may not be used with the validate: binding prefix.");
            assertSame(ex.getLocation(), l);
        }

        verify();
    }

    @Test
    public void success()
    {

        FieldValidatorSource source = mockFieldValidatorSource();
        ComponentResources container = mockComponentResources();
        ComponentResources component = mockComponentResources();
        FieldComponent instance = mockFieldComponent();
        Location l = mockLocation();
        FieldValidator validator = mockFieldValidator();

        String expression = "required,minLength=5";

        train_getComponent(component, instance);

        expect(source.createValidators(instance, expression)).andReturn(validator);

        replay();

        BindingFactory factory = new ValidateBindingFactory(source, interner);

        Binding binding = factory.newBinding("descrip", container, component, expression, l);

        assertSame(binding.get(), validator);

        verify();
    }
}
