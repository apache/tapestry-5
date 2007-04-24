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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BindingFactory;
import org.apache.tapestry.services.FieldValidatorSource;

/**
 * Factory for bindings that provide a {@link FieldValidator} based on a validator specification.
 * This binding factory is only useable with components that implement the {@link Field} interface.
 */
public class ValidateBindingFactory implements BindingFactory
{
    private final FieldValidatorSource _fieldValidatorSource;

    public ValidateBindingFactory(FieldValidatorSource fieldValidatorSource)
    {
        _fieldValidatorSource = fieldValidatorSource;
    }

    public Binding newBinding(String description, ComponentResources container,
            ComponentResources component, String expression, Location location)
    {
        Object fieldAsObject = component.getComponent();

        if (!Field.class.isInstance(fieldAsObject))
            throw new TapestryException(BindingsMessages.validateBindingForFieldsOnly(component),
                    location, null);

        Field field = (Field) fieldAsObject;

        // The expression is a validator specification, such as "required,minLength=5".
        // ValidatorBindingFactory is the odd man out becasuse it needs the binding component (the
        // component whose parameter is to be bound) rather than the containing component, the way
        // most factories work.

        FieldValidator validator = _fieldValidatorSource.createValidators(field, expression);

        return new LiteralBinding(description, validator, location);
    }

}
