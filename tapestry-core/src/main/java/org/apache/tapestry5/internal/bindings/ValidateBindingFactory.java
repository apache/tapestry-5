// Copyright 2006, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.internal.services.StringInterner;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.FieldValidatorSource;

/**
 * Factory for bindings that provide a {@link org.apache.tapestry5.FieldValidator} based on a validator specification.
 * This binding factory is only useable with components that implement the {@link org.apache.tapestry5.Field}
 * interface.
 */
public class ValidateBindingFactory implements BindingFactory
{
    private final FieldValidatorSource fieldValidatorSource;

    private final StringInterner interner;

    public ValidateBindingFactory(FieldValidatorSource fieldValidatorSource, StringInterner interner)
    {
        this.fieldValidatorSource = fieldValidatorSource;
        this.interner = interner;
    }

    public Binding newBinding(String description, ComponentResources container,
                              ComponentResources component, final String expression, Location location)
    {
        Object fieldAsObject = component.getComponent();

        if (!Field.class.isInstance(fieldAsObject))
            throw new TapestryException(BindingsMessages.validateBindingForFieldsOnly(component),
                                        location, null);

        final Field field = (Field) fieldAsObject;

        return new InvariantBinding(location, FieldValidator.class, interner.intern(description + ": " + expression))
        {
            public Object get()
            {
                // The expression is a validator specification, such as "required,minLength=5".
                // ValidatorBindingFactory is the odd man out becasuse it needs the binding component (the
                // component whose parameter is to be bound) rather than the containing component, the way
                // most factories work.

                return fieldValidatorSource.createValidators(field, expression);
            }
        };
    }
}
